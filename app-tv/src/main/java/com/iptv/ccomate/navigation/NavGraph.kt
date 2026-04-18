package com.iptv.ccomate.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.iptv.ccomate.ui.screens.HomeScreen
import com.iptv.ccomate.ui.screens.pluto.PlutoTvScreen
import com.iptv.ccomate.ui.screens.settings.SettingsScreen
import com.iptv.ccomate.ui.screens.tda.TDAScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.path,
        enterTransition = {
            slideInHorizontally(tween(300)) { it / 8 } + fadeIn(tween(300))
        },
        exitTransition = {
            fadeOut(tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(tween(300)) { -it / 8 } + fadeIn(tween(300))
        },
        popExitTransition = {
            fadeOut(tween(200))
        }
    ) {
        composable(Route.Home.path) { HomeScreen() }
        composable(Route.TDA.path) { TDAScreen() }
        composable(Route.PlutoTV.path) { PlutoTvScreen() }
        composable(Route.Settings.path) { SettingsScreen() }
    }
}
