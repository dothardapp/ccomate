package com.iptv.ccomate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.*
import com.iptv.ccomate.ui.screens.DrawerIconContent
import com.iptv.ccomate.model.DrawerItem
import com.iptv.ccomate.ui.theme.AppColors
import com.iptv.ccomate.ui.theme.AppDimensions
import com.iptv.ccomate.ui.theme.AppTypography

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
                    selectedContentColor = AppColors.selected, // Color del contenido (centralizado)
                )
            ) {
                Text(
                    text = item.label,
                    style = if (selectedIndex == index)
                        AppTypography.drawerLabelSelected
                    else
                        AppTypography.drawerLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
