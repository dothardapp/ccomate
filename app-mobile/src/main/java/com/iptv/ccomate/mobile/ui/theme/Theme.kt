package com.iptv.ccomate.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MobileColors.primary,
    onPrimary = MobileColors.onPrimary,
    secondary = MobileColors.primaryVariant,
    background = MobileColors.background,
    surface = MobileColors.surface,
    surfaceVariant = MobileColors.surfaceVariant,
    onBackground = MobileColors.textPrimary,
    onSurface = MobileColors.textPrimary,
    error = MobileColors.error
)

@Composable
fun CCOMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
