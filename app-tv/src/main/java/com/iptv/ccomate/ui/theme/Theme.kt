package com.iptv.ccomate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@Composable
fun CCOMateTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = AppColors.textPrimary,
        secondary = AppColors.gray2,
        background = AppColors.background,
        surface = AppColors.gray5,
        error = Color(0xFFFF5252)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
