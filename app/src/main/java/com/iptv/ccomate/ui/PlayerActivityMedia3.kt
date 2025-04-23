package com.iptv.ccomate.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(UnstableApi::class)
class PlayerActivityMedia3 : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantener la pantalla activa
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Obtener datos del Intent
        val url = intent.getStringExtra("url") ?: run {
            Log.w("PlayerActivityMedia3", "URL no proporcionada")
            finish()
            return
        }
        val name = intent.getStringExtra("name") ?: "Canal"
        val logo = intent.getStringExtra("logo")

        // Validar URL
        if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
            Log.w("PlayerActivityMedia3", "URL inválida: $url")
            finish()
            return
        }

        // Configurar LoadControl
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10000, 30000, 1000, 2000)
            .build()

        // Configurar DataSource.Factory
        val dataSourceFactory = object : DataSource.Factory {
            override fun createDataSource(): DataSource {
                return DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0 (Android; CCOMate IPTV) AppleWebKit/537.36")
                    .setConnectTimeoutMs(8000)
                    .setReadTimeoutMs(8000)
                    .setAllowCrossProtocolRedirects(true)
                    .setDefaultRequestProperties(
                        mapOf(
                            "Referer" to "https://ccomate.iptv.com",
                            "Origin" to "https://ccomate.iptv.com"
                        )
                    )
                    .createDataSource()
            }
        }

        // Determinar el tipo de MediaSource
        val mediaSourceFactory = when {
            url.lowercase().contains(".m3u8") -> {
                Log.d("PlayerActivityMedia3", "Usando HlsMediaSource para $url")
                HlsMediaSource.Factory(dataSourceFactory)
            }
            url.lowercase().endsWith(".flv") -> {
                Log.d("PlayerActivityMedia3", "Usando ProgressiveMediaSource para $url")
                ProgressiveMediaSource.Factory(dataSourceFactory)
            }
            else -> {
                Log.d("PlayerActivityMedia3", "Usando HlsMediaSource por defecto para $url")
                HlsMediaSource.Factory(dataSourceFactory)
            }
        }

        // Crear y configurar el reproductor
        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                try {
                    setMediaItem(MediaItem.fromUri(url.toUri()))
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("PlayerActivityMedia3", "Error de reproducción: ${error.message}", error)
                            finish() // Cerrar la actividad si hay un error crítico
                        }
                    })
                    prepare()
                    playWhenReady = true
                } catch (e: Exception) {
                    Log.e("PlayerActivityMedia3", "Error al configurar media: ${e.message}", e)
                    finish()
                }
            }

        setContent {
            PlayerScreen(
                player = player,
                channelName = name,
                channelLogo = logo
            )
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.run {
            pause()
            stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.run {
            stop()
            release()
            player = null
        }
    }

    @Composable
    fun PlayerScreen(
        player: ExoPlayer?,
        channelName: String,
        channelLogo: String?
    ) {
        var showOverlay by remember { mutableStateOf(true) }

        // Mostrar el overlay durante 3 segundos al inicio
        LaunchedEffect(Unit) {
            delay(3000)
            showOverlay = false
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Mostrar el reproductor
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = true
                        keepScreenOn = true // Refuerzo para mantener la pantalla activa
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay animado con nombre y logo del canal
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
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