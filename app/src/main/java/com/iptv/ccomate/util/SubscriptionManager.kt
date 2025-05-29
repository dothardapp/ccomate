package com.iptv.ccomate.util

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object SubscriptionManager {
    private const val SERVER_URL = "http://10.224.24.233:8000/api/register-device"
    private const val CHECK_SUBSCRIPTION_URL = "http://10.224.24.233:8000/api/check-subscription"
    private val client = OkHttpClient()

    fun registerDevice(deviceInfo: DeviceInfo, callback: (Boolean, String?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("installationId", deviceInfo.installationId)
            .add("localIp", deviceInfo.localIp ?: "unknown")
            .add("deviceModel", deviceInfo.deviceModel)
            .apply {
                deviceInfo.dni?.let { add("dni", it) }
                deviceInfo.name?.let { add("name", it) }
                deviceInfo.phone?.let { add("phone", it) }
            }
            .build()

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    val json = JSONObject(body ?: "{}")
                    val success = json.getBoolean("success")
                    val message = json.optString("message", "Unknown error")
                    callback(success, if (success) null else message)
                } catch (e: Exception) {
                    callback(false, "Parsing error: ${e.message}")
                }
            }
        })
    }

    fun checkSubscription(installationId: String, callback: (Boolean, String?) -> Unit) {
        val url = "$CHECK_SUBSCRIPTION_URL?installationId=$installationId"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    val json = JSONObject(body ?: "{}")
                    val success = json.getBoolean("success")
                    if (!success) {
                        callback(false, json.optString("message", "Unknown error"))
                        return
                    }
                    val isSubscribed = json.getBoolean("subscribed")
                    callback(isSubscribed, null)
                } catch (e: Exception) {
                    callback(false, "Parsing error: ${e.message}")
                }
            }
        })
    }
}