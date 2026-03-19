package com.iptv.ccomate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ChannelListViewModel(
    protected val channelRepository: ChannelRepository
) : ViewModel() {

    abstract val sourceName: String
    abstract val playlistUrl: String

    protected val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState.asStateFlow()

    protected fun initialize() {
        loadChannels()
        loadExtraData()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Cargando canales $sourceName...",
                    isLoading = true
                )

                val channels = withContext(Dispatchers.IO) {
                    channelRepository.getChannels(sourceName, playlistUrl)
                }

                val groups = channels.mapNotNull { it.group }.distinct()
                val firstChannel = channels.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    allChannels = channels,
                    groups = groups,
                    statusMessage = "Listo. Se cargaron ${channels.size} canales $sourceName.",
                    selectedChannelUrl = _uiState.value.selectedChannelUrl ?: firstChannel?.url,
                    selectedChannelName = _uiState.value.selectedChannelName ?: firstChannel?.name,
                    lastClickedChannelUrl = _uiState.value.lastClickedChannelUrl ?: firstChannel?.url,
                    isLoading = false
                )

                onChannelsLoaded()
            } catch (e: Exception) {
                Log.e(sourceName, "Error loading channels", e)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Error al cargar canales $sourceName: ${e.localizedMessage ?: "desconocido"}",
                    groups = listOf("Error al cargar"),
                    isLoading = false
                )
            }
        }
    }

    protected open fun loadExtraData() {}

    protected open fun onChannelsLoaded() {}

    protected open fun onChannelSelected() {}

    fun selectGroup(index: Int) {
        _uiState.value = _uiState.value.copy(selectedGroupIndex = index)
    }

    fun selectChannel(channel: Channel) {
        _uiState.value = _uiState.value.copy(
            selectedChannelUrl = channel.url,
            selectedChannelName = channel.name,
            statusMessage = "Cargando canal: ${channel.name}...",
            playbackError = null
        )
        onChannelSelected()
    }

    fun updateLastClickedChannel(url: String) {
        _uiState.value = _uiState.value.copy(lastClickedChannelUrl = url)
    }

    fun onPlaybackStarted(channelName: String?) {
        _uiState.value = _uiState.value.copy(
            statusMessage = "Reproduciendo canal: ${channelName ?: "Canal"}",
            playbackError = null,
            isPlaying = true
        )
    }

    fun onPlaybackError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            playbackError = error,
            statusMessage = "Error al reproducir: ${error.localizedMessage ?: "desconocido"}",
            isPlaying = false
        )
    }
}
