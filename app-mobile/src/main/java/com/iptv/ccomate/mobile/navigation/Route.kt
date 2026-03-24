package com.iptv.ccomate.mobile.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Route(val path: String, val label: String, val icon: ImageVector) {
    data object Home : Route("home", "Inicio", Icons.Default.Home)
    data object TDA : Route("tda", "TDA", Icons.Default.PlayArrow)
    data object PlutoTV : Route("plutotv", "Pluto TV", Icons.Default.Star)
    data object Settings : Route("settings", "Config", Icons.Default.Settings)
}

val bottomNavItems = listOf(Route.Home, Route.TDA, Route.PlutoTV, Route.Settings)
