package com.iptv.ccomate.ui.video

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VideoPanel(
    context: Context,
    videoUrl: String?,
    channelName: String?,
    onPlaybackStarted: () -> Unit,
    onPlaybackError: (Throwable) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xAB030301))
    ) {
        // Mostrar la rueda de carga si videoUrl es null (canales aún cargando)
        AnimatedVisibility(
            visible = videoUrl == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Companion.Center
            ) {
                CircularProgressIndicator(
                    color = Color.Companion.White,
                    strokeWidth = 3.dp
                )
            }
        }

        // Renderizar VideoPlayer si videoUrl no es null
        videoUrl?.let {
            VideoPlayer(
                context = context,
                videoUrl = it,
                channelName = channelName,
                modifier = Modifier.Companion
                    .background(Color.Companion.Transparent)
                    .fillMaxSize()
                    .padding(1.dp),
                onPlaybackStarted = onPlaybackStarted,
                onPlaybackError = onPlaybackError
            )
        }
    }
}