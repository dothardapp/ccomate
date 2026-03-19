package com.iptv.ccomate.ui.screens.base

import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.model.EPGProgram

data class ChannelUiState(
    val groups: List<String> = emptyList(),
    val allChannels: List<Channel> = emptyList(),
    val selectedGroupIndex: Int = 0,
    val selectedChannelUrl: String? = null,
    val selectedChannelName: String? = null,
    val lastClickedChannelUrl: String? = null,
    val statusMessage: String = "Inicializando...",
    val playbackError: Throwable? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentProgram: EPGProgram? = null
)
