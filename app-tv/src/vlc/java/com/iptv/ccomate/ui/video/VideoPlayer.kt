package com.iptv.ccomate.ui.video

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay
import org.videolan.libvlc.util.VLCVideoLayout

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
    val lifecycleOwner = LocalLifecycleOwner.current

    var showOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(videoUrl) {
        Log.d("VideoPlayer", "URL changed: $videoUrl")
        viewModel.playUrl(videoUrl)
    }

    LaunchedEffect(playerState.hasError) {
        onErrorStateChanged?.invoke(playerState.hasError)
    }

    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying) {
            showOverlay = true
            onPlaybackStarted?.invoke()
        }
    }

    LaunchedEffect(playerState.hasError, playerState.errorMessage) {
        if (playerState.hasError && playerState.errorMessage.isNotEmpty()) {
            onPlaybackError?.invoke(Exception(playerState.errorMessage))
        }
    }

    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000)
            showOverlay = false
        }
    }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) showOverlay = true
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

    // Liberar player cuando este Composable sale de composicion,
    // pero SOLO si la URL actual del ViewModel coincide con la nuestra.
    // Evita que una pantalla saliente destruya el player de la pantalla entrante.
    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.playerState.value.currentUrl == videoUrl) {
                viewModel.releasePlayer()
            }
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        // VLCVideoLayout — superficie unica reutilizada.
        // El telon opaco de buffering oculta ghost frames al cambiar canal.
        // attachViews con guard de activeVideoLayout maneja tab switching.
        AndroidView(
            factory = { ctx ->
                VLCVideoLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = true
                    viewModel.attachViews(this)
                }
            },
            onRelease = { layout ->
                viewModel.detachViews(layout)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Telon de Carga — fondo opaco sobre el frame congelado de VLC
        VideoPlayerBuffering(visible = playerState.isBuffering && !playerState.hasError)

        // Telon de Error — fondo opaco que bloquea visualmente al VLC congelado
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

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
