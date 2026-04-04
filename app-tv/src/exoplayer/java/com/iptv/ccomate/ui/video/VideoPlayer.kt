package com.iptv.ccomate.ui.video

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    channelName: String? = null,
    onPlaybackStarted: (() -> Unit)? = null,
    onPlaybackError: ((Throwable) -> Unit)? = null,
    onErrorStateChanged: ((Boolean) -> Unit)? = null,
    currentProgram: EPGProgram? = null,
    isFullscreen: Boolean = false
) {
    val context = LocalContext.current
    val activity = context.findActivity()
        ?: throw IllegalStateException("VideoPlayer debe ser alojado en un ComponentActivity")
    val viewModel: VideoPlayerViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    // P1.3: player en StateFlow propio — PlayerSurface no recompone ante cambios de
    // isBuffering/isPlaying/hasError, solo cuando la referencia al player cambia.
    val player by viewModel.player.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Overlay auto-hide
    var showOverlay by remember { mutableStateOf(false) }

    // Trigger playUrl when videoUrl changes
    LaunchedEffect(videoUrl) {
        Log.d("VideoPlayer", "URL changed: $videoUrl")
        viewModel.playUrl(videoUrl)
    }

    // Notify parent about error state changes
    LaunchedEffect(playerState.hasError) {
        onErrorStateChanged?.invoke(playerState.hasError)
    }

    // Notify parent about playback started
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            showOverlay = true
            onPlaybackStarted?.invoke()
        }
    }

    // Notify parent about errors
    LaunchedEffect(playerState.hasError, playerState.errorMessage) {
        if (playerState.hasError && playerState.errorMessage.isNotEmpty()) {
            onPlaybackError?.invoke(Exception(playerState.errorMessage))
        }
    }

    // Auto-hide overlay after 3 seconds
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000)
            showOverlay = false
        }
    }

    // Show overlay when entering fullscreen
    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            showOverlay = true
        }
    }

    // Lifecycle management
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Release player when this Composable leaves composition,
    // pero SOLO si la URL actual del ViewModel coincide con la nuestra.
    // Esto evita que la pantalla saliente (TDA) destruya el player
    // que la pantalla entrante (Pluto) acaba de crear.
    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.playerState.value.currentUrl == videoUrl) {
                viewModel.releasePlayer()
            }
        }
    }

    // UI
    Box(modifier = modifier) {
        // PlayerView surface — always present, hidden behind error overlay when needed
        player?.let {
            PlayerSurface(player = it)
        }

        // Buffering indicator
        VideoPlayerBuffering(visible = playerState.isBuffering && !playerState.hasError)

        // Error screen with retry
        VideoPlayerError(
            visible = playerState.hasError,
            errorMessage = playerState.errorMessage,
            channelName = channelName,
            onRetry = { viewModel.retry() }
        )

        // Channel/program overlay
        VideoPlayerOverlay(
            visible = showOverlay && !playerState.hasError,
            channelName = channelName,
            currentProgram = currentProgram
        )
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
