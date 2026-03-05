package com.iptv.ccomate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Modificador global para el efecto Shimmer.
 */
fun Modifier.shimmerBackground(
    shape: Shape = RoundedCornerShape(6.dp)
): Modifier = composed {
    val shimmerColors = listOf(
        Color(0xFF2C2C2C), // Gris oscuro TV-Safe
        Color(0xFF424242), // Gris un poco más claro
        Color(0xFF2C2C2C)
    )

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    this.background(brush = brush, shape = shape)
}

/**
 * Componente Skeleton para GroupList
 */
@Composable
fun GroupSkeletonItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .shimmerBackground()
    )
}

/**
 * Componente Skeleton para ChannelList
 */
@Composable
fun ChannelSkeletonItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .height(65.dp) // Altura promedio de un item de canal con imagen
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1C1C1C)) // Fondo base
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // Simulando el logo del canal
            Box(
                modifier = Modifier
                    .size(80.dp, 45.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerBackground()
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Simulando el texto del canal
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerBackground()
            )
        }
    }
}
