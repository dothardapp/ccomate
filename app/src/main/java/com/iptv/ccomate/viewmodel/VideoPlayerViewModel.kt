package com.iptv.ccomate.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.iptv.ccomate.util.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class PlayerUiState(
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val currentUrl: String? = null,
    val player: ExoPlayer? = null
)

private const val BUFFERING_TIMEOUT_MS = 15000L

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var bufferingTimeoutJob: Job? = null
    private var currentPlaybackJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            Log.d("VideoPlayerVM", "Playback state: $state")
            when (state) {
                Player.STATE_BUFFERING -> {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = true,
                        isPlaying = false
                    )
                    startBufferingTimeout()
                }
                Player.STATE_READY -> {
                    cancelBufferingTimeout()
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        hasError = false,
                        errorMessage = ""
                    )
                }
                Player.STATE_ENDED -> {
                    cancelBufferingTimeout()
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false
                    )
                }
                Player.STATE_IDLE -> {
                    cancelBufferingTimeout()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("VideoPlayerVM", "Player error: ${error.message}", error)
            cancelBufferingTimeout()
            val message = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                    "Error de conexion de red"
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Tiempo de conexion agotado"
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "El servidor respondio con un error (HTTP)"
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                    "Stream no encontrado"
                PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW ->
                    "Transmision en vivo no disponible"
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ->
                    "Error al procesar el stream"
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                    "Error al leer la lista de reproduccion"
                else ->
                    error.localizedMessage ?: "Error de reproduccion desconocido"
            }
            _playerState.value = _playerState.value.copy(
                isBuffering = false,
                isPlaying = false,
                hasError = true,
                errorMessage = message
            )
        }
    }

    private fun startBufferingTimeout() {
        cancelBufferingTimeout()
        bufferingTimeoutJob = viewModelScope.launch {
            delay(BUFFERING_TIMEOUT_MS)
            val state = _playerState.value
            if (state.isBuffering && !state.hasError) {
                Log.w("VideoPlayerVM", "Buffering timeout for: ${state.currentUrl}")
                _playerState.value = state.copy(
                    isBuffering = false,
                    hasError = true,
                    errorMessage = "El canal no responde. Posible problema de red o senal."
                )
            }
        }
    }

    private fun cancelBufferingTimeout() {
        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = null
    }

    private fun createLoadControl() = DefaultLoadControl.Builder()
        .setBufferDurationsMs(32000, 64000, 1500, 3000)
        .build()

    private fun createTrackSelector() =
        DefaultTrackSelector(appContext, AdaptiveTrackSelection.Factory())

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
                urlLower.endsWith(".flv") ->
                    ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
                urlLower.contains(".m3u8") ->
                    HlsMediaSource.Factory(factory).createMediaSource(mediaItem)
                else ->
                    ProgressiveMediaSource.Factory(factory).createMediaSource(mediaItem)
            }
        }

    fun playUrl(videoUrl: String) {
        if (videoUrl.isEmpty() || !(videoUrl.startsWith("http://") || videoUrl.startsWith("https://"))) {
            Log.w("VideoPlayerVM", "URL invalida: $videoUrl")
            _playerState.value = _playerState.value.copy(
                hasError = true,
                errorMessage = "URL invalida: $videoUrl"
            )
            return
        }

        currentPlaybackJob?.cancel()

        _playerState.value = _playerState.value.copy(
            isBuffering = true,
            hasError = false,
            errorMessage = "",
            currentUrl = videoUrl
        )

        currentPlaybackJob = viewModelScope.launch {
            try {
                val mediaSource = createMediaSource(videoUrl)

                withContext(Dispatchers.Main) {
                    val player = exoPlayer ?: run {
                        val newPlayer = ExoPlayer.Builder(appContext)
                            .setTrackSelector(createTrackSelector())
                            .setLoadControl(createLoadControl())
                            .build()
                        newPlayer.addListener(playerListener)
                        exoPlayer = newPlayer
                        newPlayer
                    }

                    player.stop()
                    player.clearMediaItems()
                    player.setMediaSource(mediaSource)
                    player.prepare()
                    player.playWhenReady = true

                    _playerState.value = _playerState.value.copy(player = player)
                }
                Log.d("VideoPlayerVM", "Playing: $videoUrl")
            } catch (e: Exception) {
                Log.e("VideoPlayerVM", "Error setting media: ${e.message}", e)
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    hasError = true,
                    errorMessage = e.localizedMessage ?: "Error desconocido"
                )
            }
        }
    }

    fun retry() {
        val url = _playerState.value.currentUrl ?: return
        playUrl(url)
    }

    fun releasePlayer() {
        currentPlaybackJob?.cancel()
        cancelBufferingTimeout()
        exoPlayer?.let { player ->
            player.removeListener(playerListener)
            player.stop()
            player.clearMediaItems()
            player.release()
            Log.d("VideoPlayerVM", "ExoPlayer released")
        }
        exoPlayer = null
        _playerState.value = PlayerUiState()
    }

    fun pausePlayer() {
        exoPlayer?.let {
            it.playWhenReady = false
            it.pause()
        }
    }

    fun resumePlayer() {
        exoPlayer?.let {
            it.playWhenReady = true
        }
    }

    fun stopPlayer() {
        exoPlayer?.let {
            it.playWhenReady = false
            it.stop()
        }
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
