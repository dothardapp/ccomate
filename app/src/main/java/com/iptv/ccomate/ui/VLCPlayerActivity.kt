package com.iptv.ccomate.ui

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
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
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class VLCPlayerActivity : ComponentActivity() {
    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener datos del Intent
        val url = intent.getStringExtra("url") ?: return
        val name = intent.getStringExtra("name") ?: "Canal"
        val logo = intent.getStringExtra("logo")

        // Validar URL antes de crear el reproductor
        if (url.isEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) {
            Log.w("VLCPlayerActivity", "URL inválida: $url")
            finish()
            return
        }

        // Inicializar LibVLC
        val vlcArgs = listOf(
            "--http-reconnect",
            "--network-caching=10000", // Buffer de 10 segundos
            "--no-drop-late-frames",
            "--no-skip-frames",
            "--avcodec-hw=any" // Habilitar aceleración por hardware
        )
        libVLC = LibVLC(this, vlcArgs)
        mediaPlayer = MediaPlayer(libVLC).apply {
            try {
                val media = Media(libVLC, url).apply {
                    addOption(":http-user-agent=Mozilla/5.0 (Android; CCOMate IPTV) AppleWebKit/537.36")
                    addOption(":http-referrer=https://ccomate.iptv.com")
                    // Agrega más opciones si necesitas headers como Origin o Authorization
                    // Ejemplo: addOption(":http-headers=Origin: https://ccomate.iptv.com")
                }
                this.media = media
                play()
            } catch (e: Exception) {
                Log.e("VLCPlayerActivity", "Error al configurar media: ${e.message}", e)
                finish()
            }
        }

        setContent {
            VLCPlayerScreen(
                mediaPlayer = mediaPlayer,
                channelName = name,
                channelLogo = logo
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.play()
    }

    override fun onStop() {
        super.onStop()
        try {
            mediaPlayer?.media?.release()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            libVLC?.release()
            mediaPlayer = null
            libVLC = null
        } catch (e: Exception) {
            Log.e("VLCPlayerActivity", "Error al liberar reproductor: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.media?.release()
            mediaPlayer?.release()
            libVLC?.release()
            mediaPlayer = null
            libVLC = null
        } catch (e: Exception) {
            Log.e("VLCPlayerActivity", "Error al liberar reproductor en onDestroy: ${e.message}", e)
        }
    }

    @Composable
    fun VLCPlayerScreen(
        mediaPlayer: MediaPlayer?,
        channelName: String,
        channelLogo: String?,
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
                    VLCVideoLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        mediaPlayer?.attachViews(this, null, true, true)
                        // Habilitar controles básicos (pausar/reanudar al tocar)
                        setOnClickListener {
                            mediaPlayer?.let { player ->
                                if (player.isPlaying) player.pause() else player.play()
                            }
                        }
                    }
                },
                update = { layout ->
                    mediaPlayer?.attachViews(layout, null, true, true)
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { layout ->
                    mediaPlayer?.detachViews()
                }
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