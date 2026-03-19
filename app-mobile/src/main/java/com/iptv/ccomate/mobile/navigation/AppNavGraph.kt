package com.iptv.ccomate.mobile.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.iptv.ccomate.mobile.ui.screens.HomeScreen
import com.iptv.ccomate.mobile.ui.screens.PlutoTvScreen
import com.iptv.ccomate.mobile.ui.screens.TDAScreen

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.path,
        modifier = modifier
    ) {
        composable(Route.Home.path) { HomeScreen() }
        composable(Route.TDA.path) { TDAScreen() }
        composable(Route.PlutoTV.path) { PlutoTvScreen() }
    }
}
