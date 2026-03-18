package com.iptv.ccomate.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.iptv.ccomate.util.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private var exoPlayer: ExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null

    // LoadControl is created per-player to avoid sharing across playback threads
    private fun createLoadControl() = DefaultLoadControl.Builder()
        .setBufferDurationsMs(10000, 30000, 1000, 2000)
        .build()

    private fun buildDynamicUserAgent(): String {
        val androidVersion = Build.VERSION.RELEASE
        val deviceModel = Build.MODEL
        val chromeVersion = "129.0.0.0"
        return "Mozilla/5.0 (Linux; Android $androidVersion; $deviceModel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/537.36 CCO IPTV"
    }

    private suspend fun getOrCreateDataSourceFactory(): DataSource.Factory =
        withContext(Dispatchers.IO) {
            if (dataSourceFactory == null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                    .setUserAgent(buildDynamicUserAgent())
                    .setDefaultRequestProperties(
                        mapOf(
                            "Referer" to AppConfig.VIDEO_REFERER,
                            "Origin" to AppConfig.VIDEO_ORIGIN
                        )
                    )
            }
            dataSourceFactory!!
        }

    private suspend fun createMediaSource(videoUrl: String): MediaSource =
        withContext(Dispatchers.IO) {
            val factory = getOrCreateDataSourceFactory()
            val mediaItem = MediaItem.fromUri(videoUrl.toUri())
            val urlLower = videoUrl.lowercase()
            when {
                urlLower.endsWith(".flv") -> {
                    ProgressiveMediaSource.Factory(factory)
                        .createMediaSource(mediaItem)
                }
                urlLower.contains(".m3u8") -> {
                    HlsMediaSource.Factory(factory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    ProgressiveMediaSource.Factory(factory)
                        .createMediaSource(mediaItem)
                }
            }
        }

    suspend fun setPlayer(videoUrl: String): Result<ExoPlayer> {
        return try {
            if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                // Preparar MediaSource en IO primero (no necesita el player anterior)
                val mediaSource = createMediaSource(videoUrl)

                // Liberar player anterior y crear nuevo en Main (requerido por ExoPlayer)
                val newPlayer = withContext(Dispatchers.Main) {
                    exoPlayer?.let { existingPlayer ->
                        Log.d("VideoPlayerViewModel", "Liberando ExoPlayer anterior (URL: $videoUrl)")
                        existingPlayer.stop()
                        existingPlayer.clearMediaItems()
                        existingPlayer.release()
                    }
                    exoPlayer = null

                    ExoPlayer.Builder(appContext)
                        .setLoadControl(createLoadControl())
                        .build()
                }
                withContext(Dispatchers.Main) {
                    newPlayer.setMediaSource(mediaSource)
                    newPlayer.prepare()
                    newPlayer.playWhenReady = true
                    newPlayer.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("VideoPlayerViewModel", "Error de reproduccion: ${error.message}", error)
                        }
                    })
                    exoPlayer = newPlayer
                }
                Log.d("VideoPlayerViewModel", "ExoPlayer configurado exitosamente")
                Result.success(newPlayer)
            } else {
                Log.w("VideoPlayerViewModel", "URL invalida: $videoUrl")
                Result.failure(IllegalArgumentException("URL invalida: $videoUrl"))
            }
        } catch (e: Exception) {
            Log.e("VideoPlayerViewModel", "Error al configurar media: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun releasePlayer() = withContext(Dispatchers.Main) {
        try {
            exoPlayer?.let { player ->
                player.stop()
                player.clearMediaItems()
                player.release()
                Log.d("VideoPlayerViewModel", "ExoPlayer liberado")
            }
            exoPlayer = null
        } catch (e: Exception) {
            Log.e("VideoPlayerViewModel", "Error al liberar ExoPlayer: ${e.message}", e)
        }
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