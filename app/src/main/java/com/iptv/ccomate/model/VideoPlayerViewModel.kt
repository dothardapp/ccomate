package com.iptv.ccomate.model

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
class VideoPlayerViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null

    // Configurar LoadControl para optimizar el búfer
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(10000, 30000, 1000, 2000)
        .build()

    // Construir un User-Agent dinámico
    private fun buildDynamicUserAgent(): String {
        val androidVersion = Build.VERSION.RELEASE
        val deviceModel = Build.MODEL
        val chromeVersion = "129.0.0.0" // Ajusta según la versión actual de Chrome
        return "Mozilla/5.0 (Linux; Android $androidVersion; $deviceModel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/537.36 CCO IPTV"
    }

    // Configurar DataSource.Factory personalizado con encabezados
    private fun createDataSourceFactory(): DataSource.Factory {
        return object : DataSource.Factory {
            override fun createDataSource(): DataSource {
                return DefaultHttpDataSource.Factory()
                    .setUserAgent(buildDynamicUserAgent())
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
    }

    // Determinar el tipo de MediaSource según la URL
    private fun createMediaSourceFactory(videoUrl: String): MediaSource.Factory {
        return if (videoUrl.lowercase().contains(".m3u8")) {
            Log.d("VideoPlayerWithoutSSL02", "Usando HlsMediaSource para $videoUrl")
            HlsMediaSource.Factory(createDataSourceFactory())
        } else if (videoUrl.lowercase().endsWith(".flv")) {
            Log.d("VideoPlayerWithoutSSL02", "Usando ProgressiveMediaSource para $videoUrl")
            ProgressiveMediaSource.Factory(createDataSourceFactory())
        } else {
            Log.d("VideoPlayerWithoutSSL02", "Usando HlsMediaSource para $videoUrl")
            HlsMediaSource.Factory(createDataSourceFactory())
        }
    }

    suspend fun setPlayer(context: Context, videoUrl: String): Result<ExoPlayer> {
        return try {
            if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                // Liberar el ExoPlayer anterior de manera explícita
                releasePlayer()

                // Crear un nuevo ExoPlayer
                val mediaSourceFactory = createMediaSourceFactory(videoUrl)
                val player = ExoPlayer.Builder(context)
                    .setLoadControl(loadControl)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build().apply {
                        setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
                        prepare()
                        playWhenReady = true
                    }

                exoPlayer = player
                Result.success(player)
            } else {
                Log.w("VideoPlayerWithoutSSL02", "URL inválida: $videoUrl")
                Result.failure(IllegalArgumentException("URL inválida: $videoUrl"))
            }
        } catch (e: Exception) {
            Log.e("VideoPlayerWithoutSSL02", "Error al configurar media: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Método para liberar el ExoPlayer de manera explícita
    private suspend fun releasePlayer() {
        exoPlayer?.let { player ->
            player.stop() // Detener la reproducción
            player.clearMediaItems() // Limpiar los elementos de medios
            player.release() // Liberar el ExoPlayer
            Log.d("VideoPlayerWithoutSSL02", "ExoPlayer liberado")
            // Agregar un pequeño retraso para asegurar que los recursos se liberen completamente
            delay(100) // 100ms de espera
        }
        exoPlayer = null
    }

    override fun onCleared() {
        // No podemos usar suspend functions directamente en onCleared(), así que hacemos una liberación síncrona aquí
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
            Log.d("VideoPlayerWithoutSSL02", "ExoPlayer liberado en onCleared")
        }
        exoPlayer = null
        super.onCleared()
    }
}