package com.iptv.ccomate.mobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.mobile.ui.theme.MobileColors
import com.iptv.ccomate.viewmodel.RefreshResult
import com.iptv.ccomate.viewmodel.SettingsViewModel

@Composable
fun MobileSettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileColors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Configuracion",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MobileColors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Actualizar canales y datos",
            fontSize = 14.sp,
            color = MobileColors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MobileColors.divider)
        Spacer(modifier = Modifier.height(20.dp))

        MobileRefreshButton(
            label = "Actualizar canales TDA",
            description = "Recarga la lista de canales TDA desde el servidor",
            isLoading = uiState.isRefreshingTda,
            result = uiState.lastResults.find { it.source == "TDA" },
            onClick = { viewModel.refreshTda() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MobileRefreshButton(
            label = "Actualizar canales Pluto TV",
            description = "Recarga la lista de canales Pluto TV desde el servidor",
            isLoading = uiState.isRefreshingPluto,
            result = uiState.lastResults.find { it.source == "PLUTO" },
            onClick = { viewModel.refreshPluto() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MobileRefreshButton(
            label = "Actualizar EPG",
            description = "Recarga la guia de programacion de Pluto TV",
            isLoading = uiState.isRefreshingEpg,
            result = uiState.lastResults.find { it.source == "EPG" },
            onClick = { viewModel.refreshEpg() }
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MobileColors.divider)
        Spacer(modifier = Modifier.height(20.dp))

        MobileRefreshButton(
            label = "Actualizar todo",
            description = "Recarga todos los canales y la guia de programas",
            isLoading = uiState.isRefreshingTda || uiState.isRefreshingPluto || uiState.isRefreshingEpg,
            result = null,
            onClick = { viewModel.refreshAll() }
        )
    }
}

@Composable
private fun MobileRefreshButton(
    label: String,
    description: String,
    isLoading: Boolean,
    result: RefreshResult?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MobileColors.divider, RoundedCornerShape(12.dp))
            .background(MobileColors.surface, RoundedCornerShape(12.dp))
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MobileColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MobileColors.textSecondary
                )

                if (result != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (result.success)
                            "OK: ${result.channelCount} elementos cargados"
                        else
                            "Error: ${result.errorMessage ?: "desconocido"}",
                        fontSize = 13.sp,
                        color = if (result.success) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.width(12.dp))
                CircularProgressIndicator(
                    color = MobileColors.primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }
    }
}
