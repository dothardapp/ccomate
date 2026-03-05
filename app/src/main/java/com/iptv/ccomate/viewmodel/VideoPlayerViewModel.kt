package com.iptv.ccomate.viewmodel

import android.annotation.SuppressLint
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
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.iptv.ccomate.util.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@OptIn(UnstableApi::class)
class VideoPlayerViewModel : ViewModel() {
    private var exoPlayer: ExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null

    // LoadControl is created per-player to avoid sharing across playback threads
    private fun createLoadControl() = DefaultLoadControl.Builder()
        .setBufferDurationsMs(5000, 15000, 500, 1000)
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
                try {
                    // Crear un TrustManager que acepte todos los certificados (solo para pruebas)
                    val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })

                    // Configurar SSLContext con el TrustManager personalizado
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, trustAllCerts, SecureRandom())

                    // Crear un OkHttpClient con el SSLContext personalizado
                    val okHttpClient = OkHttpClient.Builder()
                        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                        .hostnameVerifier { _, _ -> true } // Ignorar verificación de hostname
                        .build()

                    // Crear el DataSource.Factory usando OkHttpDataSource
                    dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                        .setUserAgent(buildDynamicUserAgent())
                        .setDefaultRequestProperties(
                            mapOf(
                                "Referer" to AppConfig.VIDEO_REFERER,
                                "Origin" to AppConfig.VIDEO_ORIGIN
                            )
                        )
                } catch (e: Exception) {
                    Log.e("VideoPlayerViewModel", "Error al configurar SSLContext: ${e.message}")
                    // Fallback a la configuración por defecto si falla
                    dataSourceFactory = OkHttpDataSource.Factory(OkHttpClient())
                        .setUserAgent(buildDynamicUserAgent())
                        .setDefaultRequestProperties(
                            mapOf(
                                "Referer" to AppConfig.VIDEO_REFERER,
                                "Origin" to AppConfig.VIDEO_ORIGIN
                            )
                        )
                }
            }
            dataSourceFactory!!
        }

    private suspend fun createMediaSource(videoUrl: String): MediaSource {
        val factory = getOrCreateDataSourceFactory()
        val urlLower = videoUrl.lowercase()
        return when {
            urlLower.endsWith(".flv") -> {
                Log.d("VideoPlayerViewModel", "Creando ProgressiveMediaSource para $videoUrl")
                ProgressiveMediaSource.Factory(factory)
                    .createMediaSource(MediaItem.fromUri(videoUrl.toUri()))
            }
            urlLower.contains(".m3u8") -> {
                Log.d("VideoPlayerViewModel", "Creando HlsMediaSource para $videoUrl")
                HlsMediaSource.Factory(factory)
                    .createMediaSource(MediaItem.fromUri(videoUrl.toUri()))
            }
            else -> {
                Log.d("VideoPlayerViewModel", "Creando ProgressiveMediaSource por defecto para $videoUrl")
                ProgressiveMediaSource.Factory(factory)
                    .createMediaSource(MediaItem.fromUri(videoUrl.toUri()))
            }
        }
    }

    suspend fun setPlayer(context: Context, videoUrl: String): Result<ExoPlayer> {
        return try {
            if (videoUrl.isNotEmpty() && (videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
                // Release previous player completely before creating a new one
                // to avoid LoadControl thread conflicts
                val existingPlayer = exoPlayer
                if (existingPlayer != null) {
                    withContext(Dispatchers.Main) {
                        existingPlayer.stop()
                        existingPlayer.clearMediaItems()
                        existingPlayer.release()
                        Log.d("VideoPlayerViewModel", "ExoPlayer anterior liberado antes de crear uno nuevo")
                    }
                    exoPlayer = null
                }

                val newPlayer = withContext(Dispatchers.Main) {
                    ExoPlayer.Builder(context)
                        .setLoadControl(createLoadControl())
                        .build()
                }
                val mediaSource = createMediaSource(videoUrl)
                withContext(Dispatchers.Main) {
                    newPlayer.setMediaSource(mediaSource)
                    newPlayer.prepare()
                    newPlayer.playWhenReady = true
                    newPlayer.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("VideoPlayerViewModel", "Error de reproducción: ${error.message}", error)
                        }
                    })
                }
                exoPlayer = newPlayer
                Log.d("VideoPlayerViewModel", "ExoPlayer configurado: $newPlayer con MediaSource: $mediaSource")
                Result.success(newPlayer)
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