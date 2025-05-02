package com.iptv.ccomate.activity

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        lifecycleScope.launch {
            viewModel.setPlayer(this@PlayerActivityMedia3, url).onSuccess { player ->
                setContent { PlayerScreen(player, name, logo) }
            }.onFailure { e ->
                Log.e("PlayerActivityMedia3", "Error al inicializar player: ${e.message}")
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch { viewModel.pausePlayer() }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { viewModel.resumePlayer() }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch { viewModel.stopPlayer() }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch { viewModel.releasePlayer() }
    }

    @Composable
    fun PlayerScreen(
        player: ExoPlayer?,
        channelName: String,
        channelLogo: String?
    ) {
        var isBuffering by remember { mutableStateOf(true) } // Estado inicial: buffering
        var showOverlay by remember { mutableStateOf(false) } // Overlay del canal

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
                delay(5000) // Mantener el overlay visible durante 5 segundos
                showOverlay = false
            }
        }

        Box(modifier = Modifier.Companion.fillMaxSize()) {
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
                    modifier = Modifier.Companion.fillMaxSize()
                )

                LaunchedEffect(p) {
                    Log.d("PlayerScreen", "Player updated: $p")
                }
            }

            // Mostrar la rueda de carga mientras está buffering
            AnimatedVisibility(
                visible = isBuffering,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Log.d("PlayerScreen", "Showing buffering indicator")
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

            // Overlay animado con nombre y logo del canal
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Log.d("PlayerScreen", "Showing channel overlay: $channelName")
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color.Companion.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    if (!channelLogo.isNullOrBlank()) {
                        AsyncImage(
                            model = channelLogo,
                            contentDescription = "Logo del canal",
                            modifier = Modifier.Companion
                                .size(width = 80.dp, height = 45.dp)
                                .background(Color.Companion.Black)
                                .padding(end = 12.dp)
                        )
                    }

                    Text(
                        text = "🎬 Reproduciendo: $channelName",
                        fontSize = 18.sp,
                        color = Color.Companion.White,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis
                    )
                }
            }
        }
    }
}