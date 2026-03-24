package com.iptv.ccomate.mobile.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.ccomate.viewmodel.PlutoTvViewModel

@Composable
fun PlutoTvScreen(viewModel: PlutoTvViewModel = hiltViewModel()) {
    // Recargar desde cache al entrar en composicion (recoge cambios de Settings)
    LaunchedEffect(Unit) { viewModel.loadChannels() }
    MobileChannelScreen(viewModel = viewModel)
}
