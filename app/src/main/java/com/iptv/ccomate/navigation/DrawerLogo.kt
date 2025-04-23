package com.iptv.ccomate.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import com.iptv.ccomate.R

@Composable
fun DrawerLogo(drawerState: DrawerState) {

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        if (drawerState.currentValue == DrawerValue.Closed) {
            Image(
                painter = painterResource(id = R.drawable.cco_logo_02_white),
                contentDescription = "Logo CCO",
                modifier = Modifier.size(16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.cco_logo_01),
                contentDescription = "Logo CCO",
                modifier = Modifier.size(35.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.cco_logo_02_white),
                contentDescription = "Logo CCO",
                modifier = Modifier.size(25.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.cco_logo_01),
                contentDescription = "Logo CCO",
                modifier = Modifier.size(70.dp)
            )
        }
    }
}