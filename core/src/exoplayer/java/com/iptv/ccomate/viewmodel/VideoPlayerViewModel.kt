package com.iptv.ccomate.viewmodel

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.extractor.ts.TsExtractor
import com.iptv.ccomate.util.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

// P1.3: ExoPlayer? removido de PlayerUiState — exponerlo en un StateFlow propio evita
// que cambios en isBuffering/isPlaying/hasError disparen recomposiciones en PlayerSurface.
data class PlayerUiState(
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val currentUrl: String? = null
)

private const val BUFFERING_TIMEOUT_MS = 15000L
private const val BUFFERING_TIMEOUT_UDP_MS = 30000L

@OptIn(UnstableApi::class)
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    // P2.3: OkHttpClient inyectado como singleton — evita crear una nueva instancia
    // (con su thread pool propio) en cada ViewModel o llamada a getOrCreateDataSourceFactory().
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    // P1.3: StateFlow independiente para la referencia al player.
    // PlayerSurface solo recompone cuando cambia el player (create/release),
    // no en cada cambio de isBuffering o isPlaying.
    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player: StateFlow<ExoPlayer?> = _player.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var bufferingTimeoutJob: Job? = null
    private var currentPlaybackJob: Job? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var isUdpStream: Boolean = false

    // IMPORTANTE: playerListener debe declararse ANTES del init{} block.
    // init{} usa Dispatchers.Main.immediate que ejecuta buildPlayer() síncronamente
    // en el constructor (ya estamos en Main thread). Si playerListener estuviera
    // después de init{}, sería null cuando addListener() lo reciba → NPE.
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

            // P1.1: Audio discontinuity en MPEG-TS live es recuperable.
            // seekToDefaultPosition() en vez de seekTo(currentPosition): después de una
            // discontinuidad el live window avanzó y la posición actual puede estar
            // detrás de él, lo que haría fallar la preparación.
            val cause = error.cause
            if (cause is androidx.media3.exoplayer.audio.AudioSink.UnexpectedDiscontinuityException) {
                Log.w("VideoPlayerVM", "Audio discontinuity detected, recovering...")
                exoPlayer?.let { player ->
                    player.seekToDefaultPosition()
                    player.prepare()
                }
                return
            }

            // Behind live window: re-sync automatico
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                Log.w("VideoPlayerVM", "Behind live window, re-syncing...")
                exoPlayer?.let { player ->
                    player.seekToDefaultPosition()
                    player.prepare()
                }
                return
            }

            val message = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                    "Error de conexion de red"
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Tiempo de conexion agotado"
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "El servidor respondio con un error (HTTP)"
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                    "Stream no encontrado"
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

    init {
        // Pre-calentar ExoPlayer durante la construcción del ViewModel.
        // El ViewModel se crea en la composición inicial de la pantalla, antes de cualquier
        // interacción del usuario. Al construir el player aquí evitamos que ExoPlayer.Builder
        // + TrackSelector + LoadControl bloqueen el Main thread cuando el usuario selecciona
        // un canal (lo que causaba los Davey de ~800-1500ms).
        viewModelScope.launch(Dispatchers.Main.immediate) {
            buildPlayer()
        }
        // P3: Pre-calentar MulticastLock junto al player — la primera llamada a
        // WifiManager.createMulticastLock() implica IPC al sistema (~1-5ms).
        // Hacerla en init{} la saca del path crítico del primer canal UDP.
        // Para canales no-UDP, playUrl() llama a releaseMulticastLock() sin overhead.
        viewModelScope.launch(Dispatchers.IO) {
            acquireMulticastLock()
        }
    }

    private fun startBufferingTimeout() {
        cancelBufferingTimeout()
        val timeout = if (isUdpStream) BUFFERING_TIMEOUT_UDP_MS else BUFFERING_TIMEOUT_MS
        bufferingTimeoutJob = viewModelScope.launch {
            delay(timeout)
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

    // ── Multicast lock ──

    private fun acquireMulticastLock() {
        if (multicastLock?.isHeld == true) return
        try {
            val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifi?.createMulticastLock("CCOMate_multicast")?.apply {
                setReferenceCounted(false)
                acquire()
            }
            Log.d("VideoPlayerVM", "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e("VideoPlayerVM", "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.d("VideoPlayerVM", "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.e("VideoPlayerVM", "Failed to release MulticastLock", e)
        }
        multicastLock = null
    }

    /**
     * Construye y configura el ExoPlayer. Debe llamarse en Main thread.
     * Se invoca desde init{} para pre-calentar el player antes de la primera
     * interacción del usuario. En playUrl() se reutiliza la instancia ya creada.
     */
    private fun buildPlayer(): ExoPlayer {
        exoPlayer?.let { return it }

        val renderersFactory = DefaultRenderersFactory(appContext)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

        val newPlayer = ExoPlayer.Builder(appContext)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(createTrackSelector())
            .setLoadControl(createLoadControl())
            .build()

        newPlayer.addListener(playerListener)
        newPlayer.setAudioAttributes(
            androidx.media3.common.AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus= */ false
        )
        exoPlayer = newPlayer
        // P1.3: emitir en StateFlow propio, no en PlayerUiState
        _player.value = newPlayer
        return newPlayer
    }

    private fun createLoadControl() = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            5000,   // minBufferMs
            30000,  // maxBufferMs
            1500,   // bufferForPlaybackMs
            3000    // bufferForPlaybackAfterRebufferMs
        )
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
                // P2.3: usar el OkHttpClient singleton inyectado por Hilt
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
            val mediaItem = MediaItem.fromUri(videoUrl.toUri())
            val urlLower = videoUrl.lowercase()
            val scheme = Uri.parse(videoUrl).scheme?.lowercase() ?: ""

            // Para UDP/RTP usar DefaultDataSource con extractor TS optimizado
            if (scheme == "udp" || scheme == "rtp") {
                val defaultFactory = DefaultDataSource.Factory(appContext)
                val tsExtractorsFactory = DefaultExtractorsFactory()
                    .setTsExtractorFlags(
                        DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES
                                or DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS
                    )
                    .setTsExtractorTimestampSearchBytes(TsExtractor.DEFAULT_TIMESTAMP_SEARCH_BYTES)
                return@withContext ProgressiveMediaSource.Factory(defaultFactory, tsExtractorsFactory)
                    .createMediaSource(mediaItem)
            }

            val factory = getOrCreateDataSourceFactory()
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
        if (videoUrl.isEmpty()) {
            Log.w("VideoPlayerVM", "URL vacia")
            _playerState.value = _playerState.value.copy(
                hasError = true,
                errorMessage = "URL vacia"
            )
            return
        }

        // Si ya esta reproduciendo la misma URL, no reiniciar
        if (_playerState.value.currentUrl == videoUrl && _playerState.value.isPlaying) {
            return
        }

        currentPlaybackJob?.cancel()
        currentPlaybackJob = viewModelScope.launch {
            // P2.2: Debounce 250ms — evita reproducir canales intermedios al navegar rapido
            // con el mando. Si el usuario cambia canal antes de 250ms, este Job se cancela
            // y no se ejecuta ningun trabajo pesado (stop, createMediaSource, prepare).
            delay(250)

            // Multicast lock para UDP/RTP (despues del debounce — solo para el canal final)
            val scheme = Uri.parse(videoUrl).scheme?.lowercase() ?: ""
            isUdpStream = scheme == "udp" || scheme == "rtp"
            if (isUdpStream) acquireMulticastLock() else releaseMulticastLock()

            _playerState.value = _playerState.value.copy(
                isBuffering = true,
                hasError = false,
                errorMessage = "",
                currentUrl = videoUrl
            )

            try {
                val mediaSource = createMediaSource(videoUrl)

                withContext(Dispatchers.Main) {
                    // buildPlayer() retorna el player ya pre-calentado desde init{}.
                    // En el caso extremo de que init{} aún no haya terminado (race muy improbable),
                    // lo construye aquí mismo en Main thread como fallback.
                    val player = buildPlayer()

                    player.stop()
                    player.clearMediaItems()
                    player.setMediaSource(mediaSource)
                    player.prepare()
                    player.playWhenReady = true
                }
                Log.d("VideoPlayerVM", "Playing: $videoUrl")
            } catch (e: CancellationException) {
                Log.d("VideoPlayerVM", "Playback coroutine cancelled for: $videoUrl")
                throw e
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
        // P1.3: limpiar el StateFlow del player al liberar
        _player.value = null
        releaseMulticastLock()
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
