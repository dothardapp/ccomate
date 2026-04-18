package com.iptv.ccomate.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptv.ccomate.core.ui.DesignTokens

/**
 * Paleta "cinema dark" para app-tv — wrapper de [DesignTokens.Colors].
 *
 * Todos los valores se delegan al sistema centralizado en `:core`.
 * Los aliases legacy se mantienen para no romper call-sites existentes.
 *
 * @see DesignTokens.Colors
 */
object AppColors {
    // ── Base dark ──
    val bgBase       = DesignTokens.Colors.bgBase
    val bgElevated   = DesignTokens.Colors.bgElevated
    val bgHighlight  = DesignTokens.Colors.bgHighlight
    val bgSurface    = DesignTokens.Colors.bgSurface

    // ── Accent principal ──
    val accent       = DesignTokens.Colors.accent
    val accentBright = DesignTokens.Colors.accentBright
    val accentSoft   = DesignTokens.Colors.accentSoft
    val accentGlow   = DesignTokens.Colors.accentGlow

    // ── Estados semánticos ──
    val success = DesignTokens.Colors.success
    val warning = DesignTokens.Colors.warning
    val error   = DesignTokens.Colors.error

    // ── Texto TV-safe (sin blanco puro) ──
    val textPrimary   = DesignTokens.Colors.textPrimary
    val textSecondary = DesignTokens.Colors.textSecondary
    val textTertiary  = DesignTokens.Colors.textTertiary
    val textSubtle    = DesignTokens.Colors.textSubtle
    val textDescription = DesignTokens.Colors.textPrimary.copy(alpha = 0.8f)

    // ── Estado de selección (drawer, listas) ──
    val selected   = textPrimary
    val unselected = textSecondary

    // ── Overlays ──
    val overlayDark   = DesignTokens.Colors.overlayDark
    val overlayDarker = DesignTokens.Colors.overlayDarker
    val overlayPanel  = DesignTokens.Colors.overlayPanel

    // ── Divisores ──
    val divider     = DesignTokens.Colors.divider
    val dividerSoft = DesignTokens.Colors.dividerSoft

    // ── Aliases de compatibilidad (legacy API) ──
    // Estos nombres existían en la paleta gris anterior; se mapean a la nueva
    // paleta semánticamente equivalente para evitar tocar call-sites.
    val lightGray = textPrimary
    val gray1 = textSecondary
    val gray2 = textTertiary
    val gray3 = divider
    val gray4 = Color(0xFF3D444D)
    val gray5 = bgElevated
    val darkGray = bgBase
    val background = bgBase
    val backgroundSecondary = bgElevated
    val accentBlue = accent
    val accentBlueFocused = accentBright
    val accentBlueBorder = accentSoft
}

/**
 * Gradientes reutilizables en toda la aplicación.
 */
object AppGradients {
    /**
     * Gradiente vertical principal — charcoal sutil (antes gris "arco iris").
     * Se usa como fondo en HomeScreen, Drawer, Settings y ChannelScreen.
     */
    val verticalGrayGradient: Brush = Brush.verticalGradient(
        colors = listOf(
            AppColors.bgBase,
            AppColors.bgElevated
        )
    )

    /** Alias semántico del nuevo nombre. */
    val screenGradient: Brush = verticalGrayGradient

    /** Gradiente para el contenedor del video cuando no hay playback. */
    val videoContainerGradient: Brush = Brush.verticalGradient(
        colors = listOf(
            AppColors.bgElevated,
            AppColors.bgBase
        )
    )

    /** Gradiente vertical para overlay cinemático sobre video. */
    val videoOverlayGradient: Brush = Brush.verticalGradient(
        0.0f to Color.Transparent,
        0.4f to Color.Transparent,
        1.0f to Color(0xCC000000)
    )
}

/**
 * Tipografía centralizada para mantener consistencia.
 */
object AppTypography {
    val drawerLabel = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.textSecondary
    )

    val drawerLabelSelected = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.selected
    )

    val body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = AppColors.textPrimary
    )

    val small = TextStyle(
        fontSize = 14.sp, // Mínimo 14sp para TV (regla tipografía)
        fontWeight = FontWeight.Normal,
        color = AppColors.textSecondary
    )
}

/**
 * Dimensiones y espaciados reutilizables.
 */
object AppDimensions {
    // Espacios
    val spacing_xs: Dp = 4.dp
    val spacing_sm: Dp = 8.dp
    val spacing_md: Dp = 12.dp
    val spacing_lg: Dp = 16.dp
    val spacing_xl: Dp = 24.dp
    val spacing_xxl: Dp = 32.dp

    // Tamaños de iconos
    val iconSmall: Dp = 24.dp
    val iconMedium: Dp = 32.dp
    val iconLarge: Dp = 48.dp

    // Padding común
    val containerPaddingSmall: Dp = 8.dp
    val containerPaddingMedium: Dp = 16.dp
    val containerPaddingLarge: Dp = 24.dp

    // Overscan (zona segura 5% para Android TV)
    val overscanHorizontal: Dp = 48.dp
    val overscanVertical: Dp = 27.dp

    // Drawer específico
    val drawerItemSpacing: Dp = 10.dp
    val drawerLogoSpacing: Dp = 12.dp

    // Radius — delegan a DesignTokens.Radius (UI-03 / UI-10)
    val radiusSm: Dp = DesignTokens.Radius.sm
    val radiusMd: Dp = DesignTokens.Radius.md
    val radiusLg: Dp = DesignTokens.Radius.lg
    val radiusXl: Dp = DesignTokens.Radius.xl
}
