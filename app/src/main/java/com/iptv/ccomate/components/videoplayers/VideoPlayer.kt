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

    // Obtener el ViewModel
    val viewModel: VideoPlayerViewModel = viewModel()

    // Estado para el ExoPlayer
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Actualizar el ExoPlayer cuando cambie la URL
    LaunchedEffect(videoUrl) {
        viewModel.setPlayer(context, videoUrl).onSuccess { player ->
            exoPlayer = player
        }.onFailure { error ->
            onPlaybackError?.invoke(error)
        }
    }

    // Manejar el ciclo de vida y eventos del ExoPlayer
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player.playWhenReady = false
                    player.pause()
                    Log.d("VideoPlayerWithoutSSL02", "Pausado en ON_PAUSE")
                }
                Lifecycle.Event.ON_START -> {
                    player.playWhenReady = true
                    Log.d("VideoPlayerWithoutSSL02", "Reanudado en ON_START")
                }
                Lifecycle.Event.ON_STOP -> {
                    player.playWhenReady = false
                    player.pause()
                    Log.d("VideoPlayerWithoutSSL02", "Pausado en ON_STOP")
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("VideoPlayerWithoutSSL02", "ON_DESTROY - No se detiene el ExoPlayer, lo hace el ViewModel")
                    // No detenemos ni liberamos aquí, el ViewModel se encarga de eso
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        showOverlay = false
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        showOverlay = true
                        onPlaybackStarted?.invoke()
                    }
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        isBuffering = false
                        showOverlay = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                showOverlay = false
                Log.e("VideoPlayerWithoutSSL02", "Error de reproducción: ${error.message}", error)
                onPlaybackError?.invoke(error)
            }
        }

        player.addListener(listener)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.removeListener(listener)
            Log.d("VideoPlayerWithoutSSL02", "onDispose - No se detiene el ExoPlayer, lo hace el ViewModel")
            // No llamamos a stop() ni clearMediaItems() aquí, el ViewModel se encarga de liberar el ExoPlayer
        }
    }

    // Ocultar overlay luego de 3 segundos
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000)
            showOverlay = false
        }
    }

    // UI del reproductor
    Box(modifier = modifier) {
        exoPlayer?.let { player ->
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        this.player = player
                        useController = false
                        keepScreenOn = true
                    }
                },
                update = { it.player = player },
                modifier = Modifier.fillMaxSize()
            )
        }

        AnimatedVisibility(
            visible = isBuffering,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
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