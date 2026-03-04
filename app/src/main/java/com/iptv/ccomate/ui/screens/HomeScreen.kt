package com.iptv.ccomate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.iptv.ccomate.ui.theme.AppDimensions
import com.iptv.ccomate.ui.theme.AppGradients
import com.iptv.ccomate.util.AppConfig

@Composable
fun HomeScreen() {
    // Configuración de Coil con Caché Habilitada
    val context = LocalContext.current
    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data(AppConfig.BANNER_URL)
            .diskCachePolicy(CachePolicy.ENABLED) // Habilitado para mejorar rendimiento
            .memoryCachePolicy(CachePolicy.ENABLED) // Habilitado para evitar parpadeos
            .crossfade(true) // Transición suave
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = AppGradients.verticalGrayGradient),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(2000)) +
                    scaleIn(initialScale = 0.7f, animationSpec = tween(2000)) +
                    slideInVertically(animationSpec = tween(2000)) { it },
            modifier = Modifier
                .padding(horizontal = AppDimensions.overscanHorizontal, vertical = AppDimensions.overscanVertical) // Overscan zona segura 5%
                .fillMaxSize()
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "IPTV Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit // Mantiene la relación de aspecto
            )
        }
    }
}
