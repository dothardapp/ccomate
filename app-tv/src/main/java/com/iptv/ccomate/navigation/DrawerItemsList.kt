package com.iptv.ccomate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import com.iptv.ccomate.model.DrawerIcon
import com.iptv.ccomate.model.DrawerItem
import com.iptv.ccomate.R

/**
 * Lista de items que se muestran en el drawer de navegación.
 * Usa las rutas centralizadas de [Route] para evitar hardcoding.
 */
val drawerItems = listOf(
    DrawerItem("HOME", DrawerIcon.Vector(Icons.Default.Home), Route.Home.path),
    DrawerItem("TDA", DrawerIcon.Resource(R.drawable.tda_tv_digital_abierta), Route.TDA.path),
    DrawerItem("PLUTO", DrawerIcon.Resource(R.drawable.baseline_live_tv_24), Route.PlutoTV.path),
    DrawerItem("CONFIG", DrawerIcon.Vector(Icons.Default.Settings), Route.Settings.path),
)
