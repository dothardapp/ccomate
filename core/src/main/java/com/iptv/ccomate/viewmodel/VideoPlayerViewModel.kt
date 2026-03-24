package com.iptv.ccomate.viewmodel

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import javax.inject.Inject

data class PlayerUiState(
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val currentUrl: String? = null
)

private const val TAG = "VideoPlayerVM"
private const val BUFFERING_TIMEOUT_MS = 15000L

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerUiState())
    val playerState: StateFlow<PlayerUiState> = _playerState.asStateFlow()

    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentMedia: Media? = null
    private var bufferingTimeoutJob: Job? = null
    private var currentPlaybackJob: Job? = null
    private var activeVideoLayout: VLCVideoLayout? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    private val vlcArgs = arrayListOf(
        // Transporte RTSP sobre TCP (evita problemas de NAT/firewall con UDP)
        "--rtsp-tcp",
        // Caching global — 1500ms es buen balance entre latencia y estabilidad.
        // Para protocolos especificos (UDP, RTP) se ajusta en media options.
        "--network-caching=1500",
        "--live-caching=1500",
        "--file-caching=1500",
        // Permitir drop de frames atrasados — esencial para live/multicast.
        // Sin esto, frames se acumulan y la latencia crece progresivamente.
        "--drop-late-frames",
        "--skip-frames",
        // Verbose logging para diagnostico (quitar en release si es necesario)
        "--verbose=0"
    )

    private val mediaPlayerListener = MediaPlayer.EventListener { event ->
        when (event.type) {
            MediaPlayer.Event.Buffering -> {
                val percent = event.buffering
                if (percent < 100f) {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = true,
                        isPlaying = false
                    )
                    startBufferingTimeout()
                } else {
                    cancelBufferingTimeout()
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        hasError = false,
                        errorMessage = ""
                    )
                }
            }
            MediaPlayer.Event.Playing -> {
                cancelBufferingTimeout()
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = true,
                    hasError = false,
                    errorMessage = ""
                )
            }
            MediaPlayer.Event.Paused -> {
                _playerState.value = _playerState.value.copy(isPlaying = false)
            }
            MediaPlayer.Event.Stopped -> {
                cancelBufferingTimeout()
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = false
                )
            }
            MediaPlayer.Event.EncounteredError -> {
                cancelBufferingTimeout()
                Log.e(TAG, "VLC playback error for: ${_playerState.value.currentUrl}")
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = false,
                    hasError = true,
                    errorMessage = "Error de reproduccion. Verifica la conexion o el canal."
                )
            }
            MediaPlayer.Event.EndReached -> {
                cancelBufferingTimeout()
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = false
                )
            }
        }
    }

    private fun getOrCreateLibVLC(): LibVLC {
        return libVLC ?: LibVLC(appContext, vlcArgs).also {
            libVLC = it
            Log.d(TAG, "LibVLC instance created")
        }
    }

    private fun getOrCreateMediaPlayer(): MediaPlayer {
        return mediaPlayer ?: MediaPlayer(getOrCreateLibVLC()).also {
            it.setEventListener(mediaPlayerListener)
            mediaPlayer = it
            Log.d(TAG, "MediaPlayer instance created")
        }
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
            Log.d(TAG, "MulticastLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire MulticastLock", e)
        }
    }

    private fun releaseMulticastLock() {
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
                Log.d(TAG, "MulticastLock released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release MulticastLock", e)
        }
        multicastLock = null
    }

    // ── API publica para el Composable ──

    /**
     * Vincula el MediaPlayer a un VLCVideoLayout.
     * Llamar desde AndroidView.factory o DisposableEffect.
     */
    fun attachViews(videoLayout: VLCVideoLayout) {
        try {
            val isReattach = activeVideoLayout != null

            // Siempre desvincular antes de re-vincular — incluso si es el mismo layout.
            // Cuando movableContentOf re-parentea la vista (fullscreen ↔ normal, rotacion),
            // la superficie nativa se destruye y VLC necesita un ciclo detach → attach
            // para bindear la nueva superficie.
            if (isReattach) {
                mediaPlayer?.detachViews()
                Log.d(TAG, "Detached views before re-attach")
            }
            activeVideoLayout = videoLayout
            val player = getOrCreateMediaPlayer()
            player.attachViews(videoLayout, null, false, false)

            // Cuando es un re-attach (fullscreen toggle / rotacion) y el player esta
            // activo, forzar refresh del video track. Algunos codecs no re-renderizan
            // automaticamente en la nueva superficie — el toggle off→on obliga a VLC
            // a re-decodificar un keyframe y pintar en el nuevo Surface.
            if (isReattach && (_playerState.value.isPlaying || _playerState.value.isBuffering)) {
                val vTrack = player.videoTrack
                if (vTrack != -1) {
                    player.videoTrack = -1
                    player.videoTrack = vTrack
                    Log.d(TAG, "Video track toggled to force surface refresh")
                }
            }

            Log.d(TAG, "Views attached (reattach=$isReattach)")
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching views", e)
        }
    }

    /**
     * Desvincula las views solo si el layout que llama es el activo.
     * Evita race condition donde el dispose de la vista vieja desconecta a la nueva.
     */
    fun detachViews(videoLayout: VLCVideoLayout) {
        if (activeVideoLayout !== videoLayout) {
            Log.d(TAG, "detachViews ignored — caller is not the active layout")
            return
        }
        try {
            mediaPlayer?.detachViews()
            activeVideoLayout = null
            Log.d(TAG, "Views detached")
        } catch (e: Exception) {
            Log.e(TAG, "Error detaching views", e)
        }
    }

    fun playUrl(videoUrl: String) {
        if (videoUrl.isEmpty()) {
            Log.w(TAG, "URL vacia")
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

        _playerState.value = _playerState.value.copy(
            isBuffering = true,
            hasError = false,
            errorMessage = "",
            currentUrl = videoUrl
        )

        currentPlaybackJob = viewModelScope.launch {
            try {
                val player = getOrCreateMediaPlayer()

                // Liberar media anterior
                currentMedia?.release()

                // Crear nuevo Media desde URI
                val vlc = getOrCreateLibVLC()
                val uri = Uri.parse(videoUrl)
                val media = Media(vlc, uri)

                // HW decoder con fallback a software — si el codec HW falla
                // (ej: perfil no soportado), VLC cae a avcodec automaticamente.
                media.setHWDecoderEnabled(true, true)

                // Opciones por protocolo de transporte
                val scheme = uri.scheme?.lowercase() ?: ""
                when {
                    scheme == "udp" || scheme == "rtp" -> {
                        // Android filtra multicast por defecto — necesitamos el lock
                        acquireMulticastLock()
                        // Multicast/UDP: mas buffer para absorber jitter de red.
                        // UDP no tiene retransmision, necesita margen para perdida de paquetes.
                        media.addOption(":network-caching=2000")
                        media.addOption(":live-caching=2000")
                        Log.d(TAG, "Applied multicast/UDP options for: $videoUrl")
                    }
                    scheme == "rtsp" -> {
                        releaseMulticastLock()
                        // RTSP: caching moderado, TCP ya maneja retransmision.
                        media.addOption(":network-caching=1500")
                        media.addOption(":live-caching=1500")
                    }
                    scheme == "http" || scheme == "https" -> {
                        releaseMulticastLock()
                        // HTTP(S): streams HLS/DASH o HTTP directo.
                        media.addOption(":network-caching=1500")
                        media.addOption(":live-caching=1500")
                        media.addOption(":http-user-agent=Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        media.addOption(":http-referrer=$videoUrl")
                    }
                    else -> {
                        releaseMulticastLock()
                        media.addOption(":network-caching=1500")
                        media.addOption(":live-caching=1500")
                    }
                }

                currentMedia = media

                player.media = media
                player.play()

                Log.d(TAG, "Playing ($scheme): $videoUrl")
            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Playback cancelled for: $videoUrl")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error playing: ${e.message}", e)
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
        // Forzar reinicio completo
        _playerState.value = _playerState.value.copy(currentUrl = null)
        playUrl(url)
    }

    fun pausePlayer() {
        mediaPlayer?.pause()
    }

    fun resumePlayer() {
        val player = mediaPlayer ?: return
        // Si el player fue detenido (ON_STOP → stopPlayer), play() sobre un player
        // en estado Stopped no funciona — necesita re-asignar el media para reiniciar.
        // Si fue pausado (ON_PAUSE → pausePlayer), play() lo reanuda correctamente.
        if (currentMedia != null && !player.isPlaying) {
            val url = _playerState.value.currentUrl
            if (url != null && player.media == null) {
                // Player fue detenido: re-asignar media y reproducir
                player.media = currentMedia
                player.play()
                Log.d(TAG, "Resumed from stopped state: $url")
                return
            }
        }
        player.play()
    }

    fun stopPlayer() {
        mediaPlayer?.stop()
    }

    fun releasePlayer() {
        currentPlaybackJob?.cancel()
        cancelBufferingTimeout()

        // Orden critico de liberacion para evitar memory leaks C++:
        // 1. Detener reproduccion
        // 2. Desvincular views
        // 3. Liberar Media
        // 4. Liberar MediaPlayer
        // (LibVLC se mantiene vivo para reusar — se libera en onCleared)

        try {
            mediaPlayer?.stop()
            mediaPlayer?.detachViews()
            activeVideoLayout = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping/detaching", e)
        }

        currentMedia?.release()
        currentMedia = null

        mediaPlayer?.setEventListener(null)
        mediaPlayer?.release()
        mediaPlayer = null

        releaseMulticastLock()

        _playerState.value = PlayerUiState()
        Log.d(TAG, "Player released")
    }

    override fun onCleared() {
        releasePlayer()
        // Liberar la instancia nativa LibVLC (binarios C++)
        libVLC?.release()
        libVLC = null
        Log.d(TAG, "LibVLC released — ViewModel cleared")
        super.onCleared()
    }

    // ── Buffering timeout ──

    private fun startBufferingTimeout() {
        cancelBufferingTimeout()
        bufferingTimeoutJob = viewModelScope.launch {
            delay(BUFFERING_TIMEOUT_MS)
            val state = _playerState.value
            if (state.isBuffering && !state.hasError) {
                Log.w(TAG, "Buffering timeout for: ${state.currentUrl}")
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
}
