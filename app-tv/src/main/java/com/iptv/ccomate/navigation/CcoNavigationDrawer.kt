package com.iptv.ccomate.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.tv.material3.*
import com.iptv.ccomate.ui.theme.AppDimensions
import com.iptv.ccomate.ui.theme.AppGradients
import kotlinx.coroutines.launch

@Composable
fun CcoNavigationDrawer(navController: NavController, content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val contentFocusRequester = remember { FocusRequester() }

    val fullscreenState = com.iptv.ccomate.util.LocalFullscreenState.current

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedIndex = remember(currentRoute) {
        drawerItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    LaunchedEffect(fullscreenState.value) {
        if (fullscreenState.value) {
            drawerState.setValue(DrawerValue.Closed)
        }
    }

    // Devolver foco al contenido cuando el drawer se cierra
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            try {
                contentFocusRequester.requestFocus()
            } catch (_: Exception) { }
        }
    }

    BackHandler(enabled = drawerState.currentValue == DrawerValue.Open) {
        scope.launch { drawerState.setValue(DrawerValue.Closed) }
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(AppDimensions.drawerLogoSpacing))
                    DrawerLogo(drawerState)
                    Spacer(modifier = Modifier.height(AppDimensions.drawerLogoSpacing))
                    DrawerItemRenderer(
                        items = drawerItems,
                        selectedIndex = selectedIndex,
                        onItemClick = { _, route ->
                            val current = navController.currentBackStackEntry?.destination?.route
                            if (current == route) {
                                scope.launch { drawerState.setValue(DrawerValue.Closed) }
                                return@DrawerItemRenderer
                            }
                            navController.navigate(route) {
                                popUpTo(Route.Home.path) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.setValue(DrawerValue.Closed) }
                        }
                    )
                }
            } else {
                // drawerContent vacío intencional en fullscreen
                Box(Modifier.width(0.dp))
            }
        }
    ) {
        Box(modifier = Modifier.focusRequester(contentFocusRequester)) {
            content()
        }
    }
}
