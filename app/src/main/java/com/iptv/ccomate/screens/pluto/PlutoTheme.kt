package com.iptv.ccomate.screens.pluto

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PlutoTvColors = darkColorScheme(
    primary = Color(0xFF40C4FF), // Azul más suave
    primaryContainer = Color(0xFF004D99), // Azul oscuro para contenedores
    secondary = Color(0xFF00D68F), // Verde brillante
    secondaryContainer = Color(0xFF006644), // Verde oscuro para contenedores
    background = Color(0xFF0A0F12), // Fondo oscuro
    surface = Color(0xFF1A1A1A), // Superficie gris oscuro
    error = Color(0xFFB71C1C), // Rojo para errores
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun PlutoTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlutoTvColors,
        typography = Typography(
            titleLarge = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            ),
            titleMedium = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            ),
            bodyLarge = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            ),
            labelSmall = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            )
        ),
        content = content
    )
}