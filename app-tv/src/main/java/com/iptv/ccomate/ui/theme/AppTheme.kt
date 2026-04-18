package com.iptv.ccomate.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paleta "cinema dark" — charcoal + blue accent, TV-safe.
 * Los tokens semánticos (background, textPrimary, accentBlue, etc.) se mantienen
 * para no romper call-sites, pero sus valores se actualizaron a la nueva paleta.
 */
object AppColors {
    // ── Base dark (antes gris claro/medio) ──
    val bgBase = Color(0xFF0E1116)        // casi negro TV-safe — fondo general
    val bgElevated = Color(0xFF161B22)    // superficies: panels, cards
    val bgHighlight = Color(0xFF21262D)   // focus/hover bg, items destacados
    val bgSurface = Color(0xFF1C2128)     // surface intermedio

    // ── Accent principal ──
    val accent = Color(0xFF4F8EF7)        // azul saturado — color primario de marca
    val accentBright = Color(0xFF6BA3FF)  // variante clara para focused/hover
    val accentSoft = Color(0xFF2D5FBA)    // variante oscura para borders sutiles
    val accentGlow = Color(0x664F8EF7)    // mismo azul 40% — para halos/shadows

    // ── Estados semánticos ──
    val success = Color(0xFF3FB950)       // en vivo, OK
    val warning = Color(0xFFD29922)       // buffering, advertencia
    val error = Color(0xFFF85149)         // error

    // ── Texto TV-safe (sin blanco puro) ──
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFFB1BAC4)
    val textTertiary = Color(0xFF8B949E)
    val textSubtle = Color(0xFF6E7681)
    val textDescription = Color(0xFFE6EDF3).copy(alpha = 0.8f)

    // ── Estado de selección (drawer, listas) ──
    val selected = textPrimary
    val unselected = textSecondary

    // ── Overlays ──
    val overlayDark = Color(0xCC0E1116)   // oscuro traslúcido sobre video
    val overlayDarker = Color(0xE60E1116) // más denso para errores
    val overlayPanel = Color(0xAB0E1116)  // panel sobre video

    // ── Divisores ──
    val divider = Color(0xFF30363D)
    val dividerSoft = Color(0x4D30363D)

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

    // Radius (UI-03)
    val radiusSm: Dp = 8.dp
    val radiusMd: Dp = 12.dp
    val radiusLg: Dp = 16.dp
    val radiusXl: Dp = 24.dp
}
