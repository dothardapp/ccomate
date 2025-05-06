package com.iptv.ccomate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.iptv.ccomate.model.DrawerIcon
import com.iptv.ccomate.model.DrawerItem
import com.iptv.ccomate.R

val drawerItems = listOf(
    DrawerItem("HOME", DrawerIcon.Vector(Icons.Default.Home), "home"),
    DrawerItem("TDA", DrawerIcon.Resource(R.drawable.tda_tv_digital_abierta), "tda"),
    DrawerItem("PLUTO", DrawerIcon.Resource(R.drawable.baseline_live_tv_24), "plutotv"),
)
