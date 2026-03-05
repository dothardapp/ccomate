package com.iptv.ccomate.ui.screens.tda

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.M3UParser
import com.iptv.ccomate.data.NetworkClient
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.util.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class TdaUiState(
    val groups: List<String> = emptyList(),
    val allChannels: List<Channel> = emptyList(),
    val selectedGroupIndex: Int = 0,
    val selectedChannelUrl: String? = null,
    val selectedChannelName: String? = null,
    val lastClickedChannelUrl: String? = null,
    val statusMessage: String = "Inicializando TDA...",
    val playbackError: Throwable? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class TdaViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(TdaUiState())
    val uiState: StateFlow<TdaUiState> = _uiState.asStateFlow()

    init {
        loadChannels()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            try {
                _uiState.compareAndSet(_uiState.value, _uiState.value.copy(statusMessage = "Conectando con TDA...", isLoading = true))
                
                val channels = withContext(Dispatchers.IO) {
                    val m3uContent = NetworkClient.fetchM3U(AppConfig.TDA_PLAYLIST_URL)
                    M3UParser.parse(m3uContent)
                }

                val groups = channels.mapNotNull { it.group }.distinct()
                val firstChannel = channels.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    allChannels = channels,
                    groups = groups,
                    statusMessage = "✅ Listo. Se cargaron ${channels.size} canales TDA.",
                    selectedChannelUrl = _uiState.value.selectedChannelUrl ?: firstChannel?.url,
                    selectedChannelName = _uiState.value.selectedChannelName ?: firstChannel?.name,
                    lastClickedChannelUrl = _uiState.value.lastClickedChannelUrl ?: firstChannel?.url,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("TdaViewModel", "Error loading M3U", e)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "❌ Error al cargar canales TDA: ${e.localizedMessage ?: "desconocido"}",
                    groups = listOf("Error al cargar"),
                    isLoading = false
                )
            }
        }
    }

    fun selectGroup(index: Int) {
        _uiState.value = _uiState.value.copy(selectedGroupIndex = index)
    }

    fun selectChannel(channel: Channel) {
        _uiState.value = _uiState.value.copy(
            selectedChannelUrl = channel.url,
            selectedChannelName = channel.name,
            statusMessage = "🎬 Cargando canal: ${channel.name}...",
            playbackError = null
        )
    }

    fun updateLastClickedChannel(url: String) {
        _uiState.value = _uiState.value.copy(lastClickedChannelUrl = url)
    }

    fun onPlaybackStarted(channelName: String?) {
        _uiState.value = _uiState.value.copy(
            statusMessage = "🎬 Reproduciendo canal: ${channelName ?: "Canal"}",
            playbackError = null,
            isPlaying = true
        )
    }

    fun onPlaybackError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            playbackError = error,
            statusMessage = "❌ Error al reproducir: ${error.localizedMessage ?: "desconocido"}",
            isPlaying = false
        )
    }
}
