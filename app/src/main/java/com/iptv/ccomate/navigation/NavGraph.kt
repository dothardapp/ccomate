package com.iptv.ccomate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.iptv.ccomate.ui.screens.HomeScreen
import com.iptv.ccomate.ui.screens.pluto.PlutoTvScreen
import com.iptv.ccomate.ui.screens.tda.TDAScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) { HomeScreen() }
        composable(Route.TDA.path) { TDAScreen() }
        composable(Route.PlutoTV.path) { PlutoTvScreen() }
    }
}
