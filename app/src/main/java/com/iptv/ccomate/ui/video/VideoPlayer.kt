package com.iptv.ccomate.ui.video

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import com.iptv.ccomate.viewmodel.VideoPlayerViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@OptIn(UnstableApi::class) private const val ENABLE_EPG_OVERLAY = true

private const val BUFFERING_TIMEOUT_MS = 15000L
private const val MAX_RETRY_ATTEMPTS = 3

@Composable
fun VideoPlayer(
        context: Context,
        videoUrl: String,
        modifier: Modifier = Modifier,
        channelName: String? = null,
        onPlaybackStarted: (() -> Unit)? = null,
        onPlaybackError: ((Throwable) -> Unit)? = null,
        currentProgram: com.iptv.ccomate.model.EPGProgram? = null,
        isFullscreen: Boolean = false
) {
    var isBuffering by remember { mutableStateOf(true) }
    var showOverlay by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var retryCount by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val retryFocusRequester = remember { FocusRequester() }

    val viewModel: VideoPlayerViewModel = viewModel()
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    Log.d("VideoPlayer", "Rendering VideoPlayer for URL: $videoUrl")

    // Actualizar el ExoPlayer cuando cambie la URL
    LaunchedEffect(videoUrl, retryCount) {
        Log.d("VideoPlayer", "LaunchedEffect triggered for URL: $videoUrl (retry: $retryCount)")
        isBuffering = true
        showOverlay = false
        hasError = false
        errorMessage = ""

        // Garantizar que la rueda de carga se muestre por al menos 300ms
        delay(300)

        viewModel
                .setPlayer(context, videoUrl)
                .onSuccess { player ->
                    Log.d("VideoPlayer", "ExoPlayer successfully set: $player")
                    exoPlayer = player
                }
                .onFailure { error ->
                    Log.e("VideoPlayer", "Failed to set ExoPlayer", error)
                    isBuffering = false
                    showOverlay = false
                    hasError = true
                    errorMessage = error.localizedMessage ?: "Error desconocido al iniciar reproducción"
                    onPlaybackError?.invoke(error)
                }
    }

    // Timeout: si el buffering dura más de BUFFERING_TIMEOUT_MS, mostrar error
    LaunchedEffect(isBuffering, videoUrl, retryCount) {
        if (isBuffering) {
            delay(BUFFERING_TIMEOUT_MS)
            // Si todavía está en buffering después del timeout
            if (isBuffering && !hasError) {
                Log.w("VideoPlayer", "Buffering timeout alcanzado para: $videoUrl")
                // Detener el player para evitar spam de codec en main thread
                try {
                    exoPlayer?.stop()
                    exoPlayer?.clearMediaItems()
                    Log.d("VideoPlayer", "Player detenido tras timeout")
                } catch (e: Exception) {
                    Log.w("VideoPlayer", "Error al detener player tras timeout", e)
                }
                isBuffering = false
                hasError = true
                errorMessage = "El canal no responde. Posible problema de red o señal."
                onPlaybackError?.invoke(
                    Exception("Timeout de carga: el stream no respondió en ${BUFFERING_TIMEOUT_MS / 1000}s")
                )
            }
        }
    }

    // Log para verificar si exoPlayer cambió
    LaunchedEffect(exoPlayer) {
        if (exoPlayer != null) {
            Log.d("VideoPlayer", "exoPlayer updated: $exoPlayer")
        } else {
            Log.d("VideoPlayer", "exoPlayer is null")
        }
    }

    // Manejar el ciclo de vida usando el ViewModel
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.pausePlayer()
                }
                Lifecycle.Event.ON_START -> {
                    viewModel.resumePlayer()
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.stopPlayer()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(
                            "VideoPlayer",
                            "ON_DESTROY - No se detiene el ExoPlayer, lo hace el ViewModel"
                    )
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d("VideoPlayer", "DisposableEffect onDispose called")
        }
    }

    // Liberar el player cuando el composable sale de la composición (ej: navegación)
    DisposableEffect(Unit) {
        onDispose {
            Log.d("VideoPlayer", "Composable leaving composition - releasing player")
            exoPlayer = null
            kotlinx.coroutines.MainScope().launch {
                viewModel.releasePlayer()
            }
        }
    }

    // Listener para los estados del ExoPlayer
    DisposableEffect(exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}

        val listener =
                object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d("VideoPlayer", "Playback state changed: $state")
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                Log.d("VideoPlayer", "Buffering started")
                                isBuffering = true
                                showOverlay = false
                                // No borrar hasError aquí: si ya hubo error, mantenerlo
                            }
                            Player.STATE_READY -> {
                                Log.d("VideoPlayer", "Player ready")
                                isBuffering = false
                                hasError = false
                                errorMessage = ""
                                showOverlay = true
                                onPlaybackStarted?.invoke()
                            }
                            Player.STATE_ENDED -> {
                                Log.d("VideoPlayer", "Playback ended")
                                isBuffering = false
                                showOverlay = false
                            }
                            Player.STATE_IDLE -> {
                                Log.d("VideoPlayer", "Player idle")
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayer", "Error de reproducción: ${error.message}", error)
                        isBuffering = false
                        showOverlay = false
                        hasError = true
                        errorMessage = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                                "Error de conexión de red"
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                                "Tiempo de conexión agotado"
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                                "El servidor respondió con un error (HTTP)"
                            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                                "Stream no encontrado"
                            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW ->
                                "Transmisión en vivo no disponible"
                            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ->
                                "Error al procesar el stream"
                            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                                "Error al leer la lista de reproducción"
                            else ->
                                error.localizedMessage ?: "Error de reproducción desconocido"
                        }
                        onPlaybackError?.invoke(error)
                    }
                }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            Log.d("VideoPlayer", "Player listener removed")
        }
    }

    // Ocultar overlay después de 3 segundos
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000)
            showOverlay = false
        }
    }

    // Mostrar overlay al entrar en fullscreen
    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            showOverlay = true
        }
    }

    // UI del reproductor
    Box(modifier = modifier) {
        // Mostrar el PlayerView solo si exoPlayer no es nulo y no hay error
        if (!hasError) {
            exoPlayer?.let { player ->
                Log.d("VideoPlayer", "Rendering PlayerView for player: $player")
                AndroidView(
                        factory = {
                            PlayerView(it).apply {
                                layoutParams =
                                        FrameLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                this.player = player
                                useController = false
                                keepScreenOn = true
                            }
                        },
                        update = { view -> view.player = player },
                        modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Indicador de buffering
        AnimatedVisibility(visible = isBuffering && !hasError, enter = fadeIn(), exit = fadeOut()) {
            Log.d("VideoPlayer", "Showing buffering indicator")
            Box(
                    modifier = Modifier.fillMaxSize().background(Color(0x66000000)),
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFFF5F5F5), strokeWidth = 3.dp) }
        }

        // Pantalla de error con botón reintentar
        AnimatedVisibility(visible = hasError, enter = fadeIn(), exit = fadeOut()) {
            Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xE6121212)),
                    contentAlignment = Alignment.Center
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                            text = errorMessage,
                            color = Color(0xFFF5F5F5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                    )
                    if (!channelName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                text = channelName,
                                color = Color(0xFFBDBDBD),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    var isRetryFocused by remember { mutableStateOf(false) }
                    Box(
                            modifier = Modifier
                                    .focusRequester(retryFocusRequester)
                                    .onFocusChanged { isRetryFocused = it.isFocused }
                                    // NO usar .focusable() aquí — .clickable() ya provee
                                    // focusability. Tener ambos crea DOS nodos de foco:
                                    // FocusRequester enfoca .focusable() pero Enter/OK
                                    // se maneja en el nodo de .clickable() (sin foco),
                                    // propagando al Box padre y toggleando fullscreen.
                                    .clickable {
                                        Log.d("VideoPlayer", "Retry button clicked for: $videoUrl")
                                        hasError = false
                                        isBuffering = true
                                        retryCount++
                                    }
                                    .background(
                                            if (isRetryFocused) Color(0xFF42A5F5) else Color(0xFF2196F3),
                                            RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                            width = if (isRetryFocused) 2.dp else 1.dp,
                                            color = if (isRetryFocused) Color.White else Color(0xFF64B5F6),
                                            shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = "Reintentar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                        )
                    }
                }
            }

            // Focus del botón reintentar — DENTRO del AnimatedVisibility
            // LaunchedEffect(Unit) se ejecuta cuando el contenido ENTRA en composición,
            // garantizando que el FocusRequester ya está adjunto al botón.
            // Reintenta múltiples veces para dispositivos lentos (ZTE zBox).
            LaunchedEffect(Unit) {
                // Esperar a que la animación fadeIn y el layout se estabilicen
                delay(600)
                yield()
                var focused = false
                repeat(5) { attempt ->
                    if (!focused) {
                        try {
                            retryFocusRequester.requestFocus()
                            focused = true
                            Log.d("VideoPlayer", "Retry button focused on attempt ${attempt + 1}")
                        } catch (e: Exception) {
                            Log.w("VideoPlayer", "Focus attempt ${attempt + 1} failed: ${e.message}")
                            delay(300)
                        }
                    }
                }
                if (!focused) {
                    Log.e("VideoPlayer", "Failed to focus retry button after 5 attempts")
                }
            }
        }

        // Overlay del canal / programa
        AnimatedVisibility(
                visible = showOverlay && !channelName.isNullOrBlank() && !hasError,
                enter = fadeIn(),
                exit = fadeOut()
        ) {
            Log.d("VideoPlayer", "Showing channel overlay: $channelName")
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(24.dp)
                                    .background(
                                            Color(0xFF121212).copy(alpha = 0.7f),
                                            RoundedCornerShape(8.dp)
                                    ),
                    contentAlignment = Alignment.CenterStart
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title (Channel or Program)
                    Text(
                            text =
                                    if (ENABLE_EPG_OVERLAY)
                                            currentProgram?.title ?: channelName ?: ""
                                    else channelName ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5F5F5),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )

                    if (ENABLE_EPG_OVERLAY && currentProgram != null) {
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                        val start = currentProgram.startTime.format(timeFormatter)
                        val end = currentProgram.endTime.format(timeFormatter)

                        Text(
                                text = "$start - $end",
                                fontSize = 16.sp,
                                color = Color(0xFFBDBDBD),
                                modifier = Modifier.padding(top = 4.dp)
                        )

                        if (!currentProgram.description.isNullOrBlank()) {
                            Text(
                                    text = currentProgram.description,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF5F5F5).copy(alpha = 0.8f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
