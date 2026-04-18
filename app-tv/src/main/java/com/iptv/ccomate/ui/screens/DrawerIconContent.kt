package com.iptv.ccomate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.iptv.ccomate.model.DrawerIcon

@Composable
fun DrawerIconContent(
    icon: DrawerIcon,
    label: String,
    modifier: Modifier = Modifier.Companion,
    tint: Color = Color.Unspecified
) {
    when (icon) {
        is DrawerIcon.Vector -> {
            Icon(
                imageVector = icon.icon,
                contentDescription = label,
                modifier = modifier,
                tint = tint
            )
        }
        is DrawerIcon.Resource -> {
            Image(
                painter = painterResource(id = icon.resId),
                contentDescription = label,
                modifier = modifier,
                colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
            )
        }
    }
}