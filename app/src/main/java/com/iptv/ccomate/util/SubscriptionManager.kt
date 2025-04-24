package com.iptv.ccomate.util


import okhttp3.*
import java.io.IOException

object SubscriptionManager {
    private const val SERVER_URL = "https://your-server.com/api/register-device"
    private const val CHECK_SUBSCRIPTION_URL = "https://your-server.com/api/check-subscription"
    private val client = OkHttpClient()

    fun registerDevice(deviceInfo: DeviceInfo, callback: (Boolean, String?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("installationId", deviceInfo.installationId)
            .add("localIp", deviceInfo.localIp ?: "unknown")
            .add("deviceModel", deviceInfo.deviceModel)
            .build()

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, response.message)
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
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    // Suponiendo que el servidor devuelve un JSON como {"subscribed": true}
                    val isSubscribed = body?.contains("\"subscribed\":true") == true
                    callback(isSubscribed, null)
                } else {
                    callback(false, response.message)
                }
            }
        })
    }
}