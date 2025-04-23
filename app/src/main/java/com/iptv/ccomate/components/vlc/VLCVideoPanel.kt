package com.iptv.ccomate.components.vlc

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VLCVideoPanel(
    context: Context,
    videoUrl: String?,
    channelName: String?,
    onPlaybackStarted: () -> Unit,
    onPlaybackError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xAB030301))
    ) {
        videoUrl?.let {
            VLCVideoPlayer(
                context = context,
                videoUrl = it,
                channelName = channelName,
                modifier = Modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
                    .padding(1.dp),
                onPlaybackStarted = onPlaybackStarted,
                onPlaybackError = onPlaybackError
            )
        }
    }
}