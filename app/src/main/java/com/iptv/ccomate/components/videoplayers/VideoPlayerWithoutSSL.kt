package com.iptv.ccomate.components.videoplayers

import android.annotation.SuppressLint
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import kotlinx.coroutines.delay
import java.lang.reflect.Field
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerWithoutSSL(
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
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Configurar LoadControl para optimizar el búfer
    val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(10000, 30000, 1000, 2000)
        .build()

    // Configurar TrustManager inseguro (solo para pruebas)
    val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    })

    // Configurar SSLContext con TrustManager inseguro
    val sslSocketFactory = try {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        sslContext.socketFactory
    } catch (e: Exception) {
        Log.e("VideoPlayer02", "Error al configurar SSL: ${e.message}")
        null
    }

    // Configurar DataSource.Factory personalizado con encabezados
    val dataSourceFactory = object : DataSource.Factory {
        override fun createDataSource(): DataSource {
            val factory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Android; CCOMate IPTV) AppleWebKit/537.36")
                .setConnectTimeoutMs(8000)
                .setReadTimeoutMs(8000)
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://ccomate.iptv.com", // Ajusta según el dominio esperado
                        "Origin" to "https://ccomate.iptv.com"
                        // Agrega "Authorization" si tienes un token, por ejemplo:
                        // "Authorization" to "Bearer TU_TOKEN"
                    )
                )
            val dataSource = factory.createDataSource()
            if (videoUrl.startsWith("https://")) {
                sslSocketFactory?.let { socketFactory ->
                    try {
                        val field: Field = DefaultHttpDataSource::class.java.getDeclaredField("socketFactory")
                        field.isAccessible = true
                        field.set(dataSource, socketFactory)
                        field.isAccessible = false
                    } catch (e: Exception) {
                        Log.e("VideoPlayer02", "Error al configurar SSLSocketFactory: ${e.message}")
                    }
                }
            }
            return dataSource
        }
    }

    // Determinar el tipo de MediaSource según la URL
    val mediaSourceFactory = if (videoUrl.lowercase().contains(".m3u8")) {
        Log.d("VideoPlayer02", "Usando HlsMediaSource para $videoUrl")
        HlsMediaSource.Factory(dataSourceFactory)
    } else if (videoUrl.lowercase().endsWith(".flv")) {
        Log.d("VideoPlayer02", "Usando ProgressiveMediaSource para $videoUrl")
        ProgressiveMediaSource.Factory(dataSourceFactory)
    } else {
        Log.d("VideoPlayer02", "Usando HlsMediaSource para $videoUrl")
        HlsMediaSource.Factory(dataSourceFactory)
    }

    // Crear/Recrear player al cambiar de canal
    LaunchedEffect(videoUrl) {
        if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
            isBuffering = true
            showOverlay = false

            exoPlayer?.release()

            val player = ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    try {
                        setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
                        prepare()
                        playWhenReady = true
                    } catch (e: Exception) {
                        Log.e("VideoPlayer02", "Error al configurar media: ${e.message}")
                        onPlaybackError?.invoke(e)
                    }
                }

            exoPlayer = player
        } else {
            Log.w("VideoPlayer02", "URL inválida: $videoUrl")
            onPlaybackError?.invoke(IllegalArgumentException("URL inválida: $videoUrl"))
        }
    }

    // Liberar correctamente al salir de la app
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player.pause()
                Lifecycle.Event.ON_START -> player.playWhenReady = true
                Lifecycle.Event.ON_STOP -> player.release()
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
                Log.e("VideoPlayer02", "Error de reproducción: ${error.message}", error)
                onPlaybackError?.invoke(error)
            }
        }

        player.addListener(listener)

        onDispose {
            try {
                lifecycleOwner.lifecycle.removeObserver(observer)
                player.removeListener(listener)
                player.release()
            } catch (e: Exception) {
                Log.e("VideoPlayer02", "Error al liberar reproductor: ${e.message}")
            }
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