package com.iptv.ccomate.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class SubscriptionResult {
    data class Subscribed(val clientIp: String?) : SubscriptionResult()
    data class NotSubscribed(val message: String, val clientIp: String?) : SubscriptionResult()
    object NotFound : SubscriptionResult()
    data class Error(val message: String) : SubscriptionResult()
}

sealed class RegisterResult {
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

@Singleton
class SubscriptionManager @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun checkSubscription(installationId: String): SubscriptionResult =
        withContext(Dispatchers.IO) {
            val url = "${AppConfig.CHECK_SUBSCRIPTION_URL}?installationId=$installationId"
            val request = Request.Builder().url(url).get().build()

            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: "{}"
                val json = JSONObject(bodyString)

                val success = json.getBoolean("success")
                if (!success) {
                    val message = json.optString("message", "Unknown error")
                    if (message.contains("Device not found")) {
                        return@withContext SubscriptionResult.NotFound
                    }
                    return@withContext SubscriptionResult.Error(message)
                }

                val clientIp = json.optString("clientIp", null)
                val isSubscribed = json.getBoolean("subscribed")

                if (isSubscribed) {
                    SubscriptionResult.Subscribed(clientIp)
                } else {
                    SubscriptionResult.NotSubscribed("No tienes una suscripcion activa", clientIp)
                }
            } catch (e: Exception) {
                SubscriptionResult.Error("Error de red: ${e.message}")
            }
        }

    suspend fun registerDevice(deviceInfo: DeviceInfo, clientIp: String?): RegisterResult =
        withContext(Dispatchers.IO) {
            val requestBody = FormBody.Builder()
                .add("installationId", deviceInfo.installationId)
                .add("localIp", deviceInfo.localIp ?: "unknown")
                .add("deviceModel", deviceInfo.deviceModel)
                .apply {
                    deviceInfo.dni?.let { add("dni", it) }
                    deviceInfo.name?.let { add("name", it) }
                    deviceInfo.phone?.let { add("phone", it) }
                    clientIp?.let { add("gatewayIp", it) }
                }
                .build()

            val request = Request.Builder()
                .url(AppConfig.REGISTER_DEVICE_URL)
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: "{}"
                val json = JSONObject(bodyString)
                val success = json.getBoolean("success")
                if (success) {
                    RegisterResult.Success
                } else {
                    val message = json.optString("message", "Unknown error")
                    RegisterResult.Error(message)
                }
            } catch (e: Exception) {
                RegisterResult.Error("Error de red: ${e.message}")
            }
        }
}
