package com.iptv.ccomate.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.iptv.ccomate.navigation.AppNavGraph
import com.iptv.ccomate.navigation.CcoNavigationDrawer
import com.iptv.ccomate.ui.theme.CCOMateTheme
import com.iptv.ccomate.util.LocalFullscreenState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CcoMateApp() }
    }
}

@Composable
fun CcoMateApp() {
    val navController = rememberNavController()
    val fullscreenState = remember { mutableStateOf(false) }

    CCOMateTheme {
        CompositionLocalProvider(LocalFullscreenState provides fullscreenState) {
            val navContent = remember {
                movableContentOf { AppNavGraph(navController = navController) }
            }
            CcoNavigationDrawer(navController = navController) { navContent() }
        }
    }
}
