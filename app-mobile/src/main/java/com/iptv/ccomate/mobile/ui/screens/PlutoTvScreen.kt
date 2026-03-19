package com.iptv.ccomate.mobile.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.ccomate.viewmodel.PlutoTvViewModel

@Composable
fun PlutoTvScreen(viewModel: PlutoTvViewModel = hiltViewModel()) {
    MobileChannelScreen(viewModel = viewModel)
}
