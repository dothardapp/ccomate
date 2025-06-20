package com.iptv.ccomate.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import java.net.NetworkInterface
import java.util.UUID

data class DeviceInfo(
    val installationId: String,
    val localIp: String?,
    val deviceModel: String,
    val dni: String? = null,
    val name: String? = null,
    val phone: String? = null
)

object DeviceIdentifier {
    private const val PREFS_NAME = "DevicePrefs"
    private const val KEY_INSTALLATION_ID = "installation_id"

    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            installationId = getInstallationId(context),
            localIp = getLocalIpAddress(),
            deviceModel = getDeviceModel()
        )
    }

    fun getInstallationId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var installationId = prefs.getString(KEY_INSTALLATION_ID, null)

        if (installationId == null) {
            installationId = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_INSTALLATION_ID, installationId) }
        }

        return installationId
    }

    private fun getLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.asSequence()
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.filter { !it.isLoopbackAddress && it.hostAddress?.contains(':') == false }
                ?.map { it.hostAddress }
                ?.firstOrNull()
        } catch (e: Exception) {
            Log.e("DeviceIdentifier", "Error getting IP address: ${e.message}", e)
            null
        }
    }

    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }
}