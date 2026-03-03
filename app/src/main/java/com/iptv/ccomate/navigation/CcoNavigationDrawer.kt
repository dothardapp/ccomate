package com.iptv.ccomate.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.tv.material3.*
import com.iptv.ccomate.ui.theme.AppDimensions
import com.iptv.ccomate.ui.theme.AppGradients
import kotlinx.coroutines.launch

@Composable
fun CcoNavigationDrawer(navController: NavController, content: @Composable () -> Unit) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val fullscreenState = com.iptv.ccomate.util.LocalFullscreenState.current

    LaunchedEffect(fullscreenState.value) {
        if (fullscreenState.value) {
            drawerState.setValue(DrawerValue.Closed)
        }
    }

    NavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (!fullscreenState.value) {
                Column(
                    Modifier
                        .fillMaxHeight()
                        .background(brush = AppGradients.verticalGrayGradient)
                        .selectableGroup(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.drawerItemSpacing)
                ) {
                    Spacer(modifier = Modifier.height(AppDimensions.drawerLogoSpacing))
                    DrawerLogo(drawerState)
                    Spacer(modifier = Modifier.height(AppDimensions.drawerLogoSpacing))
                    DrawerItemRenderer(
                        items = drawerItems,
                        selectedIndex = selectedIndex,
                        onItemClick = { index, route ->
                            selectedIndex = index
                            navController.navigate(route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.setValue(DrawerValue.Closed) }
                        }
                    )
                }
            } else {
                // Keep the drawer effectively invisible/gone when fullscreen
                Spacer(Modifier.width(0.dp))
            }
        }
    ) { content() }
}
