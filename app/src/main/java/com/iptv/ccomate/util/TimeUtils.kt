package com.iptv.ccomate.util

import java.util.*

object TimeUtils {
    fun isSystemTimeValid(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis

        // Ejemplo: alerta si el año es anterior a 2023
        return calendar.get(Calendar.YEAR) >= 2023
    }

    fun getSystemTimeMessage(): String {
        val calendar = Calendar.getInstance()
        return "Hora actual del sistema: ${calendar.time}"
    }
}
