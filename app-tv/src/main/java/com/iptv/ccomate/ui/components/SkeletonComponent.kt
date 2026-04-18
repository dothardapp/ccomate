package com.iptv.ccomate.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.iptv.ccomate.core.ui.DesignTokens

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DesignTokens.Radius.sm)
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            DesignTokens.Colors.bgElevated,
            DesignTokens.Colors.bgHighlight.copy(alpha = 0.8f),
            DesignTokens.Colors.bgElevated
        ),
        start = Offset(translateAnimation - 500f, translateAnimation - 500f),
        end = Offset(translateAnimation, translateAnimation)
    )

    Box(modifier = modifier.background(brush, shape))
}

/**
 * Componente Skeleton para GroupList
 */
@Composable
fun GroupSkeletonItem() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(DesignTokens.Radius.sm))
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
            .height(65.dp)
            .clip(RoundedCornerShape(DesignTokens.Radius.sm))
            .background(DesignTokens.Colors.bgSurface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .size(80.dp, 45.dp)
                    .clip(RoundedCornerShape(DesignTokens.Radius.sm)),
                shape = RoundedCornerShape(DesignTokens.Radius.sm)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(DesignTokens.Radius.sm)),
                shape = RoundedCornerShape(DesignTokens.Radius.sm)
            )
        }
    }
}

