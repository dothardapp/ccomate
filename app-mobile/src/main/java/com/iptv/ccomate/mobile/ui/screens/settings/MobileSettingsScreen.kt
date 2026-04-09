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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

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

        // --- Refresh buttons ---
        Text(
            text = "Actualizar datos",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MobileColors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Recarga los canales y la guia de programacion desde las URLs configuradas",
            fontSize = 13.sp,
            color = MobileColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        MobileRefreshButton(
            label = "Actualizar canales TDA",
            description = "Recarga la lista de canales TDA",
            isLoading = uiState.isRefreshingTda,
            result = uiState.lastResults.find { it.source == "TDA" },
            onClick = { viewModel.refreshTda() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MobileRefreshButton(
            label = "Actualizar canales Pluto TV",
            description = "Recarga la lista de canales Pluto TV",
            isLoading = uiState.isRefreshingPluto,
            result = uiState.lastResults.find { it.source == "PLUTO" },
            onClick = { viewModel.refreshPluto() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MobileRefreshButton(
            label = "Actualizar EPG",
            description = "Recarga la guia de programacion desde todas las fuentes EPG configuradas",
            isLoading = uiState.isRefreshingEpg,
            result = uiState.lastResults.find { it.source == "EPG" },
            onClick = { viewModel.refreshEpg() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        MobileRefreshButton(
            label = "Actualizar todo",
            description = "Recarga todos los canales y la guia de programas",
            isLoading = uiState.isRefreshingTda || uiState.isRefreshingPluto || uiState.isRefreshingEpg,
            result = null,
            onClick = { viewModel.refreshAll() }
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MobileColors.divider)
        Spacer(modifier = Modifier.height(20.dp))

        // --- URL Configuration ---
        Text(
            text = "URLs de contenido",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MobileColors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configura los enlaces M3U y EPG para cada fuente",
            fontSize = 13.sp,
            color = MobileColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TDA section
        Text(
            text = "TDA (Television Digital Abierta)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MobileColors.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        MobileUrlField(
            label = "Playlist M3U",
            value = uiState.tdaPlaylistUrl,
            error = uiState.tdaPlaylistError,
            onValueChange = { viewModel.updateTdaPlaylistUrl(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MobileUrlField(
            label = "EPG (XML)",
            value = uiState.tdaEpgUrl,
            error = uiState.tdaEpgError,
            placeholder = "Sin configurar (opcional)",
            onValueChange = { viewModel.updateTdaEpgUrl(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pluto section
        Text(
            text = "Pluto TV",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MobileColors.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        MobileUrlField(
            label = "Playlist M3U",
            value = uiState.plutoPlaylistUrl,
            error = uiState.plutoPlaylistError,
            onValueChange = { viewModel.updatePlutoPlaylistUrl(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MobileUrlField(
            label = "EPG (XML)",
            value = uiState.plutoEpgUrl,
            error = uiState.plutoEpgError,
            onValueChange = { viewModel.updatePlutoEpgUrl(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save / Reset buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.saveUrls() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar URLs", color = Color.White)
            }
            Button(
                onClick = { viewModel.resetUrlsToDefaults() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Restaurar por defecto", color = Color.White)
            }
        }

        if (uiState.urlsSaved) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "URLs guardadas correctamente",
                fontSize = 13.sp,
                color = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
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

@Composable
private fun MobileUrlField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, fontSize = 14.sp) }
            } else null,
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MobileColors.textPrimary,
                unfocusedTextColor = MobileColors.textPrimary,
                cursorColor = MobileColors.primary,
                focusedBorderColor = MobileColors.primary,
                unfocusedBorderColor = MobileColors.divider,
                errorBorderColor = Color(0xFFEF5350),
                focusedLabelColor = MobileColors.primary,
                unfocusedLabelColor = MobileColors.textSecondary,
                errorLabelColor = Color(0xFFEF5350),
                focusedContainerColor = MobileColors.surface,
                unfocusedContainerColor = MobileColors.surface,
                errorContainerColor = MobileColors.surface,
                focusedPlaceholderColor = MobileColors.textSecondary.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MobileColors.textSecondary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = Color(0xFFEF5350),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
