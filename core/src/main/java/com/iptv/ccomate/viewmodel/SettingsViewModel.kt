package com.iptv.ccomate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.data.ChannelRepository
import com.iptv.ccomate.data.EPGRepository
import com.iptv.ccomate.util.UrlPreferences
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
    val lastResults: List<RefreshResult> = emptyList(),
    // URL fields
    val tdaPlaylistUrl: String = "",
    val tdaEpgUrl: String = "",
    val plutoPlaylistUrl: String = "",
    val plutoEpgUrl: String = "",
    // Validation errors (null = valid)
    val tdaPlaylistError: String? = null,
    val tdaEpgError: String? = null,
    val plutoPlaylistError: String? = null,
    val plutoEpgError: String? = null,
    // Save feedback
    val urlsSaved: Boolean = false
)

private const val TAG = "SettingsVM"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val epgRepository: EPGRepository,
    private val urlPreferences: UrlPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUrls()
    }

    private fun loadUrls() {
        _uiState.value = _uiState.value.copy(
            tdaPlaylistUrl = urlPreferences.tdaPlaylistUrl,
            tdaEpgUrl = urlPreferences.tdaEpgUrl,
            plutoPlaylistUrl = urlPreferences.plutoPlaylistUrl,
            plutoEpgUrl = urlPreferences.plutoEpgUrl,
            urlsSaved = false
        )
    }

    // --- URL editing ---

    fun updateTdaPlaylistUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            tdaPlaylistUrl = url,
            tdaPlaylistError = UrlPreferences.validatePlaylistUrl(url),
            urlsSaved = false
        )
    }

    fun updateTdaEpgUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            tdaEpgUrl = url,
            tdaEpgError = UrlPreferences.validateEpgUrl(url),
            urlsSaved = false
        )
    }

    fun updatePlutoPlaylistUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            plutoPlaylistUrl = url,
            plutoPlaylistError = UrlPreferences.validatePlaylistUrl(url),
            urlsSaved = false
        )
    }

    fun updatePlutoEpgUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            plutoEpgUrl = url,
            plutoEpgError = UrlPreferences.validateEpgUrl(url),
            urlsSaved = false
        )
    }

    fun saveUrls() {
        val state = _uiState.value

        // Validate all before saving
        val tdaPlaylistErr = UrlPreferences.validatePlaylistUrl(state.tdaPlaylistUrl)
        val tdaEpgErr = UrlPreferences.validateEpgUrl(state.tdaEpgUrl)
        val plutoPlaylistErr = UrlPreferences.validatePlaylistUrl(state.plutoPlaylistUrl)
        val plutoEpgErr = UrlPreferences.validateEpgUrl(state.plutoEpgUrl)

        if (tdaPlaylistErr != null || tdaEpgErr != null || plutoPlaylistErr != null || plutoEpgErr != null) {
            _uiState.value = state.copy(
                tdaPlaylistError = tdaPlaylistErr,
                tdaEpgError = tdaEpgErr,
                plutoPlaylistError = plutoPlaylistErr,
                plutoEpgError = plutoEpgErr
            )
            return
        }

        urlPreferences.tdaPlaylistUrl = state.tdaPlaylistUrl
        urlPreferences.tdaEpgUrl = state.tdaEpgUrl
        urlPreferences.plutoPlaylistUrl = state.plutoPlaylistUrl
        urlPreferences.plutoEpgUrl = state.plutoEpgUrl

        _uiState.value = state.copy(urlsSaved = true)
        Log.d(TAG, "URLs saved successfully")
    }

    fun resetUrlsToDefaults() {
        urlPreferences.resetToDefaults()
        loadUrls()
    }

    // --- Refresh operations ---

    fun refreshTda() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingTda = true)
            val result = try {
                val channels = withContext(Dispatchers.IO) {
                    channelRepository.forceRefresh("TDA", urlPreferences.tdaPlaylistUrl)
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
                    channelRepository.forceRefresh("PLUTO", urlPreferences.plutoPlaylistUrl)
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
