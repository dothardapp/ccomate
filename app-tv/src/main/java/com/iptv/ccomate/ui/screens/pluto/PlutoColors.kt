package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.iptv.ccomate.ui.theme.AppColors

/**
 * Paleta scoped para las pantallas Pluto/TDA.
 * Los tokens se mapean al sistema unificado "cinema dark" (AppColors).
 * Se mantiene el objeto como capa de compatibilidad y para definir
 * gradientes específicos de estas pantallas.
 */
object PlutoColors {

    // ── Fondos principales ──
    val ScreenGradient = Brush.verticalGradient(
        listOf(AppColors.bgBase, AppColors.bgElevated)
    )

    val VideoContainerGradient = Brush.verticalGradient(
        listOf(AppColors.bgElevated, AppColors.bgBase)
    )

    val PanelGradient = Brush.verticalGradient(
        listOf(AppColors.bgElevated, AppColors.bgSurface)
    )

    val InfoPanelBackground = AppColors.bgElevated.copy(alpha = 0.92f)

    // ── Bordes ──
    val PanelBorder = AppColors.divider
    val DividerColor = AppColors.dividerSoft

    // ── Textos ──
    val TextPrimary = AppColors.textPrimary
    val TextSecondary = AppColors.textSecondary
    val TextSubtle = AppColors.textSubtle
    val TextError = AppColors.error

    // ── Banner de advertencia (reloj) ──
    val WarningBannerBackground = Color(0xE6D29922) // warning con opacidad
    val WarningBannerBorder = AppColors.warning

    // ── Fullscreen ──
    val FullscreenBackground = AppColors.bgBase

    // ── Estado de reproducción ──
    val StatusLive = AppColors.success
    val StatusBuffering = AppColors.warning

    // ── Barra de progreso EPG ──
    val ProgressTrack = AppColors.bgHighlight

    // ── Divisor de secciones del panel ──
    val DividerPanel = AppColors.dividerSoft
}
