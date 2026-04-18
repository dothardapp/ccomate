package com.iptv.ccomate.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.iptv.ccomate.ui.DesignTokens

/**
 * Paleta de colores centralizada para toda la aplicación.
 * Facilita cambios globales de tema y mantiene consistencia visual.
 */
object AppColors {
    // Fondos
    val background = DesignTokens.Colors.bgBase
    val backgroundSecondary = DesignTokens.Colors.bgElevated

    // Textos
    val textPrimary = DesignTokens.Colors.textPrimary
    val textSecondary = DesignTokens.Colors.textSecondary
    val textTertiary = DesignTokens.Colors.textTertiary

    // Estado
    val selected = DesignTokens.Colors.textPrimary
    val unselected = DesignTokens.Colors.textSecondary

    // Acentos
    val accentBlue = DesignTokens.Colors.accent
    val accentBlueFocused = DesignTokens.Colors.accent
    val accentBlueBorder = DesignTokens.Colors.accentGlow

    // Drawer
    val drawerSelectionIndicator = DesignTokens.Colors.accent
    val drawerItemFocused = DesignTokens.Colors.bgHighlight

    // Overlays
    val overlayDark = DesignTokens.Colors.overlayDark
    val overlayDarker = DesignTokens.Colors.bgBase
    val overlayPanel = DesignTokens.Colors.bgElevated.copy(alpha = 0.9f)

    // Texto específico del reproductor
    val textDescription = DesignTokens.Colors.textSecondary
}

/**
 * Gradientes reutilizables en toda la aplicación.
 * Evita duplicación de código y facilita cambios globales.
 */
object AppGradients {
    /**
     * Gradiente vertical sutil charcoal.
     */
    val verticalGrayGradient: Brush = Brush.verticalGradient(
            colors = listOf(
                DesignTokens.Colors.bgBase,
                DesignTokens.Colors.bgElevated
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
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
        fontWeight = FontWeight.Normal,
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
 * Mantiene consistencia visual y facilita mantenimiento.
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
    val drawerSelectionIndicatorWidth: Dp = 3.dp
    val drawerSelectionIndicatorHeight: Dp = 24.dp
}
