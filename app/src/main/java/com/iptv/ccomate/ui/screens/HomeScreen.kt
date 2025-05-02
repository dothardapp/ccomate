package com.iptv.ccomate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(
                brush = Brush.Companion.verticalGradient(
                    colors = listOf(Color(0xFF1E1F22), MaterialTheme.colorScheme.background)
                )
            )
            .focusable(true),
        contentAlignment = Alignment.Companion.Center
    )
    {
    }
}