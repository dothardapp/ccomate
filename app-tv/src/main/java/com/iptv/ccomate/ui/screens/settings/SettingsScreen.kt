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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.verticalGrayGradient)
            .verticalScroll(rememberScrollState())
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

        // --- Refresh buttons ---
        Text(
            text = "Actualizar datos",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Recarga los canales y la guia de programacion desde las URLs configuradas",
            fontSize = 14.sp,
            color = AppColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefreshButton(
            label = "Actualizar canales TDA",
            description = "Recarga la lista de canales TDA",
            isLoading = uiState.isRefreshingTda,
            result = uiState.lastResults.find { it.source == "TDA" },
            onClick = { viewModel.refreshTda() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefreshButton(
            label = "Actualizar canales Pluto TV",
            description = "Recarga la lista de canales Pluto TV",
            isLoading = uiState.isRefreshingPluto,
            result = uiState.lastResults.find { it.source == "PLUTO" },
            onClick = { viewModel.refreshPluto() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefreshButton(
            label = "Actualizar EPG (Guia de programas)",
            description = "Recarga la guia de programacion desde todas las fuentes EPG configuradas",
            isLoading = uiState.isRefreshingEpg,
            result = uiState.lastResults.find { it.source == "EPG" },
            onClick = { viewModel.refreshEpg() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        RefreshButton(
            label = "Actualizar todo",
            description = "Recarga todos los canales y la guia de programas",
            isLoading = uiState.isRefreshingTda || uiState.isRefreshingPluto || uiState.isRefreshingEpg,
            result = null,
            onClick = { viewModel.refreshAll() }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 0.5.dp, color = AppColors.gray3)
        Spacer(modifier = Modifier.height(24.dp))

        // --- URL Configuration ---
        Text(
            text = "URLs de contenido",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configura los enlaces M3U y EPG para cada fuente",
            fontSize = 14.sp,
            color = AppColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TDA section
        Text(
            text = "TDA (Television Digital Abierta)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.accentBlueFocused
        )
        Spacer(modifier = Modifier.height(8.dp))

        UrlField(
            label = "Playlist M3U",
            value = uiState.tdaPlaylistUrl,
            error = uiState.tdaPlaylistError,
            onValueChange = { viewModel.updateTdaPlaylistUrl(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        UrlField(
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.accentBlueFocused
        )
        Spacer(modifier = Modifier.height(8.dp))

        UrlField(
            label = "Playlist M3U",
            value = uiState.plutoPlaylistUrl,
            error = uiState.plutoPlaylistError,
            onValueChange = { viewModel.updatePlutoPlaylistUrl(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        UrlField(
            label = "EPG (XML)",
            value = uiState.plutoEpgUrl,
            error = uiState.plutoEpgError,
            onValueChange = { viewModel.updatePlutoEpgUrl(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save / Reset buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionButton(
                label = "Guardar URLs",
                onClick = { viewModel.saveUrls() },
                accentColor = Color(0xFF4CAF50)
            )
            ActionButton(
                label = "Restaurar por defecto",
                onClick = { viewModel.resetUrlsToDefaults() },
                accentColor = Color(0xFFFF9800)
            )
        }

        if (uiState.urlsSaved) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "URLs guardadas correctamente",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun UrlField(
    label: String,
    value: String,
    error: String?,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppColors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = AppColors.textPrimary,
                fontSize = 15.sp
            ),
            cursorBrush = SolidColor(AppColors.accentBlue),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = when {
                                error != null -> Color(0xFFEF5350)
                                isFocused -> AppColors.accentBlueBorder
                                else -> AppColors.gray4
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color(0xFF1C1C1C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = AppColors.textSecondary.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = Color(0xFFEF5350)
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) accentColor else AppColors.gray4,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                if (isFocused) accentColor.copy(alpha = 0.15f) else Color(0xFF1C1C1C),
                RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isFocused) accentColor else AppColors.textPrimary
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
