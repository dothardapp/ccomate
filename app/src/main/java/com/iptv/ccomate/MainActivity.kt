package com.iptv.ccomate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.iptv.ccomate.navigation.CcoNavigationDrawer
import com.iptv.ccomate.navigation.AppNavGraph


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CcoMateApp()
        }
    }
}

@Composable
fun CcoMateApp() {
    val navController = rememberNavController()
    MaterialTheme {
        CcoNavigationDrawer(navController = navController) {
            AppNavGraph(navController = navController)
        }
    }
}
