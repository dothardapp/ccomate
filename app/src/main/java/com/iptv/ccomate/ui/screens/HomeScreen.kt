package com.iptv.ccomate.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun HomeScreen() {
    // Animación del brillo (posición del gradiente que se mueve de izquierda a derecha)
    val shineOffset by rememberInfiniteTransition().animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Animación de movimiento vertical (desplazamiento suave)
    val offsetY by rememberInfiniteTransition().animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación de movimiento horizontal (desplazamiento suave)
    val offsetX by rememberInfiniteTransition().animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animación de rotación sutil
    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Lista de partículas con animaciones individuales
    val particleCount = 30
    val particleOffsets = List(particleCount) { index ->
        rememberInfiniteTransition().animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(24000 + (index * 100), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Lista de posiciones iniciales para las partículas (calculadas una sola vez)
    val particlePositions = remember {
        List(particleCount) { Offset((0..1000).random().toFloat(), (0..1000).random().toFloat()) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(
                colors = listOf(Color(0xFF87DEA2), Color(0xFF0C1B36), Color(0xFF1C2526)),
                center = Offset.Infinite,
                radius = Float.POSITIVE_INFINITY
            ))
            // Efecto de partículas animadas
            .drawBehind {
                particleOffsets.forEachIndexed { index, offset ->
                    val basePosition = particlePositions[index]
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = 2f,
                        center = Offset(
                            x = basePosition.x + offset.value,
                            y = basePosition.y + offset.value
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(2000)) +
                    scaleIn(initialScale = 0.7f, animationSpec = tween(2000)) +
                    slideInVertically(animationSpec = tween(2000)) { it },
            modifier = Modifier
                .sizeIn(maxWidth = 640.dp, maxHeight = 480.dp)
                .offset(x = offsetX.dp, y = offsetY.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
                .shadow(3.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFFFCFCDF), Color(0xFFE5E5AF))),
                    shape = RoundedCornerShape(28.dp)
                )
                .background(Color.Black)
                // Fondo con resplandor
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8D0326), Color(0xFF0000AA)),
                        center = Offset.Infinite,
                        radius = Float.POSITIVE_INFINITY
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                // Efecto de brillo
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color(0xFF9D0B7F), Color.Transparent),
                            start = Offset(shineOffset * size.width, 0f),
                            end = Offset((shineOffset + 0.5f) * size.width, size.height)
                        )
                    )
                }
        ) {
            AsyncImage(
                model = "http://10.224.24.232:8081/iptvbanner.jpg",
                contentDescription = "IPTV Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit // Mantiene la relación de aspecto
            )
        }
    }
}