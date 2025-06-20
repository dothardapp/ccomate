package com.iptv.ccomate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.*
import kotlinx.coroutines.launch

@Composable
fun CcoNavigationDrawer(
    navController: NavController,
    content: @Composable () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    NavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                Modifier
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF5F5F5), // Gris claro (casi blanco)
                                Color(0xFFD3D3D3), // Gris claro
                                Color(0xFFB0B0B0), // Gris claro medio
                                Color(0xFF808080), // Gris medio
                                Color(0xFF696969), // Gris oscuro
                                Color(0xFF4A4A4A),  // Gris muy oscuro
                                Color(0xFF2F4F4F)   // Gris oscuro (casi negro)
                            )
                        )
                    )
                    .selectableGroup(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Spacer(modifier = Modifier.height(12.dp))
                DrawerLogo(drawerState)
                Spacer(modifier = Modifier.height(12.dp))
                DrawerItemRenderer(
                    items = drawerItems,
                    selectedIndex = selectedIndex,
                    onItemClick = { index, route ->
                        selectedIndex = index
                        navController.navigate(route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        scope.launch {
                            drawerState.setValue(DrawerValue.Closed)
                        }
                    }
                )
            }
        }
    ) {
        content()
    }
}
