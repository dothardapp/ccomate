package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores centralizada para la pantalla PlutoTV.
 * Evita colores hardcodeados dispersos y facilita el mantenimiento del tema visual.
 */
object PlutoColors {

    // ── Fondos principales ──
    val ScreenGradient = Brush.verticalGradient(
        listOf(Color(0xFFD3D3D3), Color(0xFF808080), Color(0xFF4A4A4A))
    )

    val VideoContainerGradient = Brush.verticalGradient(
        listOf(Color(0xFF696969), Color(0xFF2F4F4F))
    )

    val PanelGradient = Brush.verticalGradient(
        listOf(Color(0xFF696969), Color(0xFF4A4A4A))
    )

    val InfoPanelBackground = Color(0xCC696969)

    // ── Bordes ──
    val PanelBorder = Color(0xFFB0B0B0)
    val DividerColor = Color(0xFFB0B0B0)

    // ── Textos ──
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFCFD8DC)
    val TextSubtle = Color(0xFFB0B0B0)
    val TextError = Color(0xFFFF5252)

    // ── Banner de advertencia (reloj) ──
    val WarningBannerBackground = Color(0xE6FF6F00)
    val WarningBannerBorder = Color(0xFFF5F5F5)

    // ── Fullscreen ──
    val FullscreenBackground = Color.Black
}
