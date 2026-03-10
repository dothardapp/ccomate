package com.iptv.ccomate.ui.screens.pluto

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.ui.screens.ChannelList
import com.iptv.ccomate.ui.screens.GroupList
import com.iptv.ccomate.ui.video.FullscreenDPadContainer
import com.iptv.ccomate.ui.video.VideoPanel
import com.iptv.ccomate.util.LocalFullscreenState
import com.iptv.ccomate.util.TimeUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val ENABLE_EPG_SIDE_PANEL = true

@Composable
fun PlutoTvScreen(
    viewModel: PlutoTvViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fullscreenState = LocalFullscreenState.current
    val isFullscreen = fullscreenState.value

    // Observar el estado desde el ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // -- Estado de UI local --
    var playerRestartKey by remember { mutableIntStateOf(0) }
    var restoreFocus by remember { mutableStateOf(false) }

    // FocusRequesters para navegacion D-Pad entre listas
    val groupListFocusRequester = remember { FocusRequester() }
    val channelListFocusRequester = remember { FocusRequester() }

    // -- Estado de tiempo --
    val isTimeIncorrect = remember { !TimeUtils.isSystemTimeValid() }
    val currentTimeMessage = remember { TimeUtils.getSystemTimeMessage() }

    // -- Derivados --
    val selectedGroup = uiState.groups.getOrNull(uiState.selectedGroupIndex)
    val filteredChannels = uiState.allChannels.filter { it.group == selectedGroup }
    val selectedChannel = uiState.allChannels.firstOrNull { it.url == uiState.selectedChannelUrl }
    val selectedChannelLogo = selectedChannel?.logo

    // -- Lifecycle: reiniciar player al volver --
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                playerRestartKey++
                viewModel.updateCurrentProgram()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Estado de error del player (para FullscreenDPadContainer)
    var hasPlayerError by remember { mutableStateOf(false) }

    // movableContentOf: permite mover el VideoPanel entre layout normal y fullscreen
    // sin destruir/recrear el ExoPlayer. SIN .clickable (no agrega nodo de foco).
    val videoContent =
            remember {
                movableContentOf {
                        url: String?,
                        name: String?,
                        isFull: Boolean,
                        program: EPGProgram? ->
                    VideoPanel(
                            context = context,
                            videoUrl = url,
                            channelName = name,
                            onPlaybackStarted = {
                                viewModel.onPlaybackStarted(name)
                            },
                            onPlaybackError = { error ->
                                viewModel.onPlaybackError(error)
                                Log.e("VideoPanel", "Error de reproduccion", error)
                            },
                            modifier = Modifier.fillMaxSize(),
                            onErrorStateChanged = { hasPlayerError = it },
                            currentProgram = program,
                            isFullscreen = isFull
                    )
                }
            }

    // -- Render --
    if (isFullscreen) {
        FullscreenDPadContainer(
                channels = filteredChannels,
                selectedChannelUrl = uiState.selectedChannelUrl,
                hasPlayerError = hasPlayerError,
                backgroundColor = PlutoColors.FullscreenBackground,
                onChannelChanged = { channel ->
                    viewModel.selectChannel(channel)
                    viewModel.updateLastClickedChannel(channel.url)
                },
                onExitFullscreen = {
                    restoreFocus = true
                    fullscreenState.value = false
                }
        ) {
            videoContent(uiState.selectedChannelUrl, selectedChannel?.name, true, uiState.currentProgram)
        }
    } else {
        PlutoNormalLayout(
                groups = uiState.groups,
                selectedGroupIndex = uiState.selectedGroupIndex,
                onGroupSelected = { viewModel.selectGroup(it) },
                filteredChannels = filteredChannels,
                selectedChannelUrl = uiState.selectedChannelUrl,
                lastClickedChannelUrl = uiState.lastClickedChannelUrl,
                selectedChannelLogo = selectedChannelLogo,
                statusMessage = uiState.statusMessage,
                playbackError = uiState.playbackError,
                currentProgram = uiState.currentProgram,
                restoreFocus = restoreFocus,
                isTimeIncorrect = isTimeIncorrect,
                currentTimeMessage = currentTimeMessage,
                onChannelSelected = { channel ->
                    viewModel.selectChannel(channel)
                },
                onUpdateLastClicked = { viewModel.updateLastClickedChannel(it) },
                onFullscreenRequest = { fullscreenState.value = true },
                onFocusRestored = { restoreFocus = false },
                groupListFocusRequester = groupListFocusRequester,
                channelListFocusRequester = channelListFocusRequester,
                isLoading = uiState.isLoading
        ) {
            videoContent(uiState.selectedChannelUrl, selectedChannel?.name, false, uiState.currentProgram)
        }
    }
}

// ================================================================
//  Composables internos
// ================================================================

/** Layout normal (no fullscreen) con video, info, grupos y canales. */
@Composable
private fun PlutoNormalLayout(
        groups: List<String>,
        selectedGroupIndex: Int,
        onGroupSelected: (Int) -> Unit,
        filteredChannels: List<com.iptv.ccomate.model.Channel>,
        selectedChannelUrl: String?,
        lastClickedChannelUrl: String?,
        selectedChannelLogo: String?,
        statusMessage: String,
        playbackError: Throwable?,
        currentProgram: EPGProgram?,
        restoreFocus: Boolean,
        isTimeIncorrect: Boolean,
        currentTimeMessage: String,
        onChannelSelected: (com.iptv.ccomate.model.Channel) -> Unit,
        onUpdateLastClicked: (String) -> Unit,
        onFullscreenRequest: () -> Unit,
        onFocusRestored: () -> Unit,
        groupListFocusRequester: FocusRequester = remember { FocusRequester() },
        channelListFocusRequester: FocusRequester = remember { FocusRequester() },
        isLoading: Boolean = false,
        videoContent: @Composable () -> Unit
) {
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(PlutoColors.ScreenGradient)
                            .padding(
                                    horizontal = 48.dp,
                                    vertical = 27.dp
                            )
    ) {
        // -- Fila superior: Video + Info --
        Row(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
            // Panel de video
            StyledPanelBox(
                    modifier = Modifier.weight(1f).padding(8.dp),
                    gradient = PlutoColors.VideoContainerGradient
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    videoContent()

                    TimeWarningBanner(
                            isVisible = isTimeIncorrect,
                            timeMessage = currentTimeMessage,
                            modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            // Panel de informacion del canal
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

        // -- Fila inferior: Grupos + Canales --
        Row(modifier = Modifier.weight(1.6f).fillMaxSize().padding(8.dp)) {
            // Lista de grupos
            StyledPanelBox(modifier = Modifier.weight(1f).focusRequester(groupListFocusRequester)) {
                GroupList(
                        groups = groups,
                        selectedIndex = selectedGroupIndex,
                        onSelect = onGroupSelected,
                        onNavigateToChannels = {
                            try { channelListFocusRequester.requestFocus() } catch (_: Exception) {}
                        },
                        isLoading = isLoading
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Lista de canales
            StyledPanelBox(modifier = Modifier.weight(2f).focusRequester(channelListFocusRequester)) {
                ChannelList(
                        channels = filteredChannels,
                        selectedUrl = selectedChannelUrl,
                        lastClickedUrl = lastClickedChannelUrl,
                        onUpdateLastClicked = onUpdateLastClicked,
                        onSelect = onChannelSelected,
                        onFullscreenRequest = onFullscreenRequest,
                        onNavigateToGroups = {
                            try { groupListFocusRequester.requestFocus() } catch (_: Exception) {}
                        },
                        restoreFocus = restoreFocus,
                        onFocusRestored = onFocusRestored,
                        isLoading = isLoading
                )
            }
        }
    }
}
