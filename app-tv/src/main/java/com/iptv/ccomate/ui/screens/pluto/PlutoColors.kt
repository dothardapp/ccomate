package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.iptv.ccomate.core.ui.DesignTokens

/**
 * Paleta scoped para las pantallas Pluto/TDA.
 *
 * Los tokens se delegan directamente a [DesignTokens.Colors] (`:core`).
 * Solo se mantienen los gradientes específicos de estas pantallas
 * y el alias semántico `WarningBannerBackground` (con opacidad custom).
 *
 * @see DesignTokens.Colors
 */
object PlutoColors {

    // ── Fondos principales (gradientes scoped) ──
    val ScreenGradient = Brush.verticalGradient(
        listOf(DesignTokens.Colors.bgBase, DesignTokens.Colors.bgElevated)
    )

    val VideoContainerGradient = Brush.verticalGradient(
        listOf(DesignTokens.Colors.bgElevated, DesignTokens.Colors.bgBase)
    )

    val PanelGradient = Brush.verticalGradient(
        listOf(DesignTokens.Colors.bgElevated, DesignTokens.Colors.bgSurface)
    )

    val InfoPanelBackground = DesignTokens.Colors.bgElevated.copy(alpha = 0.92f)

    // ── Bordes ──
    val PanelBorder  = DesignTokens.Colors.divider
    val DividerColor = DesignTokens.Colors.dividerSoft

    // ── Textos ──
    val TextPrimary   = DesignTokens.Colors.textPrimary
    val TextSecondary = DesignTokens.Colors.textSecondary
    val TextSubtle    = DesignTokens.Colors.textSubtle
    val TextError     = DesignTokens.Colors.error

    // ── Banner de advertencia (reloj) — scoped: opacidad custom ──
    val WarningBannerBackground = Color(0xE6D29922)
    val WarningBannerBorder     = DesignTokens.Colors.warning

    // ── Fullscreen ──
    val FullscreenBackground = DesignTokens.Colors.bgBase

    // ── Estado de reproducción ──
    val StatusLive      = DesignTokens.Colors.success
    val StatusBuffering = DesignTokens.Colors.warning

    // ── Barra de progreso EPG ──
    val ProgressTrack = DesignTokens.Colors.bgHighlight

    // ── Divisor de secciones del panel ──
    val DividerPanel = DesignTokens.Colors.dividerSoft
}
