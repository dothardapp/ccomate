package com.iptv.ccomate.ui.screens.pluto

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.data.EPGRepository
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.util.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class PlutoUiState(
    val groups: List<String> = emptyList(),
    val allChannels: List<Channel> = emptyList(),
    val selectedGroupIndex: Int = 0,
    val selectedChannelUrl: String? = null,
    val lastClickedChannelUrl: String? = null,
    val statusMessage: String = "Inicializando...",
    val playbackError: Throwable? = null,
    val epgData: Map<String, List<EPGProgram>> = emptyMap(),
    val currentProgram: EPGProgram? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class PlutoTvViewModel @Inject constructor(
    private val epgRepository: EPGRepository,
    private val channelRepository: ChannelRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlutoUiState())
    val uiState: StateFlow<PlutoUiState> = _uiState.asStateFlow()

    init {
        loadChannels()
        loadEPG()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(statusMessage = "Cargando canales...", isLoading = true)

                val channels = withContext(Dispatchers.IO) {
                    channelRepository.getChannels("PLUTO", AppConfig.PLUTO_PLAYLIST_URL)
                }

                val groups = channels.mapNotNull { it.group }.distinct()
                val firstChannel = channels.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    allChannels = channels,
                    groups = groups,
                    statusMessage = "Listo. Se cargaron ${channels.size} canales.",
                    selectedChannelUrl = _uiState.value.selectedChannelUrl ?: firstChannel?.url,
                    lastClickedChannelUrl = _uiState.value.lastClickedChannelUrl ?: firstChannel?.url,
                    isLoading = false
                )

                updateCurrentProgram()
            } catch (e: Exception) {
                Log.e("PlutoTvViewModel", "Error loading channels", e)
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Error al cargar canales: ${e.localizedMessage ?: "desconocido"}",
                    groups = listOf("Error al cargar"),
                    isLoading = false
                )
            }
        }
    }

    private fun loadEPG() {
        viewModelScope.launch {
            try {
                val parsedEpg = withContext(Dispatchers.IO) {
                    epgRepository.getEPGData()
                }
                _uiState.value = _uiState.value.copy(epgData = parsedEpg)
                Log.d("PlutoTvViewModel", "EPG Loaded: ${parsedEpg.size} channels")
                updateCurrentProgram()
            } catch (e: Exception) {
                Log.e("PlutoTvViewModel", "Error loading EPG", e)
            }
        }
    }

    fun selectGroup(index: Int) {
        _uiState.value = _uiState.value.copy(selectedGroupIndex = index)
    }

    fun selectChannel(channel: Channel) {
        _uiState.value = _uiState.value.copy(
            selectedChannelUrl = channel.url,
            statusMessage = "🎬 Cargando canal: ${channel.name}...",
            playbackError = null
        )
        updateCurrentProgram()
    }

    fun updateLastClickedChannel(url: String) {
        _uiState.value = _uiState.value.copy(lastClickedChannelUrl = url)
    }

    fun onPlaybackStarted(channelName: String?) {
        _uiState.value = _uiState.value.copy(
            statusMessage = "🎬 Reproduciendo canal: ${channelName ?: "Canal"}",
            playbackError = null
        )
    }

    fun onPlaybackError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            playbackError = error,
            statusMessage = "❌ Error al reproducir: ${error.localizedMessage ?: "desconocido"}"
        )
    }

    fun updateCurrentProgram() {
        val state = _uiState.value
        val selectedChannel = state.allChannels.firstOrNull { it.url == state.selectedChannelUrl }
        
        val program = selectedChannel?.tvgId?.let { tvgId -> state.epgData[tvgId] }?.find { p ->
            val now = ZonedDateTime.now()
            now.isAfter(p.startTime) && now.isBefore(p.endTime)
        }
        
        _uiState.value = _uiState.value.copy(currentProgram = program)
    }
}
