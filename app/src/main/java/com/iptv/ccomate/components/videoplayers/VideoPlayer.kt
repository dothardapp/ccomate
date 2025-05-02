package com.iptv.ccomate.components.videoplayers

import android.content.Context
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import com.iptv.ccomate.model.VideoPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    context: Context,
    videoUrl: String,
    modifier: Modifier = Modifier,
    channelName: String? = null,
    onPlaybackStarted: (() -> Unit)? = null,
    onPlaybackError: ((Throwable) -> Unit)? = null
) {
    var isBuffering by remember { mutableStateOf(true) }
    var showOverlay by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel: VideoPlayerViewModel = viewModel()
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    Log.d("VideoPlayer", "Rendering VideoPlayer for URL: $videoUrl")

    // Actualizar el ExoPlayer cuando cambie la URL
    LaunchedEffect(videoUrl) {
        Log.d("VideoPlayer", "LaunchedEffect triggered for URL: $videoUrl")
        isBuffering = true
        showOverlay = false

        // Garantizar que la rueda de carga se muestre por al menos 300ms
        delay(300)

        viewModel.setPlayer(context, videoUrl).onSuccess { player ->
            Log.d("VideoPlayer", "ExoPlayer successfully set: $player")
            exoPlayer = player
        }.onFailure { error ->
            Log.e("VideoPlayer", "Failed to set ExoPlayer", error)
            isBuffering = false
            showOverlay = false
            onPlaybackError?.invoke(error)
        }
    }

    // Log para verificar si exoPlayer cambió
    LaunchedEffect(exoPlayer) {
        if (exoPlayer != null) {
            Log.d("VideoPlayer", "exoPlayer updated: $exoPlayer")
        } else {
            Log.d("VideoPlayer", "exoPlayer is null")
        }
    }

    // Manejar el ciclo de vida usando el ViewModel
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.pausePlayer()
                }
                Lifecycle.Event.ON_START -> {
                    viewModel.resumePlayer()
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.stopPlayer()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("VideoPlayer", "ON_DESTROY - No se detiene el ExoPlayer, lo hace el ViewModel")
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d("VideoPlayer", "DisposableEffect onDispose called")
        }
    }

    // Listener para los estados del ExoPlayer
    DisposableEffect(exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                Log.d("VideoPlayer", "Playback state changed: $state")
                when (state) {
                    Player.STATE_BUFFERING -> {
                        Log.d("VideoPlayer", "Buffering started")
                        isBuffering = true
                        showOverlay = false
                    }
                    Player.STATE_READY -> {
                        Log.d("VideoPlayer", "Player ready")
                        isBuffering = false
                        showOverlay = true
                        onPlaybackStarted?.invoke()
                    }
                    Player.STATE_ENDED -> {
                        Log.d("VideoPlayer", "Playback ended")
                        isBuffering = false
                        showOverlay = false
                    }
                    Player.STATE_IDLE -> {
                        Log.d("VideoPlayer", "Player idle")
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoPlayer", "Error de reproducción: ${error.message}", error)
                isBuffering = false
                showOverlay = false
                onPlaybackError?.invoke(error)
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            Log.d("VideoPlayer", "Player listener removed")
        }
    }

    // Ocultar overlay después de 3 segundos
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000)
            showOverlay = false
        }
    }

    // UI del reproductor
    Box(modifier = modifier) {
        // Mostrar el PlayerView solo si exoPlayer no es nulo
        exoPlayer?.let { player ->
            Log.d("VideoPlayer", "Rendering PlayerView for player: $player")
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        this.player = player
                        useController = false
                        keepScreenOn = true
                    }
                },
                update = { view ->
                    view.player = player
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        AnimatedVisibility(
            visible = isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Log.d("VideoPlayer", "Showing buffering indicator")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }
        }

        AnimatedVisibility(
            visible = showOverlay && !channelName.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Log.d("VideoPlayer", "Showing channel overlay: $channelName")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = channelName ?: "",
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}