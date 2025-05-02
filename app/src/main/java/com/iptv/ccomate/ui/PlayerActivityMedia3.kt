package com.iptv.ccomate.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.model.VideoPlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerActivityMedia3 : ComponentActivity() {
    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val url = intent.getStringExtra("url") ?: run {
            Log.w("PlayerActivityMedia3", "URL no proporcionada")
            finish()
            return
        }
        val name = intent.getStringExtra("name") ?: "Canal"
        val logo = intent.getStringExtra("logo")

        if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
            Log.w("PlayerActivityMedia3", "URL inválida: $url")
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.setPlayer(this@PlayerActivityMedia3, url).onSuccess {
                setContent { PlayerScreen(viewModel, url, name, logo) }
            }.onFailure { e ->
                Log.e("PlayerActivityMedia3", "Error al inicializar player: ${e.message}")
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch(Dispatchers.Main) { viewModel.pausePlayer() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Main) { viewModel.resumePlayer() }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch(Dispatchers.Main) { viewModel.stopPlayer() }
    }

    override fun onDestroy() {
        super.onDestroy()
        // No es necesario liberar aquí, ya que el ViewModel lo hace en onCleared
    }

    @Composable
    fun PlayerScreen(
        viewModel: VideoPlayerViewModel,
        videoUrl: String,
        channelName: String,
        channelLogo: String?
    ) {
        var isBuffering by remember { mutableStateOf(true) }
        var showOverlay by remember { mutableStateOf(false) }
        var player by remember { mutableStateOf<ExoPlayer?>(null) }

        // Actualizar el player cuando cambie la URL
        LaunchedEffect(videoUrl) {
            viewModel.setPlayer(this@PlayerActivityMedia3, videoUrl).onSuccess { exoPlayer ->
                Log.d("PlayerScreen", "ExoPlayer successfully set: $exoPlayer")
                player = exoPlayer
            }.onFailure { e ->
                Log.e("PlayerScreen", "Error al actualizar player: ${e.message}")
            }
        }

        // Escuchar los eventos del ExoPlayer para actualizar los estados
        DisposableEffect(player) {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    Log.d("PlayerScreen", "Playback state changed: $state")
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            Log.d("PlayerScreen", "Buffering started")
                            isBuffering = true
                            showOverlay = false
                        }
                        Player.STATE_READY -> {
                            Log.d("PlayerScreen", "Player ready")
                            isBuffering = false
                            showOverlay = true
                        }
                        Player.STATE_ENDED -> {
                            Log.d("PlayerScreen", "Playback ended")
                            isBuffering = false
                            showOverlay = false
                        }
                        Player.STATE_IDLE -> {
                            Log.d("PlayerScreen", "Player idle")
                            isBuffering = true
                            showOverlay = false
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("PlayerScreen", "Error de reproducción: ${error.message}", error)
                    isBuffering = false
                    showOverlay = false
                }
            }

            player?.addListener(listener)

            onDispose {
                player?.removeListener(listener)
                Log.d("PlayerScreen", "Player listener removed")
            }
        }

        // Ocultar el overlay después de 5 segundos cuando se muestra
        LaunchedEffect(showOverlay) {
            if (showOverlay) {
                delay(5000)
                showOverlay = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Mostrar el reproductor solo si el player no es nulo
            player?.let { p ->
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            this.player = p
                            useController = true
                            keepScreenOn = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Mostrar la rueda de carga mientras está buffering
            AnimatedVisibility(
                visible = isBuffering,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Log.d("PlayerScreen", "Showing buffering indicator")
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

            // Overlay animado con nombre y logo del canal
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Log.d("PlayerScreen", "Showing channel overlay: $channelName")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!channelLogo.isNullOrBlank()) {
                        AsyncImage(
                            model = channelLogo,
                            contentDescription = "Logo del canal",
                            modifier = Modifier
                                .size(width = 80.dp, height = 45.dp)
                                .background(Color.Black)
                                .padding(end = 12.dp)
                        )
                    }

                    Text(
                        text = "🎬 Reproduciendo: $channelName",
                        fontSize = 18.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}