package com.iptv.ccomate.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta de colores inspirada en Pluto TV (morados y azules)
private val PlutoTvStaticColors = darkColorScheme(
    primary = Color(0xFF5B3EFF), // Morado vibrante
    primaryContainer = Color(0xFF3A2A99), // Morado más oscuro
    secondary = Color(0xFF00D1FF), // Cian brillante
    secondaryContainer = Color(0xFF0088A3), // Cian oscuro
    background = Color(0xFF0D0D1A), // Fondo azul oscuro
    surface = Color(0xFF1A1A2E), // Superficie gris-azul
    error = Color(0xFFE63946), // Rojo para errores
    onPrimary = Color.Companion.White,
    onSecondary = Color.Companion.White,
    onBackground = Color(0xFFE6E6FA), // Blanco suave para texto
    onSurface = Color(0xFFE6E6FA),
    onError = Color.Companion.White
)

// Tipografía personalizada con más estilos
private val PlutoTvTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Companion.Bold,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Companion.Bold,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Companion.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Companion.Medium,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Companion.Medium,
        lineHeight = 16.sp
    )
)

@Composable
fun PlutoTvThemeAlternative(
    useDynamicColors: Boolean = true,
    customColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(context)
        }
        customColorScheme != null -> customColorScheme
        else -> PlutoTvStaticColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PlutoTvTypography,
        content = content
    )
}