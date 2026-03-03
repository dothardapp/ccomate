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

    // Fondos
    val background = Color.Black
    val backgroundSecondary = darkGray

    // Textos
    val textPrimary = Color.White
    val textSecondary = Color.White.copy(alpha = 0.7f)
    val textTertiary = Color.White.copy(alpha = 0.5f)

    // Estado
    val selected = Color.White
    val unselected = Color.White.copy(alpha = 0.7f)
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
    val verticalGrayGradient: Brush
        get() = Brush.verticalGradient(
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
        fontSize = 12.sp,
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

    // Drawer específico
    val drawerItemSpacing: Dp = 10.dp
    val drawerLogoSpacing: Dp = 12.dp
}
