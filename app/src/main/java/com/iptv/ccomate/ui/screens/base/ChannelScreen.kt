package com.iptv.ccomate.ui.screens.base

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.ui.screens.ChannelList
import com.iptv.ccomate.ui.screens.GroupList
import com.iptv.ccomate.ui.screens.pluto.PlutoColors
import com.iptv.ccomate.ui.screens.pluto.StyledPanelBox
import com.iptv.ccomate.ui.screens.pluto.TimeWarningBanner
import com.iptv.ccomate.ui.video.FullscreenDPadContainer
import com.iptv.ccomate.ui.video.VideoPanel
import com.iptv.ccomate.util.LocalFullscreenState
import com.iptv.ccomate.util.TimeUtils

@Composable
fun ChannelScreen(
    uiState: ChannelUiState,
    onSelectGroup: (Int) -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onUpdateLastClicked: (String) -> Unit,
    onPlaybackStarted: (String?) -> Unit,
    onPlaybackError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    screenGradient: Brush = PlutoColors.ScreenGradient,
    fullscreenBackground: Color = PlutoColors.FullscreenBackground,
    onResume: (() -> Unit)? = null,
    infoPanelContent: @Composable (Channel?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val fullscreenState = LocalFullscreenState.current
    val isFullscreen = fullscreenState.value

    // -- Estado de UI local --
    var playerRestartKey by remember { mutableIntStateOf(0) }
    var restoreFocus by remember { mutableStateOf(false) }
    var hasPlayerError by remember { mutableStateOf(false) }

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
                onResume?.invoke()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // movableContentOf: permite mover el VideoPanel entre layout normal y fullscreen
    // sin destruir/recrear el ExoPlayer
    val videoContent = remember {
        movableContentOf { url: String?, name: String?, isFull: Boolean, program: EPGProgram? ->
            VideoPanel(
                videoUrl = url,
                channelName = name,
                onPlaybackStarted = { onPlaybackStarted(name) },
                onPlaybackError = { error ->
                    onPlaybackError(error)
                    Log.e("ChannelScreen", "Error de reproduccion", error)
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
            backgroundColor = fullscreenBackground,
            onChannelChanged = { channel ->
                onSelectChannel(channel)
                onUpdateLastClicked(channel.url)
            },
            onExitFullscreen = {
                restoreFocus = true
                fullscreenState.value = false
            }
        ) {
            videoContent(uiState.selectedChannelUrl, selectedChannel?.name, true, uiState.currentProgram)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(screenGradient)
                .padding(horizontal = 48.dp, vertical = 27.dp)
        ) {
            // -- Fila superior: Video + Info --
            Row(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
                // Panel de video
                StyledPanelBox(
                    modifier = Modifier.weight(1f).padding(8.dp),
                    gradient = PlutoColors.VideoContainerGradient
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        videoContent(uiState.selectedChannelUrl, selectedChannel?.name, false, uiState.currentProgram)

                        TimeWarningBanner(
                            isVisible = isTimeIncorrect,
                            timeMessage = currentTimeMessage,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }

                // Panel de informacion del canal (slot)
                Box(modifier = Modifier.weight(1.6f).padding(8.dp)) {
                    infoPanelContent(selectedChannel)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = PlutoColors.DividerColor)
            Spacer(modifier = Modifier.height(8.dp))

            // -- Fila inferior: Grupos + Canales --
            Row(modifier = Modifier.weight(1.6f).fillMaxSize().padding(8.dp)) {
                StyledPanelBox(modifier = Modifier.weight(1f).focusRequester(groupListFocusRequester)) {
                    GroupList(
                        groups = uiState.groups,
                        selectedIndex = uiState.selectedGroupIndex,
                        onSelect = onSelectGroup,
                        onNavigateToChannels = {
                            try { channelListFocusRequester.requestFocus() } catch (_: Exception) {}
                        },
                        isLoading = uiState.isLoading
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                StyledPanelBox(modifier = Modifier.weight(2f).focusRequester(channelListFocusRequester)) {
                    ChannelList(
                        channels = filteredChannels,
                        selectedUrl = uiState.selectedChannelUrl,
                        lastClickedUrl = uiState.lastClickedChannelUrl,
                        onUpdateLastClicked = onUpdateLastClicked,
                        onSelect = onSelectChannel,
                        onFullscreenRequest = { fullscreenState.value = true },
                        onNavigateToGroups = {
                            try { groupListFocusRequester.requestFocus() } catch (_: Exception) {}
                        },
                        restoreFocus = restoreFocus,
                        onFocusRestored = { restoreFocus = false },
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
    }
}
