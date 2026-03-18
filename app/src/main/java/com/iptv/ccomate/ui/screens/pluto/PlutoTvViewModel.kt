package com.iptv.ccomate.ui.screens.pluto

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.data.EPGRepository
import com.iptv.ccomate.ui.screens.base.ChannelListViewModel
import com.iptv.ccomate.util.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class PlutoTvViewModel @Inject constructor(
    private val epgRepository: EPGRepository,
    channelRepository: ChannelRepository
) : ChannelListViewModel(channelRepository) {

    override val sourceName = "PLUTO"
    override val playlistUrl = AppConfig.PLUTO_PLAYLIST_URL

    init {
        initialize()
    }

    override fun loadExtraData() {
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

    override fun onChannelsLoaded() {
        updateCurrentProgram()
    }

    override fun onChannelSelected() {
        updateCurrentProgram()
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
