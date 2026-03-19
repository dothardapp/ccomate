package com.iptv.ccomate.mobile.ui.video

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.iptv.ccomate.viewmodel.VideoPlayerViewModel

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    channelName: String? = null,
    onPlaybackStarted: (() -> Unit)? = null,
    onPlaybackError: ((Throwable) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context.findActivity()
        ?: throw IllegalStateException("VideoPlayer debe ser alojado en un ComponentActivity")
    val viewModel: VideoPlayerViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(videoUrl) {
        Log.d("VideoPlayer", "URL changed: $videoUrl")
        viewModel.playUrl(videoUrl)
    }

    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) onPlaybackStarted?.invoke()
    }

    LaunchedEffect(playerState.hasError, playerState.errorMessage) {
        if (playerState.hasError && playerState.errorMessage.isNotEmpty()) {
            onPlaybackError?.invoke(Exception(playerState.errorMessage))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pausePlayer()
                Lifecycle.Event.ON_START -> viewModel.resumePlayer()
                Lifecycle.Event.ON_STOP -> viewModel.stopPlayer()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.playerState.value.currentUrl == videoUrl) {
                viewModel.releasePlayer()
            }
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        playerState.player?.let { player ->
            PlayerSurface(player = player)
        }

        if (playerState.isBuffering && !playerState.hasError) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        if (playerState.hasError) {
            Text(
                text = "Error: ${playerState.errorMessage}",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PlayerSurface(player: ExoPlayer) {
    AndroidView(
        factory = {
            PlayerView(it).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                this.player = player
                useController = false
                keepScreenOn = true
            }
        },
        update = { view -> view.player = player },
        modifier = Modifier.fillMaxSize()
    )
}

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
