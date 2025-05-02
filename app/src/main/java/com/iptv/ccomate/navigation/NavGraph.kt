package com.iptv.ccomate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.iptv.ccomate.ui.screens.HomeScreen
import com.iptv.ccomate.screens.pluto.PlutoTvScreen
import com.iptv.ccomate.screens.pluto.PlutoTvScreenGrok
import com.iptv.ccomate.screens.tda.TDAScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("tda") { TDAScreen() }
        composable("plutotv") { PlutoTvScreen() }
        composable("grok") { PlutoTvScreenGrok() }
    }
}
