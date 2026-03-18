package com.iptv.ccomate.ui.screens.tda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.ui.screens.base.ChannelScreen
import com.iptv.ccomate.ui.screens.pluto.ChannelInfoPanel
import com.iptv.ccomate.ui.screens.pluto.PlutoColors

@Composable
fun TDAScreen(
    viewModel: TdaViewModel = hiltViewModel()
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
        infoPanelContent = { selectedChannel ->
            ChannelInfoPanel(
                channelLogo = selectedChannel?.logo,
                statusMessage = uiState.statusMessage,
                playbackError = uiState.playbackError,
                currentProgram = null,
                showEpg = false
            )
        }
    )
}
