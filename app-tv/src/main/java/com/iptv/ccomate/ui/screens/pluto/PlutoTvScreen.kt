package com.iptv.ccomate.ui.screens.pluto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.ui.screens.base.ChannelScreen
import com.iptv.ccomate.viewmodel.PlutoTvViewModel

private const val ENABLE_EPG_SIDE_PANEL = true

@Composable
fun PlutoTvScreen(
    viewModel: PlutoTvViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChannelScreen(
        uiState = uiState,
        onSelectGroup = viewModel::selectGroup,
        onSelectChannel = viewModel::selectChannel,
        onUpdateLastClicked = viewModel::updateLastClickedChannel,
        onPlaybackStarted = viewModel::onPlaybackStarted,
        onPlaybackError = viewModel::onPlaybackError,
        screenGradient = PlutoColors.ScreenGradient,
        fullscreenBackground = PlutoColors.FullscreenBackground,
        onResume = { viewModel.updateCurrentProgram() },
        infoPanelContent = { selectedChannel ->
            ChannelInfoPanel(
                channelLogo = selectedChannel?.logo,
                statusMessage = uiState.statusMessage,
                playbackError = uiState.playbackError,
                currentProgram = uiState.currentProgram,
                showEpg = ENABLE_EPG_SIDE_PANEL
            )
        }
    )
}
