package com.iptv.ccomate.mobile

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.iptv.ccomate.mobile.navigation.AppNavGraph
import com.iptv.ccomate.mobile.navigation.bottomNavItems
import com.iptv.ccomate.mobile.ui.theme.CCOMateTheme
import com.iptv.ccomate.mobile.ui.theme.MobileColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CcoMateMobileApp() }
    }
}

@Composable
fun CcoMateMobileApp() {
    val navController = rememberNavController()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    CCOMateTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!isLandscape) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    NavigationBar(containerColor = MobileColors.surface) {
                        bottomNavItems.forEach { route ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == route.path } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(route.path) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = route.icon,
                                        contentDescription = route.label
                                    )
                                },
                                label = { Text(route.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MobileColors.primary,
                                    selectedTextColor = MobileColors.primary,
                                    unselectedIconColor = MobileColors.textSecondary,
                                    unselectedTextColor = MobileColors.textSecondary,
                                    indicatorColor = MobileColors.selectedItem
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
