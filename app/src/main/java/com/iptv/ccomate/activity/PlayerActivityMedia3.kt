package com.iptv.ccomate.activity

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class PlayerActivityMedia3 : ComponentActivity() {
    private val viewModel: VideoPlayerViewModel by viewModels()
    private var currentChannelIndex by mutableIntStateOf(0)
    private var channelList by mutableStateOf(emptyList<Channel>())
    private var pendingChannelIndex by mutableIntStateOf(-1) // Para debounce del cambio de canal
    private var displayChannelIndex by mutableIntStateOf(-1) // Para overlay inmediato

    companion object {
        private const val KEY_CURRENT_CHANNEL_INDEX = "current_channel_index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Restaurar currentChannelIndex desde savedInstanceState si existe
        currentChannelIndex = savedInstanceState?.getInt(KEY_CURRENT_CHANNEL_INDEX, 0) ?: 0
        displayChannelIndex = currentChannelIndex // Inicializar displayChannelIndex

        val initialUrl = intent.getStringExtra("url")
        val initialName = intent.getStringExtra("name") ?: "Canal"
        val initialLogo = intent.getStringExtra("logo")
        val channels = intent.getParcelableArrayListExtra<Channel>("channels") ?: emptyList()

        if (initialUrl.isNullOrEmpty() || !(initialUrl.startsWith("http://") || initialUrl.startsWith("https://"))) {
            Log.w("PlayerActivityMedia3", "URL inválida: $initialUrl")
            finish()
            return
        }

        channelList = channels
        // Solo actualizar currentChannelIndex si es el primer inicio
        if (savedInstanceState == null) {
            currentChannelIndex = channels.indexOfFirst { it.url == initialUrl }.coerceAtLeast(0)
            displayChannelIndex = currentChannelIndex
        }

        // Configurar estados para el composable
        var playerState by mutableStateOf<ExoPlayer?>(null)
        var channelNameState by mutableStateOf(initialName)
        var channelLogoState by mutableStateOf(initialLogo)
        var errorState by mutableStateOf<String?>(null)
        var isBuffering by mutableStateOf(true) // Forzar buffering al inicio

        setContent {
            if (errorState != null) {
                ErrorScreen(message = errorState!!) {
                    // Reintentar con el mismo canal
                    isBuffering = true // Mostrar rueda al reintentar
                    loadChannel(
                        channelList.getOrNull(currentChannelIndex)?.url,
                        channelList.getOrNull(currentChannelIndex)?.name,
                        channelList.getOrNull(currentChannelIndex)?.logo,
                        onSuccess = { player, name, logo ->
                            playerState = player
                            channelNameState = name
                            channelLogoState = logo
                            errorState = null
                            isBuffering = false
                        },
                        onFailure = { message ->
                            errorState = message
                            isBuffering = false
                        }
                    )
                }
            } else {
                PlayerScreen(
                    player = playerState,
                    channelName = channelNameState,
                    channelLogo = channelLogoState,
                    channels = channelList,
                    displayChannelIndexState = displayChannelIndex,
                    pendingChannelIndexState = pendingChannelIndex,
                    onChannelChange = { newIndex ->
                        currentChannelIndex = newIndex
                        displayChannelIndex = newIndex
                        pendingChannelIndex = -1 // Resetear después del cambio
                        Log.d("PlayerActivityMedia3", "Channel changed to index: $newIndex")
                        isBuffering = true // Mostrar rueda al cambiar canal
                        loadChannel(
                            channelList.getOrNull(newIndex)?.url,
                            channelList.getOrNull(newIndex)?.name,
                            channelList.getOrNull(newIndex)?.logo,
                            onSuccess = { player, name, logo ->
                                playerState = player
                                channelNameState = name
                                channelLogoState = logo
                                errorState = null
                                isBuffering = false
                            },
                            onFailure = { message ->
                                errorState = message
                                isBuffering = false
                            }
                        )
                    }
                )
            }
        }

        // Cargar el canal inicial
        isBuffering = true // Mostrar rueda al inicio
        loadChannel(
            channelList.getOrNull(currentChannelIndex)?.url ?: initialUrl,
            channelList.getOrNull(currentChannelIndex)?.name ?: initialName,
            channelList.getOrNull(currentChannelIndex)?.logo ?: initialLogo,
            onSuccess = { player, name, logo ->
                playerState = player
                channelNameState = name
                channelLogoState = logo
                errorState = null
                isBuffering = false
            },
            onFailure = { message ->
                errorState = message
                isBuffering = false
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_CHANNEL_INDEX, currentChannelIndex)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_CHANNEL_UP -> {
                    if (displayChannelIndex > 0) {
                        displayChannelIndex-- // Actualizar overlay inmediatamente
                        pendingChannelIndex = displayChannelIndex // Actualizar para debounce
                        Log.d("PlayerActivityMedia3", "Pending channel index: $pendingChannelIndex, Display: $displayChannelIndex")
                        return true
                    }
                }
                KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                    if (displayChannelIndex < channelList.size - 1) {
                        displayChannelIndex++ // Actualizar overlay inmediatamente
                        pendingChannelIndex = displayChannelIndex // Actualizar para debounce
                        Log.d("PlayerActivityMedia3", "Pending channel index: $pendingChannelIndex, Display: $displayChannelIndex")
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun loadChannel(
        url: String?,
        name: String?,
        logo: String?,
        onSuccess: (ExoPlayer, String, String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (url.isNullOrEmpty()) {
            Log.w("PlayerActivityMedia3", "URL para cargar canal es nula o vacía")
            onFailure("URL para cargar canal es nula o vacía")
            return
        }
        lifecycleScope.launch {
            viewModel.setPlayer(this@PlayerActivityMedia3, url).onSuccess { player ->
                onSuccess(player, name ?: "Canal", logo)
            }.onFailure { e ->
                Log.e("PlayerActivityMedia3", "Error al cargar canal ($name): ${e.message}")
                onFailure("Error al cargar el canal ${name ?: ""}: ${e.message}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumePlayer()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch { viewModel.releasePlayer() }
    }

    @Composable
    fun PlayerScreen(
        player: ExoPlayer?,
        channelName: String,
        channelLogo: String?,
        channels: List<Channel>,
        displayChannelIndexState: Int,
        pendingChannelIndexState: Int,
        onChannelChange: (Int) -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        var isBuffering by remember { mutableStateOf(true) } // Inicializar en true
        var showOverlay by remember { mutableStateOf(true) } // Mostrar overlay al inicio
        var playerError by remember { mutableStateOf<PlaybackException?>(null) }
        var playerView by remember { mutableStateOf<PlayerView?>(null) }
        var debounceJob by remember { mutableStateOf<Job?>(null) }

        // Usar displayChannelIndexState para el overlay
        val displayChannelName = if (displayChannelIndexState in channels.indices) {
            channels[displayChannelIndexState].name
        } else {
            channelName
        }
        val displayChannelLogo = if (displayChannelIndexState in channels.indices) {
            channels[displayChannelIndexState].logo
        } else {
            channelLogo
        }

        // Manejar el debounce para el cambio de canal
        LaunchedEffect(pendingChannelIndexState) {
            if (pendingChannelIndexState in channels.indices) {
                showOverlay = true
                Log.d("PlayerScreen", "Overlay activated for pending index: $pendingChannelIndexState")
                debounceJob?.cancel()
                debounceJob = coroutineScope.launch {
                    delay(1500) // Esperar 1.5 segundos
                    if (pendingChannelIndexState in channels.indices) {
                        onChannelChange(pendingChannelIndexState)
                        showOverlay = true // Mostrar overlay de reproducción
                    }
                }
            }
        }

        // Ocultar el overlay después de 5 segundos si no hay cambio pendiente
        LaunchedEffect(showOverlay, pendingChannelIndexState) {
            if (showOverlay && pendingChannelIndexState == -1) {
                delay(5000)
                showOverlay = false
                Log.d("PlayerScreen", "Overlay hidden after 5 seconds")
            }
        }

        DisposableEffect(player) {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    Log.d("PlayerScreen", "Playback state changed: $state")
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            isBuffering = true
                            Log.d("PlayerScreen", "Buffering state: isBuffering = true")
                        }
                        Player.STATE_READY -> {
                            isBuffering = false
                            showOverlay = true
                            Log.d("PlayerScreen", "Ready state: isBuffering = false, showOverlay = true")
                        }
                        Player.STATE_ENDED -> {
                            isBuffering = false
                            Log.d("PlayerScreen", "Ended state: isBuffering = false")
                        }
                        Player.STATE_IDLE -> {
                            isBuffering = true
                            Log.d("PlayerScreen", "Idle state: isBuffering = true")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("PlayerScreen", "Error de reproducción: ${error.message}", error)
                    isBuffering = false
                    showOverlay = false
                    playerError = error
                }
            }

            player?.addListener(listener)

            onDispose {
                player?.removeListener(listener)
                debounceJob?.cancel()
                Log.d("PlayerScreen", "Player listener and debounce job removed")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            player?.let { p ->
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            this.player = p
                            useController = false
                            keepScreenOn = true
                            playerView = this
                        }
                    },
                    update = { view ->
                        if (view.player != p) {
                            view.player = p
                            Log.d("PlayerScreen", "PlayerView updated with new player: $p")
                            p.prepare()
                            p.playWhenReady = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            AnimatedVisibility(
                visible = isBuffering,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Log.d("PlayerScreen", "Showing buffering indicator")
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
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
            ) {
                Log.d("PlayerScreen", "Showing channel overlay: $displayChannelName")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayChannelLogo?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Logo del canal $displayChannelName",
                            modifier = Modifier
                                .size(width = 80.dp, height = 45.dp)
                                .background(Color.Black)
                                .padding(end = 12.dp)
                        )
                    }

                    Text(
                        text = if (pendingChannelIndexState in channels.indices) {
                            "🔜 Seleccionando canal: $displayChannelName"
                        } else {
                            "🎬 Reproduciendo canal: $displayChannelName"
                        },
                        fontSize = 18.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (playerError != null) {
                AlertDialog(
                    onDismissRequest = { playerError = null },
                    title = { Text("Error de Reproducción") },
                    text = { Text(playerError?.localizedMessage ?: "Ocurrió un error al reproducir el contenido.") },
                    confirmButton = {
                        TextButton(onClick = { playerError = null }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text("Salir")
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun ErrorScreen(message: String, onRetry: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "¡Oops! Ha ocurrido un error.",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = message,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(24.dp))
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        }
    }
}