package com.iptv.ccomate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.ccomate.util.DeviceIdentifier
import com.iptv.ccomate.util.DeviceInfo
import com.iptv.ccomate.util.SubscriptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    data class Subscribed(val installationId: String) : SubscriptionState()
    data class NotSubscribed(val message: String) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
    object NeedsUserInfo : SubscriptionState()
}

class SubscriptionViewModel : ViewModel() {
    private val _state = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val state: StateFlow<SubscriptionState> = _state

    private lateinit var deviceInfo: DeviceInfo

    fun checkSubscription(context: Context) {
        viewModelScope.launch {
            deviceInfo = DeviceIdentifier.getDeviceInfo(context)
            println("Installation ID: ${deviceInfo.installationId}")

            SubscriptionManager.checkSubscription(deviceInfo.installationId) { isSubscribed, subError ->
                if (subError == null) {
                    if (isSubscribed) {
                        _state.value = SubscriptionState.Subscribed(deviceInfo.installationId)
                    } else {
                        _state.value = SubscriptionState.NotSubscribed("No tienes una suscripción activa")
                    }
                } else if (subError.contains("Device not found")) {
                    _state.value = SubscriptionState.NeedsUserInfo
                } else {
                    _state.value = SubscriptionState.Error(subError)
                }
            }
        }
    }

    fun registerWithUserInfo(dni: String, name: String, phone: String) {
        _state.value = SubscriptionState.Loading
        viewModelScope.launch {
            val updatedDeviceInfo = deviceInfo.copy(
                dni = dni,
                name = name,
                phone = phone
            )

            SubscriptionManager.registerDevice(updatedDeviceInfo) { regSuccess, regError ->
                if (regSuccess) {
                    SubscriptionManager.checkSubscription(updatedDeviceInfo.installationId) { newIsSubscribed, newSubError ->
                        if (newSubError != null) {
                            _state.value = SubscriptionState.Error(newSubError)
                        } else if (newIsSubscribed) {
                            _state.value = SubscriptionState.Subscribed(updatedDeviceInfo.installationId)
                        } else {
                            _state.value = SubscriptionState.NotSubscribed("No tienes una suscripción activa")
                        }
                    }
                } else {
                    _state.value = SubscriptionState.Error(regError ?: "Error al registrar dispositivo")
                }
            }
        }
    }
}