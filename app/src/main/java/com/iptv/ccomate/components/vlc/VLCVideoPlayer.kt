package com.iptv.ccomate.components.vlc

import android.content.Context
import android.util.Log
import android.view.ViewGroup
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun VLCVideoPlayer(
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
    var libVLC by remember { mutableStateOf<LibVLC?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isViewAttached by remember { mutableStateOf(false) }

    // Inicializar LibVLC y MediaPlayer
    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
            isBuffering = true
            showOverlay = false
            isViewAttached = false

            // Liberar el reproductor anterior de forma segura
            mediaPlayer?.let { player ->
                try {
                    player.stop()
                    player.media?.release()
                    player.release()
                } catch (e: Exception) {
                    Log.e("VLCVideoPlayer", "Error al liberar reproductor anterior: ${e.message}", e)
                }
            }
            libVLC?.release()
            mediaPlayer = null
            libVLC = null

            // Crear nueva instancia de LibVLC con lista mutable
            val vlcArgs = arrayListOf(
                "--http-reconnect",
                "--network-caching=10000", // Buffer de 10 segundos
                "--no-drop-late-frames",
                "--no-skip-frames",
                "--avcodec-hw=any", // Habilitar aceleración por hardware
                "--aout=android_audiotrack", // Usar AudioTrack para audio, compatible con Android 7.0
                "--demux=hls", // Forzar demuxer HLS para streams
                "--avcodec-dr=0" // Deshabilitar decodificación directa para estabilidad
            )
            try {
                libVLC = LibVLC(context, vlcArgs)
                mediaPlayer = MediaPlayer(libVLC).apply {
                    try {
                        val media = Media(libVLC, videoUrl.toUri()).apply {
                            addOption(":http-user-agent=Mozilla/5.0 (Android; CCOMate IPTV) AppleWebKit/537.36")
                            addOption(":http-referrer=https://ccomate.iptv.com")
                            addOption(":file-caching=10000")
                            addOption(":network-caching=10000")
                            addOption(":live-caching=10000")
                            addOption(":no-video-title-show")
                        }
                        this.media = media
                        play()
                    } catch (e: Exception) {
                        Log.e("VLCVideoPlayer", "Error al configurar media: ${e.message}", e)
                        onPlaybackError?.invoke(e)
                    }
                }
            } catch (e: Exception) {
                Log.e("VLCVideoPlayer", "Error al inicializar LibVLC: ${e.message}", e)
                onPlaybackError?.invoke(e)
            }
        } else {
            Log.w("VLCVideoPlayer", "URL inválida: $videoUrl")
            onPlaybackError?.invoke(IllegalArgumentException("URL inválida: $videoUrl"))
        }
    }

    // Manejar eventos del reproductor
    DisposableEffect(lifecycleOwner, mediaPlayer) {
        val player = mediaPlayer ?: return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        player.pause()
                    } catch (e: Exception) {
                        Log.e("VLCVideoPlayer", "Error al pausar: ${e.message}", e)
                    }
                }
                Lifecycle.Event.ON_START -> {
                    try {
                        player.play()
                    } catch (e: Exception) {
                        Log.e("VLCVideoPlayer", "Error al reproducir: ${e.message}", e)
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    try {
                        player.stop()
                        player.media?.release()
                        player.release()
                        isViewAttached = false
                    } catch (e: Exception) {
                        Log.e("VLCVideoPlayer", "Error al detener en ON_STOP: ${e.message}", e)
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        val eventListener = MediaPlayer.EventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Buffering -> {
                    isBuffering = event.buffering < 100f
                    showOverlay = false
                }
                MediaPlayer.Event.Playing -> {
                    isBuffering = false
                    showOverlay = true
                    onPlaybackStarted?.invoke()
                }
                MediaPlayer.Event.EncounteredError -> {
                    isBuffering = false
                    showOverlay = false
                    Log.e("VLCVideoPlayer", "Error de reproducción: $event")
                    onPlaybackError?.invoke(Exception("Error de reproducción en VLC"))
                }
                MediaPlayer.Event.EndReached -> {
                    isBuffering = false
                    showOverlay = false
                }
            }
        }

        player.setEventListener(eventListener)

        onDispose {
            try {
                lifecycleOwner.lifecycle.removeObserver(observer)
                player.setEventListener(null)
                // Solo detener y liberar si el player sigue siendo válido
                if (player.isReleased) {
                    Log.w("VLCVideoPlayer", "Player ya liberado, omitiendo stop/release")
                } else {
                    player.stop()
                    player.media?.release()
                    player.release()
                }
                libVLC?.release()
                isViewAttached = false
            } catch (e: Exception) {
                Log.e("VLCVideoPlayer", "Error al liberar reproductor: ${e.message}", e)
            }
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
        mediaPlayer?.let { player ->
            AndroidView(
                factory = { ctx ->
                    VLCVideoLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        if (!isViewAttached) {
                            player.attachViews(this, null, true, true)
                            isViewAttached = true
                        }
                    }
                },
                update = { layout ->
                    if (!isViewAttached) {
                        player.attachViews(layout, null, true, true)
                        isViewAttached = true
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { layout ->
                    try {
                        player.detachViews()
                    } catch (e: Exception) {
                        Log.e("VLCVideoPlayer", "Error al liberar vistas: ${e.message}", e)
                    }
                    isViewAttached = false
                }
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