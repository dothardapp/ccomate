package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.ui.graphics.Brush
import com.iptv.ccomate.ui.DesignTokens

/**
 * Paleta de colores centralizada para la pantalla PlutoTV.
 * Consumida directamente desde DesignTokens en :core
 */
object PlutoColors {

    // ── Fondos principales ──
    val ScreenGradient = Brush.verticalGradient(
        listOf(DesignTokens.Colors.bgBase, DesignTokens.Colors.bgElevated)
    )

    val VideoContainerGradient = Brush.verticalGradient(
        listOf(DesignTokens.Colors.bgElevated, DesignTokens.Colors.bgHighlight)
    )

    val PanelGradient = Brush.verticalGradient(
        listOf(DesignTokens.Colors.bgElevated, DesignTokens.Colors.bgHighlight)
    )

    val InfoPanelBackground = DesignTokens.Colors.bgElevated.copy(alpha = 0.8f)

    // ── Bordes ──
    val PanelBorder = DesignTokens.Colors.divider
    val DividerColor = DesignTokens.Colors.divider

    // ── Textos ──
    val TextPrimary = DesignTokens.Colors.textPrimary
    val TextSecondary = DesignTokens.Colors.textSecondary
    val TextSubtle = DesignTokens.Colors.textTertiary
    val TextError = DesignTokens.Colors.error

    // ── Banner de advertencia (reloj) ──
    val WarningBannerBackground = DesignTokens.Colors.warning.copy(alpha = 0.9f)
    val WarningBannerBorder = DesignTokens.Colors.warning

    // ── Fullscreen ──
    val FullscreenBackground = DesignTokens.Colors.bgBase

    // ── Estado de reproducción ──
    val StatusLive = DesignTokens.Colors.success
    val StatusBuffering = DesignTokens.Colors.warning

    // ── Barra de progreso EPG ──
    val ProgressTrack = DesignTokens.Colors.bgHighlight

    // ── Divisor de secciones del panel ──
    val DividerPanel = DesignTokens.Colors.divider.copy(alpha = 0.4f)
}
