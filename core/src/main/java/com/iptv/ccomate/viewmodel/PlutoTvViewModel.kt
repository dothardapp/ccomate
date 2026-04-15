package com.iptv.ccomate.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.data.EPGRepository

import com.iptv.ccomate.util.UrlPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import com.iptv.ccomate.model.EPGProgram
import javax.inject.Inject

@HiltViewModel
class PlutoTvViewModel @Inject constructor(
    private val epgRepository: EPGRepository,
    channelRepository: ChannelRepository,
    private val urlPreferences: UrlPreferences
) : ChannelListViewModel(channelRepository) {

    override val sourceName = "PLUTO"
    override val playlistUrl: String
        get() = urlPreferences.plutoPlaylistUrl

    private var epgData: Map<String, List<EPGProgram>> = emptyMap()

    init {
        initialize()
    }

    override fun loadExtraData() {
        viewModelScope.launch {
            try {
                val parsedEpg = withContext(Dispatchers.IO) {
                    epgRepository.getEPGData()
                }
                epgData = parsedEpg
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

        val programs = selectedChannel?.tvgId?.let { tvgId -> epgData[tvgId] }
        val now = ZonedDateTime.now()

        val program = programs?.find { p ->
            now.isAfter(p.startTime) && now.isBefore(p.endTime)
        }

        // Buscar el siguiente programa: el primero que empiece después del actual
        val nextProgram = if (program != null) {
            programs?.filter { it.startTime >= program.endTime }
                ?.minByOrNull { it.startTime }
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            currentProgram = program,
            nextProgram = nextProgram
        )
    }
}
