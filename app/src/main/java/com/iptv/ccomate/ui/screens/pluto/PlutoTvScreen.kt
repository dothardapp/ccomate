package com.iptv.ccomate.ui.screens.pluto

import android.util.Log
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iptv.ccomate.data.EPGRepository

import com.iptv.ccomate.data.M3UParser
import com.iptv.ccomate.data.NetworkClient
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.ui.screens.ChannelList
import com.iptv.ccomate.ui.screens.GroupList
import com.iptv.ccomate.ui.video.VideoPanel
import com.iptv.ccomate.util.AppConfig
import com.iptv.ccomate.util.LocalFullscreenState
import com.iptv.ccomate.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ENABLE_EPG_SIDE_PANEL = true

@Composable
fun PlutoTvScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fullscreenState = LocalFullscreenState.current
    val isFullscreen = fullscreenState.value

    // ── Estado de canales ──
    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var groups by remember { mutableStateOf<List<String>>(emptyList()) }
    var allChannels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var selectedChannelUrl by remember { mutableStateOf<String?>(null) }
    var lastClickedChannelUrl by remember { mutableStateOf<String?>(null) }

    // ── Estado de UI ──
    var statusMessage by remember { mutableStateOf("Inicializando...") }
    var playbackError by remember { mutableStateOf<Throwable?>(null) }
    var playerRestartKey by remember { mutableIntStateOf(0) }
    var restoreFocus by remember { mutableStateOf(false) }

    // ── Estado de tiempo ──
    val isTimeIncorrect = remember { !TimeUtils.isSystemTimeValid() }
    val currentTimeMessage = remember { TimeUtils.getSystemTimeMessage() }

    // ── Estado EPG ──
    var epgData by remember { mutableStateOf<Map<String, List<EPGProgram>>>(emptyMap()) }
    var currentProgram by remember { mutableStateOf<EPGProgram?>(null) }
    val epgRepository = remember { EPGRepository(context) }


    // ── Derivados ──
    val selectedGroup = groups.getOrNull(selectedGroupIndex)
    val filteredChannels = allChannels.filter { it.group == selectedGroup }
    val selectedChannel = allChannels.firstOrNull { it.url == selectedChannelUrl }
    val selectedChannelLogo = selectedChannel?.logo

    // ── Lifecycle: reiniciar player al volver ──
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                playerRestartKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Carga de canales M3U ──
    LaunchedEffect(Unit) {
        try {
            statusMessage = "Conectando con el servidor..."
            val channels =
                    withContext(Dispatchers.IO) {
                        val m3uContent = NetworkClient.fetchM3U(AppConfig.PLUTO_PLAYLIST_URL)
                        M3UParser.parse(m3uContent)
                    }
            statusMessage = "Procesando canales..."
            groups = channels.mapNotNull { it.group }.distinct()
            allChannels = channels
            statusMessage = "Listo. Se cargaron ${channels.size} canales."

            if (selectedChannelUrl == null) {
                val first = channels.firstOrNull()
                selectedChannelUrl = first?.url
                lastClickedChannelUrl = first?.url
            }
            playerRestartKey++
        } catch (e: Exception) {
            statusMessage = "❌ Error al cargar canales: ${e.localizedMessage ?: "desconocido"}"
            groups = listOf("Error al cargar")
            Log.e("PlutoScreen", "Error al cargar M3U", e)
        }
    }

    // ── Carga de EPG ──
    LaunchedEffect(Unit) {
        try {
            val parsedEpg = epgRepository.getEPGData()
            epgData = parsedEpg
            Log.d("PlutoTvScreen", "EPG Loaded: ${parsedEpg.size} channels")
        } catch (e: Exception) {
            Log.e("PlutoTvScreen", "Error loading EPG", e)
        }
    }


    // ── Actualizar programa actual ──
    LaunchedEffect(selectedChannel, epgData) {
        currentProgram =
                selectedChannel?.tvgId?.let { tvgId -> epgData[tvgId] }?.find { program ->
                    val now = java.time.ZonedDateTime.now()
                    now.isAfter(program.startTime) && now.isBefore(program.endTime)
                }
    }

    // ── Contenido de video reutilizable ──
    val videoContent =
            remember(playerRestartKey) {
                movableContentOf {
                        url: String?,
                        name: String?,
                        isFull: Boolean,
                        program: EPGProgram? ->
                    VideoContentBlock(
                            context = context,
                            videoUrl = url,
                            channelName = name,
                            isFullscreen = isFull,
                            currentProgram = program,
                            isTimeIncorrect = isTimeIncorrect,
                            currentTimeMessage = currentTimeMessage,
                            onPlaybackStarted = {
                                statusMessage = "🎬 Reproduciendo canal: ${name ?: "Canal"}"
                                playbackError = null
                            },
                            onPlaybackError = { error ->
                                playbackError = error
                                statusMessage =
                                        "❌ Error al reproducir: ${error.localizedMessage ?: "desconocido"}"
                                Log.e("VideoPanel", "Error de reproducción", error)
                            },
                            onToggleFullscreen = {
                                if (fullscreenState.value) {
                                    restoreFocus = true
                                    fullscreenState.value = false
                                } else {
                                    fullscreenState.value = true
                                }
                            }
                    )
                }
            }

    // ── Render ──
    if (isFullscreen) {
        PlutoFullscreenView(
                filteredChannels = filteredChannels,
                selectedChannelUrl = selectedChannelUrl,
                onChannelChanged = { channel ->
                    selectedChannelUrl = channel.url
                    lastClickedChannelUrl = channel.url
                },
                onExitFullscreen = {
                    restoreFocus = true
                    fullscreenState.value = false
                }
        ) { videoContent(selectedChannelUrl, selectedChannel?.name, true, currentProgram) }
    } else {
        PlutoNormalLayout(
                groups = groups,
                selectedGroupIndex = selectedGroupIndex,
                onGroupSelected = { selectedGroupIndex = it },
                filteredChannels = filteredChannels,
                selectedChannelUrl = selectedChannelUrl,
                lastClickedChannelUrl = lastClickedChannelUrl,
                selectedChannelLogo = selectedChannelLogo,
                statusMessage = statusMessage,
                playbackError = playbackError,
                currentProgram = currentProgram,
                restoreFocus = restoreFocus,
                onChannelSelected = { channel ->
                    selectedChannelUrl = channel.url
                    statusMessage = "🎬 Cargando canal: ${channel.name}..."
                    playbackError = null
                },
                onUpdateLastClicked = { lastClickedChannelUrl = it },
                onFullscreenRequest = { fullscreenState.value = true },
                onFocusRestored = { restoreFocus = false }
        ) { videoContent(selectedChannelUrl, selectedChannel?.name, false, currentProgram) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Composables internos extraídos
// ═══════════════════════════════════════════════════════════════

/** Bloque de video con overlay de advertencia de hora. */
@Composable
private fun VideoContentBlock(
        context: android.content.Context,
        videoUrl: String?,
        channelName: String?,
        isFullscreen: Boolean,
        currentProgram: EPGProgram?,
        isTimeIncorrect: Boolean,
        currentTimeMessage: String,
        onPlaybackStarted: () -> Unit,
        onPlaybackError: (Throwable) -> Unit,
        onToggleFullscreen: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().focusable().clickable { onToggleFullscreen() }) {
        VideoPanel(
                context = context,
                videoUrl = videoUrl,
                channelName = channelName,
                onPlaybackStarted = onPlaybackStarted,
                onPlaybackError = onPlaybackError,
                modifier = Modifier.fillMaxSize(),
                currentProgram = currentProgram,
                isFullscreen = isFullscreen
        )

        if (!isFullscreen) {
            TimeWarningBanner(
                    isVisible = isTimeIncorrect,
                    timeMessage = currentTimeMessage,
                    modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/** Vista fullscreen con navegación por D-Pad. */
@Composable
private fun PlutoFullscreenView(
        filteredChannels: List<Channel>,
        selectedChannelUrl: String?,
        onChannelChanged: (Channel) -> Unit,
        onExitFullscreen: () -> Unit,
        videoContent: @Composable () -> Unit
) {
    BackHandler { onExitFullscreen() }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(PlutoColors.FullscreenBackground)
                            .onKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown) {
                                    val currentIndex =
                                            filteredChannels.indexOfFirst {
                                                it.url == selectedChannelUrl
                                            }
                                    if (currentIndex != -1) {
                                        when (event.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_UP -> {
                                                val prevIndex =
                                                        if (currentIndex <= 0)
                                                                filteredChannels.size - 1
                                                        else currentIndex - 1
                                                onChannelChanged(filteredChannels[prevIndex])
                                                true
                                            }
                                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                val nextIndex =
                                                        (currentIndex + 1) % filteredChannels.size
                                                onChannelChanged(filteredChannels[nextIndex])
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                } else false
                            }
                            .focusable()
    ) { videoContent() }
}

/** Layout normal (no fullscreen) con video, info, grupos y canales. */
@Composable
private fun PlutoNormalLayout(
        groups: List<String>,
        selectedGroupIndex: Int,
        onGroupSelected: (Int) -> Unit,
        filteredChannels: List<Channel>,
        selectedChannelUrl: String?,
        lastClickedChannelUrl: String?,
        selectedChannelLogo: String?,
        statusMessage: String,
        playbackError: Throwable?,
        currentProgram: EPGProgram?,
        restoreFocus: Boolean,
        onChannelSelected: (Channel) -> Unit,
        onUpdateLastClicked: (String) -> Unit,
        onFullscreenRequest: () -> Unit,
        onFocusRestored: () -> Unit,
        videoContent: @Composable () -> Unit
) {
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(PlutoColors.ScreenGradient)
                            .padding(
                                    horizontal = 48.dp,
                                    vertical = 27.dp
                            ) // Overscan zona segura 5%
    ) {
        // ── Fila superior: Video + Info ──
        Row(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
            // Panel de video
            StyledPanelBox(
                    modifier = Modifier.weight(1f).padding(8.dp),
                    gradient = PlutoColors.VideoContainerGradient
            ) { videoContent() }

            // Panel de información del canal
            Box(modifier = Modifier.weight(1.6f).padding(8.dp)) {
                ChannelInfoPanel(
                        channelLogo = selectedChannelLogo,
                        statusMessage = statusMessage,
                        playbackError = playbackError,
                        currentProgram = currentProgram,
                        showEpg = ENABLE_EPG_SIDE_PANEL
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = PlutoColors.DividerColor)
        Spacer(modifier = Modifier.height(8.dp))

        // ── Fila inferior: Grupos + Canales ──
        Row(modifier = Modifier.weight(1.6f).fillMaxSize().padding(8.dp)) {
            // Lista de grupos
            StyledPanelBox(modifier = Modifier.weight(1f)) {
                GroupList(
                        groups = groups,
                        selectedIndex = selectedGroupIndex,
                        onSelect = onGroupSelected
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Lista de canales
            StyledPanelBox(modifier = Modifier.weight(2f)) {
                ChannelList(
                        channels = filteredChannels,
                        selectedUrl = selectedChannelUrl,
                        lastClickedUrl = lastClickedChannelUrl,
                        onUpdateLastClicked = onUpdateLastClicked,
                        onSelect = onChannelSelected,
                        onFullscreenRequest = onFullscreenRequest,
                        restoreFocus = restoreFocus,
                        onFocusRestored = onFocusRestored
                )
            }
        }
    }
}
