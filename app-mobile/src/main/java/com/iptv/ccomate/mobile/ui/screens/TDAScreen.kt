package com.iptv.ccomate.mobile.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.ccomate.viewmodel.TdaViewModel

@Composable
fun TDAScreen(viewModel: TdaViewModel = hiltViewModel()) {
    MobileChannelScreen(viewModel = viewModel)
}
