package com.iptv.ccomate.mobile.ui.theme

import androidx.compose.ui.graphics.Color
import com.iptv.ccomate.core.ui.DesignTokens

/**
 * Paleta mobile — wrapper de [DesignTokens.Colors].
 *
 * Consume los tokens centralizados del sistema "cinema dark" en `:core`.
 * Los nombres de propiedades se mantienen para compatibilidad con call-sites
 * existentes en la app mobile.
 *
 * @see DesignTokens.Colors
 */
object MobileColors {
    val background     = DesignTokens.Colors.bgBase
    val surface        = DesignTokens.Colors.bgElevated
    val surfaceVariant = DesignTokens.Colors.bgHighlight
    val primary        = DesignTokens.Colors.accent
    val primaryVariant = DesignTokens.Colors.accentSoft
    val onPrimary      = Color(0xFF000000)
    val textPrimary    = DesignTokens.Colors.textPrimary
    val textSecondary  = DesignTokens.Colors.textSecondary
    val error          = DesignTokens.Colors.error
    val divider        = DesignTokens.Colors.divider

    // ── Mobile-specific ──
    val selectedItem      = DesignTokens.Colors.accent.copy(alpha = 0.15f)
    val playerBackground  = DesignTokens.Colors.bgBase
}

