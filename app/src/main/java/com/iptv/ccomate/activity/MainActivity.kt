package com.iptv.ccomate.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.iptv.ccomate.navigation.AppNavGraph
import com.iptv.ccomate.navigation.CcoNavigationDrawer
import com.iptv.ccomate.viewmodel.SubscriptionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CcoMateApp() }
    }
}

@Composable
fun CcoMateApp(viewModel: SubscriptionViewModel = viewModel()) {
    val context = LocalContext.current
    val navController = rememberNavController()
    // val state by viewModel.state.collectAsState()
    val fullscreenState = remember { mutableStateOf(false) }

    // LaunchedEffect(Unit) { viewModel.checkSubscription(context) }

    MaterialTheme {
        androidx.compose.runtime.CompositionLocalProvider(
                com.iptv.ccomate.util.LocalFullscreenState provides fullscreenState
        ) {
            // Bypass subscription check temporarily
            val navContent = remember {
                androidx.compose.runtime.movableContentOf {
                    AppNavGraph(navController = navController)
                }
            }

            CcoNavigationDrawer(navController = navController) { navContent() }

            /*
            when (state) {
                is SubscriptionState.Loading -> {
                    LoadingScreen()
                }
                is SubscriptionState.Subscribed -> {
                    val navContent = remember {
                        androidx.compose.runtime.movableContentOf {
                            AppNavGraph(navController = navController)
                        }
                    }

                    CcoNavigationDrawer(navController = navController) { navContent() }
                }
                is SubscriptionState.NotSubscribed -> {
                    ErrorScreen(
                            message = (state as SubscriptionState.NotSubscribed).message,
                            onRetry = { viewModel.checkSubscription(context) }
                    )
                }
                is SubscriptionState.Error -> {
                    ErrorScreen(
                            message = (state as SubscriptionState.Error).message,
                            onRetry = { viewModel.checkSubscription(context) }
                    )
                }
                is SubscriptionState.NeedsUserInfo -> {
                    UserInfoScreen { dni, name, phone ->
                        viewModel.registerWithUserInfo(dni, name, phone)
                    }
                }
            }
            */
        }
    }
}
