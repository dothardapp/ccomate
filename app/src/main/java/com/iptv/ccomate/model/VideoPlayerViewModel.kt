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

    // Cache del DataSource.Factory
    private var dataSourceFactory: DataSource.Factory? = null

    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(5000, 15000, 500, 1000)
        .build()

    private fun buildDynamicUserAgent(): String {
        val androidVersion = Build.VERSION.RELEASE
        val deviceModel = Build.MODEL
        val chromeVersion = "129.0.0.0"
        return "Mozilla/5.0 (Linux; Android $androidVersion; $deviceModel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/537.36 CCO IPTV"
    }

    // Solo crea el DataSource.Factory una vez
    private suspend fun getOrCreateDataSourceFactory(): DataSource.Factory = withContext(Dispatchers.IO) {
        if (dataSourceFactory == null) {
            dataSourceFactory = object : DataSource.Factory {
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
        dataSourceFactory!!
    }

    // Reutiliza el DataSource.Factory ya creado
    private suspend fun createMediaSourceFactory(videoUrl: String): MediaSource.Factory = withContext(Dispatchers.IO) {
        val factory = getOrCreateDataSourceFactory()
        when {
            videoUrl.lowercase().contains(".m3u8") -> {
                Log.d("VideoPlayerViewModel", "Usando HlsMediaSource para $videoUrl")
                HlsMediaSource.Factory(factory)
            }
            videoUrl.lowercase().endsWith(".flv") -> {
                Log.d("VideoPlayerViewModel", "Usando ProgressiveMediaSource para $videoUrl")
                ProgressiveMediaSource.Factory(factory)
            }
            else -> {
                Log.d("VideoPlayerViewModel", "Usando HlsMediaSource por defecto para $videoUrl")
                HlsMediaSource.Factory(factory)
            }
        }
    }

    suspend fun setPlayer(context: Context, videoUrl: String): Result<ExoPlayer> {
        return try {
            if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                val currentPlayer = exoPlayer
                if (currentPlayer == null || currentPlayer.currentMediaItem?.mediaId != videoUrl) {
                    releasePlayer()
                    val mediaSourceFactory = createMediaSourceFactory(videoUrl)
                    val player = withContext(Dispatchers.IO) {
                        ExoPlayer.Builder(context)
                            .setLoadControl(loadControl)
                            .setMediaSourceFactory(mediaSourceFactory)
                            .build()
                    }
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

    suspend fun releasePlayer() = withContext(Dispatchers.Main) {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
            Log.d("VideoPlayerViewModel", "ExoPlayer liberado")
            delay(100)
        }
        exoPlayer = null
    }

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