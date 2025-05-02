package com.iptv.ccomate.model

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
class VideoPlayerViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null

    // Configurar LoadControl para optimizar el búfer
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(5000, 15000, 500, 1000) // Reducido para ahorrar memoria
        .build()

    // Construir un User-Agent dinámico
    private fun buildDynamicUserAgent(): String {
        val androidVersion = Build.VERSION.RELEASE
        val deviceModel = Build.MODEL
        val chromeVersion = "129.0.0.0"
        return "Mozilla/5.0 (Linux; Android $androidVersion; $deviceModel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/537.36 CCO IPTV"
    }

    // Configurar DataSource.Factory personalizado con encabezados
    private suspend fun createDataSourceFactory(): DataSource.Factory = withContext(Dispatchers.IO) {
        object : DataSource.Factory {
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
    private suspend fun createMediaSourceFactory(videoUrl: String): MediaSource.Factory = withContext(Dispatchers.IO) {
        if (videoUrl.lowercase().contains(".m3u8")) {
            Log.d("VideoPlayerViewModel", "Usando HlsMediaSource para $videoUrl")
            HlsMediaSource.Factory(createDataSourceFactory())
        } else if (videoUrl.lowercase().endsWith(".flv")) {
            Log.d("VideoPlayerViewModel", "Usando ProgressiveMediaSource para $videoUrl")
            ProgressiveMediaSource.Factory(createDataSourceFactory())
        } else {
            Log.d("VideoPlayerViewModel", "Usando HlsMediaSource por defecto para $videoUrl")
            HlsMediaSource.Factory(createDataSourceFactory())
        }
    }

    // Método para configurar o reutilizar el ExoPlayer
    suspend fun setPlayer(context: Context, videoUrl: String): Result<ExoPlayer> {
        return try {
            if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                val currentPlayer = exoPlayer
                if (currentPlayer == null || currentPlayer.currentMediaItem?.mediaId != videoUrl) {
                    // Liberar solo si el player existe y la URL es diferente
                    releasePlayer()

                    // Crear el ExoPlayer y configurar el MediaSource en un hilo secundario
                    val mediaSourceFactory = createMediaSourceFactory(videoUrl)
                    val player = withContext(Dispatchers.IO) {
                        ExoPlayer.Builder(context)
                            .setLoadControl(loadControl)
                            .setMediaSourceFactory(mediaSourceFactory)
                            .build()
                    }

                    // Operaciones que requieren el hilo principal
                    withContext(Dispatchers.Main) {
                        player.setMediaItem(MediaItem.fromUri(videoUrl.toUri()))
                        player.prepare()
                        player.playWhenReady = true
                        player.addListener(object : Player.Listener {
                            override fun onPlayerError(error: PlaybackException) {
                                Log.e("VideoPlayerViewModel", "Error de reproducción: ${error.message}", error)
                            }
                        })
                    }

                    exoPlayer = player
                    Log.d("VideoPlayerViewModel", "ExoPlayer created successfully: $player")
                } else {
                    // Reutilizar el player existente
                    withContext(Dispatchers.Main) {
                        currentPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                        currentPlayer.prepare()
                        currentPlayer.playWhenReady = true
                    }
                    Log.d("VideoPlayerViewModel", "Reutilizando ExoPlayer con nueva URL: $videoUrl")
                }
                Result.success(exoPlayer!!)
            } else {
                Log.w("VideoPlayerViewModel", "URL inválida: $videoUrl")
                Result.failure(IllegalArgumentException("URL inválida: $videoUrl"))
            }
        } catch (e: Exception) {
            Log.e("VideoPlayerViewModel", "Error al configurar media: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Método para liberar el ExoPlayer
    suspend fun releasePlayer() = withContext(Dispatchers.Main) {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
            Log.d("VideoPlayerViewModel", "ExoPlayer liberado")
            delay(100) // 100ms de espera para asegurar liberación de recursos
        }
        exoPlayer = null
    }

    // Métodos para manejar el ciclo de vida
    fun pausePlayer() {
        exoPlayer?.let {
            it.playWhenReady = false
            it.pause()
            Log.d("VideoPlayerViewModel", "Player paused")
        }
    }

    fun resumePlayer() {
        exoPlayer?.let {
            it.playWhenReady = true
            Log.d("VideoPlayerViewModel", "Player resumed")
        }
    }

    fun stopPlayer() {
        exoPlayer?.let {
            it.playWhenReady = false
            it.stop()
            Log.d("VideoPlayerViewModel", "Player stopped")
        }
    }

    override fun onCleared() {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
            Log.d("VideoPlayerViewModel", "ExoPlayer liberado en onCleared")
        }
        exoPlayer = null
        super.onCleared()
    }
}