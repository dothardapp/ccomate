package com.iptv.ccomate.ui.video

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
import androidx.compose.ui.unit.sp

@Composable
fun VideoPanel(
        videoUrl: String?,
        channelName: String?,
        onPlaybackStarted: () -> Unit,
        onPlaybackError: (Throwable) -> Unit,
        modifier: Modifier = Modifier,
        onErrorStateChanged: ((Boolean) -> Unit)? = null,
        currentProgram: com.iptv.ccomate.model.EPGProgram? = null,
        isFullscreen: Boolean = false
) {
    Box(modifier = modifier.background(Color(0xAB030301))) {
        // Mostrar la rueda de carga si videoUrl es null (canales aún cargando)
        AnimatedVisibility(visible = videoUrl == null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                    modifier = Modifier.Companion.fillMaxSize().background(Color(0x66000000)),
                    contentAlignment = Alignment.Companion.Center
            ) { CircularProgressIndicator(color = Color(0xFFF5F5F5), strokeWidth = 3.dp) }
        }

        // Renderizar VideoPlayer si videoUrl no es null
        videoUrl?.let {
            VideoPlayer(
                    videoUrl = it,
                    channelName = channelName,
                    modifier =
                            Modifier.Companion.background(Color.Companion.Transparent)
                                    .fillMaxSize()
                                    .padding(1.dp),
                    onPlaybackStarted = onPlaybackStarted,
                    onPlaybackError = onPlaybackError,
                    onErrorStateChanged = onErrorStateChanged,
                    currentProgram = currentProgram,
                    isFullscreen = isFullscreen
            )
        }
        if (ENABLE_EPG_PANEL && currentProgram != null) {
            Box(
                    modifier =
                            Modifier.align(Alignment.TopCenter)
                                    .padding(8.dp)
                                    .background(
                                            Color(0xFF121212).copy(alpha = 0.6f),
                                            androidx.compose.foundation.shape.RoundedCornerShape(
                                                    4.dp
                                            )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                androidx.tv.material3.Text(
                        text = "Ahora: ${currentProgram.title}",
                        color = Color(0xFFF5F5F5),
                        fontSize = 14.sp // Mínimo 14sp para TV
                )
            }
        }
    }
}

private const val ENABLE_EPG_PANEL = false
