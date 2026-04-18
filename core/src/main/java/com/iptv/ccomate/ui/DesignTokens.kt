package com.iptv.ccomate.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DesignTokens {
    object Colors {
        // Base / Backgrounds (Cinema Dark)
        val bgBase = Color(0xFF0E1116)
        val bgElevated = Color(0xFF161B22)
        val bgHighlight = Color(0xFF21262D)

        // Brand / Accents
        val accent = Color(0xFF4F8EF7)
        val accentGlow = Color(0x664F8EF7)

        // Status Semantics
        val success = Color(0xFF3FB950)
        val warning = Color(0xFFD29922)
        val error = Color(0xFFF85149)

        // Texts (TV Safe)
        val textPrimary = Color(0xFFF5F5F5)
        val textSecondary = Color(0xFF8B949E)
        val textTertiary = Color(0xFF8B949E).copy(alpha = 0.7f)

        // Overlays & Dividers
        val overlayDark = bgBase.copy(alpha = 0.8f)      
        val divider = Color(0xFF30363D)
    }

    object Elevation {
        val none: Dp = 0.dp
        val low: Dp = 2.dp
        val medium: Dp = 4.dp
        val high: Dp = 8.dp
    }

    object Radius {
        val sm: Dp = 8.dp
        val md: Dp = 12.dp
        val lg: Dp = 16.dp
        val xl: Dp = 24.dp
    }

    object Motion {
        const val fast = 150
        const val normal = 250
        const val slow = 400
    }
}
