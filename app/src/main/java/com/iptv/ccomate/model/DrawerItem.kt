package com.iptv.ccomate.model

import androidx.compose.ui.graphics.vector.ImageVector

sealed class DrawerIcon {
    data class Vector(val icon: ImageVector) : DrawerIcon()
    data class Resource(val resId: Int) : DrawerIcon()
    data class Svg(val resId: Int) : DrawerIcon()
}

data class DrawerItem(
    val label: String,
    val icon: DrawerIcon,
    val route: String
)
