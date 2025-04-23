package com.iptv.ccomate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                            colors = listOf(Color(0xFF1E1F22), MaterialTheme.colorScheme.background)
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
