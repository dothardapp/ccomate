package com.iptv.ccomate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@Composable
fun CCOMateTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = AppColors.textPrimary,
        secondary = AppColors.textSecondary,
        background = AppColors.background,
        surface = AppColors.backgroundSecondary,
        error = com.iptv.ccomate.ui.DesignTokens.Colors.error
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
