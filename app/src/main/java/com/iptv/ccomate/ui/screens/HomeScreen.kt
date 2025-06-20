package com.iptv.ccomate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun HomeScreen() {
    // Configuración de Coil para evitar la caché
    val context = LocalContext.current
    val imageRequest = remember {
        ImageRequest.Builder(context)
            .data("http://10.224.24.232:8081/iptvbanner.png")
            .diskCachePolicy(CachePolicy.DISABLED) // Deshabilita la caché en disco
            .memoryCachePolicy(CachePolicy.DISABLED) // Deshabilita la caché en memoria
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5F5), // Gris claro (casi blanco)
                        Color(0xFFD3D3D3), // Gris claro
                        Color(0xFFB0B0B0), // Gris claro medio
                        Color(0xFF808080), // Gris medio
                        Color(0xFF696969), // Gris oscuro
                        Color(0xFF4A4A4A),  // Gris muy oscuro
                        Color(0xFF2F4F4F)   // Gris oscuro (casi negro)
                    ),
                    startY = 0f,               // (opcional) dónde empieza
                    endY = Float.POSITIVE_INFINITY // (opcional) dónde termina
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(2000)) +
                    scaleIn(initialScale = 0.7f, animationSpec = tween(2000)) +
                    slideInVertically(animationSpec = tween(2000)) { it },
            modifier = Modifier
                .padding(42.dp)
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