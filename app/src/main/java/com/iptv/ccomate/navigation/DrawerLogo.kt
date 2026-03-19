package com.iptv.ccomate.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import com.iptv.ccomate.R

@Composable
fun DrawerLogo(drawerState: DrawerState) {
    val isOpen = drawerState.currentValue == DrawerValue.Open

    val satelliteSize by animateDpAsState(
        targetValue = if (isOpen) 25.dp else 16.dp,
        animationSpec = tween(300),
        label = "satelliteSize"
    )
    val logoSize by animateDpAsState(
        targetValue = if (isOpen) 70.dp else 35.dp,
        animationSpec = tween(300),
        label = "logoSize"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.baseline_satellite_alt_24),
            contentDescription = "Logo CCO",
            modifier = Modifier.size(satelliteSize)
        )
        Image(
            painter = painterResource(id = R.drawable.cconetbar),
            contentDescription = "Logo CCO",
            modifier = Modifier.size(logoSize)
        )
    }
}
