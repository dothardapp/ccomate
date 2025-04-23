package com.iptv.ccomate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.iptv.ccomate.components.DrawerIconContent
import com.iptv.ccomate.model.DrawerItem

@Composable
fun NavigationDrawerScope.DrawerItemRenderer(
    items: List<DrawerItem>,
    selectedIndex: Int,
    onItemClick: (Int, String) -> Unit
) {
    Column {
        items.forEachIndexed { index, item ->
            NavigationDrawerItem(
                modifier = Modifier
                    .background(Color.Transparent), // Fondo transparente para todos los estados
                selected = selectedIndex == index, // Mantenemos selected para lógica
                onClick = { onItemClick(index, item.route) },
                leadingContent = {
                    // Usamos DrawerIconContent para renderizar DrawerIcon
                    DrawerIconContent(icon = item.icon, label = item.label)
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color.Transparent, // Sin fondo para seleccionado
                    selectedContentColor = Color.White, // Color del contenido
                )
            ) {
                Text(
                    text = item.label,
                    color = Color.White.copy(alpha = 0.7f), // Color uniforme para todos los ítems
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}