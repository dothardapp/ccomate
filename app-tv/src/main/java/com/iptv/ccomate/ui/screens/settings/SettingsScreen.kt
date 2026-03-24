package com.iptv.ccomate.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptv.ccomate.ui.theme.AppColors
import com.iptv.ccomate.ui.theme.AppDimensions
import com.iptv.ccomate.ui.theme.AppGradients
import com.iptv.ccomate.viewmodel.RefreshResult
import com.iptv.ccomate.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.verticalGrayGradient)
            .padding(
                horizontal = AppDimensions.overscanHorizontal,
                vertical = AppDimensions.overscanVertical
            )
    ) {
        Text(
            text = "Configuracion",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Actualizar canales y datos",
            fontSize = 16.sp,
            color = AppColors.textSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 0.5.dp, color = AppColors.gray3)
        Spacer(modifier = Modifier.height(24.dp))

        // Botones de refresh
        RefreshButton(
            label = "Actualizar canales TDA",
            description = "Recarga la lista de canales TDA desde el servidor",
            isLoading = uiState.isRefreshingTda,
            result = uiState.lastResults.find { it.source == "TDA" },
            onClick = { viewModel.refreshTda() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefreshButton(
            label = "Actualizar canales Pluto TV",
            description = "Recarga la lista de canales Pluto TV desde el servidor",
            isLoading = uiState.isRefreshingPluto,
            result = uiState.lastResults.find { it.source == "PLUTO" },
            onClick = { viewModel.refreshPluto() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefreshButton(
            label = "Actualizar EPG (Guia de programas)",
            description = "Recarga la guia de programacion de Pluto TV",
            isLoading = uiState.isRefreshingEpg,
            result = uiState.lastResults.find { it.source == "EPG" },
            onClick = { viewModel.refreshEpg() }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 0.5.dp, color = AppColors.gray3)
        Spacer(modifier = Modifier.height(24.dp))

        RefreshButton(
            label = "Actualizar todo",
            description = "Recarga todos los canales y la guia de programas",
            isLoading = uiState.isRefreshingTda || uiState.isRefreshingPluto || uiState.isRefreshingEpg,
            result = null,
            onClick = { viewModel.refreshAll() }
        )
    }
}

@Composable
private fun RefreshButton(
    label: String,
    description: String,
    isLoading: Boolean,
    result: RefreshResult?,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = when {
        isFocused -> AppColors.accentBlueBorder
        else -> AppColors.gray4
    }
    val bgColor = when {
        isFocused -> Color(0xFF1E3A5F)
        else -> Color(0xFF1C1C1C)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter) &&
                    !isLoading
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isFocused) AppColors.accentBlueFocused else AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )

                // Resultado del ultimo refresh
                if (result != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (result.success)
                            "OK: ${result.channelCount} elementos cargados"
                        else
                            "Error: ${result.errorMessage ?: "desconocido"}",
                        fontSize = 14.sp,
                        color = if (result.success) Color(0xFF4CAF50) else Color(0xFFEF5350)
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgressIndicator(
                    color = AppColors.accentBlue,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}
