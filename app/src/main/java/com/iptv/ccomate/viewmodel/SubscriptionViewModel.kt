package com.iptv.ccomate.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.util.DeviceIdentifier
import com.iptv.ccomate.util.DeviceInfo
import com.iptv.ccomate.util.RegisterResult
import com.iptv.ccomate.util.SubscriptionManager
import com.iptv.ccomate.util.SubscriptionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    data class Subscribed(val installationId: String) : SubscriptionState()
    data class NotSubscribed(val message: String) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
    object NeedsUserInfo : SubscriptionState()
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {
    private val _state = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val state: StateFlow<SubscriptionState> = _state

    private var deviceInfo: DeviceInfo? = null
    private var clientIp: String? = null

    fun checkSubscription() {
        _state.value = SubscriptionState.Loading
        viewModelScope.launch {
            val info = DeviceIdentifier.getDeviceInfo(appContext)
            deviceInfo = info
            Log.d("SubscriptionViewModel", "Installation ID: ${info.installationId}")

            val result = subscriptionManager.checkSubscription(info.installationId)

            _state.value = when (result) {
                is SubscriptionResult.Subscribed -> {
                    clientIp = result.clientIp
                    SubscriptionState.Subscribed(info.installationId)
                }
                is SubscriptionResult.NotSubscribed -> {
                    clientIp = result.clientIp
                    SubscriptionState.NotSubscribed(result.message)
                }
                is SubscriptionResult.NotFound -> SubscriptionState.NeedsUserInfo
                is SubscriptionResult.Error -> SubscriptionState.Error(result.message)
            }
        }
    }

    fun registerWithUserInfo(dni: String, name: String, phone: String) {
        val info = deviceInfo ?: run {
            _state.value = SubscriptionState.Error("Debe verificar la suscripción o conexión primero")
            return
        }
        _state.value = SubscriptionState.Loading
        viewModelScope.launch {
            val updatedDeviceInfo = info.copy(
                dni = dni,
                name = name,
                phone = phone
            )

            when (val regResult = subscriptionManager.registerDevice(updatedDeviceInfo, clientIp)) {
                is RegisterResult.Success -> {
                    val subResult = subscriptionManager.checkSubscription(updatedDeviceInfo.installationId)
                    _state.value = when (subResult) {
                        is SubscriptionResult.Subscribed -> SubscriptionState.Subscribed(updatedDeviceInfo.installationId)
                        is SubscriptionResult.NotSubscribed -> SubscriptionState.NotSubscribed(subResult.message)
                        is SubscriptionResult.NotFound -> SubscriptionState.NeedsUserInfo
                        is SubscriptionResult.Error -> SubscriptionState.Error(subResult.message)
                    }
                }
                is RegisterResult.Error -> {
                    _state.value = SubscriptionState.Error(regResult.message)
                }
            }
        }
    }
}
