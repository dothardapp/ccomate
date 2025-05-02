package com.iptv.ccomate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.iptv.ccomate.model.DrawerIcon

@Composable
fun DrawerIconContent(
    icon: DrawerIcon,
    label: String,
    modifier: Modifier = Modifier.Companion
) {
    when (icon) {
        is DrawerIcon.Vector -> {
            Icon(
                imageVector = icon.icon,
                contentDescription = label,
                modifier = modifier
            )
        }
        is DrawerIcon.Resource -> {
            Image(
                painter = painterResource(id = icon.resId),
                contentDescription = label,
                modifier = modifier
            )
        }
        is DrawerIcon.Svg -> {
            // Nota: Para SVG, necesitas una librería como coil-compose
            // Por ahora, asumimos que es un recurso drawable
            Image(
                painter = painterResource(id = icon.resId),
                contentDescription = label,
                modifier = modifier
            )
        }
    }
}