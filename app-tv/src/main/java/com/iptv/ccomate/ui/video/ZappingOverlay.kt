package com.iptv.ccomate.ui.video

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iptv.ccomate.core.ui.DesignTokens
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.model.EPGProgram

/**
 * Overlay de "zapping" que muestra info del canal al cambiar con D-Pad en fullscreen.
 *
 * ┌──────────────┐
 * │ [Logo]       │ ← desliza desde la derecha
 * │ ESPN HD      │   fade in 300ms
 * │ ▸ Programa   │   auto-hide controlado por el padre
 * └──────────────┘
 *
 * @param channel Canal a mostrar. Si es null, el overlay desaparece con fade out.
 * @param currentProgram Programa EPG actual (opcional).
 *
 * @see <a href="https://github.com/dothardapp/ccomate/issues/86">Issue #86</a>
 */
@Composable
fun ZappingOverlay(
    channel: Channel?,
    currentProgram: EPGProgram?,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = channel,
        transitionSpec = {
            (slideInHorizontally(
                animationSpec = tween(DesignTokens.Motion.normal),
                initialOffsetX = { fullWidth -> fullWidth }
            ) + fadeIn(tween(DesignTokens.Motion.normal)))
                .togetherWith(fadeOut(tween(DesignTokens.Motion.fast)))
        },
        label = "zapping_overlay",
        modifier = modifier
    ) { ch ->
        if (ch != null) {
            ZappingCard(
                channel = ch,
                currentProgram = currentProgram
            )
        }
    }
}

/**
 * Tarjeta con info del canal para el overlay de zapping.
 */
@Composable
private fun ZappingCard(
    channel: Channel,
    currentProgram: EPGProgram?,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(DesignTokens.Radius.md)

    Box(
        modifier = modifier
            .padding(24.dp)
            .width(280.dp)
            .clip(cardShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DesignTokens.Colors.bgElevated.copy(alpha = 0.95f),
                        DesignTokens.Colors.bgSurface.copy(alpha = 0.9f)
                    )
                )
            )
            .border(1.dp, DesignTokens.Colors.dividerSoft, cardShape)
            .padding(16.dp)
    ) {
        Column {
            // Logo del canal
            val context = LocalContext.current
            if (!channel.logo.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(channel.logo)
                        .size(160, 96)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Logo ${channel.name}",
                    modifier = Modifier
                        .size(64.dp, 36.dp)
                        .clip(RoundedCornerShape(DesignTokens.Radius.sm))
                )
                Spacer(Modifier.height(8.dp))
            }

            // Nombre del canal
            Text(
                text = channel.name,
                color = DesignTokens.Colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Programa actual (si disponible)
            currentProgram?.let { prog ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "▸ ${prog.title}",
                    color = DesignTokens.Colors.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
