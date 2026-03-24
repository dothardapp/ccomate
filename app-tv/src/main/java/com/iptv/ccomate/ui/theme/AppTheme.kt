package com.iptv.ccomate.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paleta de colores centralizada para toda la aplicación.
 * Facilita cambios globales de tema y mantiene consistencia visual.
 */
object AppColors {
    // Grises
    val lightGray = Color(0xFFF5F5F5)  // Gris claro (casi blanco)
    val gray1 = Color(0xFFD3D3D3)      // Gris claro
    val gray2 = Color(0xFFB0B0B0)      // Gris claro medio
    val gray3 = Color(0xFF808080)      // Gris medio
    val gray4 = Color(0xFF696969)      // Gris oscuro
    val gray5 = Color(0xFF4A4A4A)      // Gris muy oscuro
    val darkGray = Color(0xFF2F4F4F)   // Gris oscuro (casi negro)

    // Fondos (TV-safe: evitar negro puro #000000)
    val background = Color(0xFF121212)
    val backgroundSecondary = darkGray

    // Textos (TV-safe: evitar blanco puro #FFFFFF)
    val textPrimary = Color(0xFFF5F5F5)
    val textSecondary = Color(0xFFF5F5F5).copy(alpha = 0.7f)
    val textTertiary = Color(0xFFF5F5F5).copy(alpha = 0.5f)

    // Estado
    val selected = Color(0xFFF5F5F5)
    val unselected = Color(0xFFF5F5F5).copy(alpha = 0.7f)

    // Acentos
    val accentBlue = Color(0xFF2196F3)
    val accentBlueFocused = Color(0xFF42A5F5)
    val accentBlueBorder = Color(0xFF64B5F6)

    // Overlays
    val overlayDark = Color(0xFF000000)
    val overlayDarker = Color(0xFF121212)
    val overlayPanel = Color(0xAB030301)

    // Texto específico del reproductor
    val textDescription = Color(0xFFF5F5F5).copy(alpha = 0.8f)
}

/**
 * Gradientes reutilizables en toda la aplicación.
 * Evita duplicación de código y facilita cambios globales.
 */
object AppGradients {
    /**
     * Gradiente vertical gris usado en HomeScreen y Drawer.
     * Va de gris claro a gris oscuro (casi negro).
     */
    val verticalGrayGradient: Brush = Brush.verticalGradient(
            colors = listOf(
                AppColors.lightGray,
                AppColors.gray1,
                AppColors.gray2,
                AppColors.gray3,
                AppColors.gray4,
                AppColors.gray5,
                AppColors.darkGray
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
}
