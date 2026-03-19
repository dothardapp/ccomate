package com.iptv.ccomate.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.ui.screens.ErrorScreen
import com.iptv.ccomate.ui.screens.LoadingScreen
import com.iptv.ccomate.ui.screens.UserInfoScreen
import com.iptv.ccomate.viewmodel.SubscriptionState
import com.iptv.ccomate.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionGate(
    viewModel: SubscriptionViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.checkSubscription() }

    when (val currentState = state) {
        is SubscriptionState.Loading -> LoadingScreen()
        is SubscriptionState.Subscribed -> content()
        is SubscriptionState.NotSubscribed -> ErrorScreen(
            message = currentState.message,
            onRetry = { viewModel.checkSubscription() }
        )
        is SubscriptionState.Error -> ErrorScreen(
            message = currentState.message,
            onRetry = { viewModel.checkSubscription() }
        )
        is SubscriptionState.NeedsUserInfo -> UserInfoScreen { dni, name, phone ->
            viewModel.registerWithUserInfo(dni, name, phone)
        }
    }
}
