package com.iptv.ccomate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.data.EPGRepository
import com.iptv.ccomate.util.AppConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RefreshResult(
    val source: String,
    val success: Boolean,
    val channelCount: Int = 0,
    val errorMessage: String? = null
)

data class SettingsUiState(
    val isRefreshingTda: Boolean = false,
    val isRefreshingPluto: Boolean = false,
    val isRefreshingEpg: Boolean = false,
    val lastResults: List<RefreshResult> = emptyList()
)

private const val TAG = "SettingsVM"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val epgRepository: EPGRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun refreshTda() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingTda = true)
            val result = try {
                val channels = withContext(Dispatchers.IO) {
                    channelRepository.forceRefresh("TDA", AppConfig.TDA_PLAYLIST_URL)
                }
                Log.d(TAG, "TDA refreshed: ${channels.size} channels")
                RefreshResult("TDA", true, channels.size)
            } catch (e: Exception) {
                Log.e(TAG, "TDA refresh failed", e)
                RefreshResult("TDA", false, errorMessage = e.localizedMessage)
            }
            _uiState.value = _uiState.value.copy(
                isRefreshingTda = false,
                lastResults = _uiState.value.lastResults.filter { it.source != "TDA" } + result
            )
        }
    }

    fun refreshPluto() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingPluto = true)
            val result = try {
                val channels = withContext(Dispatchers.IO) {
                    channelRepository.forceRefresh("PLUTO", AppConfig.PLUTO_PLAYLIST_URL)
                }
                Log.d(TAG, "Pluto refreshed: ${channels.size} channels")
                RefreshResult("PLUTO", true, channels.size)
            } catch (e: Exception) {
                Log.e(TAG, "Pluto refresh failed", e)
                RefreshResult("PLUTO", false, errorMessage = e.localizedMessage)
            }
            _uiState.value = _uiState.value.copy(
                isRefreshingPluto = false,
                lastResults = _uiState.value.lastResults.filter { it.source != "PLUTO" } + result
            )
        }
    }

    fun refreshEpg() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingEpg = true)
            val result = try {
                val epgData = withContext(Dispatchers.IO) {
                    epgRepository.getEPGData(forceRefresh = true)
                }
                val totalPrograms = epgData.values.sumOf { it.size }
                Log.d(TAG, "EPG refreshed: $totalPrograms programs")
                RefreshResult("EPG", true, totalPrograms)
            } catch (e: Exception) {
                Log.e(TAG, "EPG refresh failed", e)
                RefreshResult("EPG", false, errorMessage = e.localizedMessage)
            }
            _uiState.value = _uiState.value.copy(
                isRefreshingEpg = false,
                lastResults = _uiState.value.lastResults.filter { it.source != "EPG" } + result
            )
        }
    }

    fun refreshAll() {
        refreshTda()
        refreshPluto()
        refreshEpg()
    }
}
