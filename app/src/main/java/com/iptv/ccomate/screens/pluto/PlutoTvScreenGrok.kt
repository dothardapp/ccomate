package com.iptv.ccomate.screens.pluto

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.iptv.ccomate.components.*
import com.iptv.ccomate.components.videopanels.VideoPanelWithoutSSL
import com.iptv.ccomate.data.M3UParser
import com.iptv.ccomate.data.Network
import com.iptv.ccomate.model.Channel
import kotlinx.coroutines.launch

@Composable
fun PlutoTvScreenGrok() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var groups by remember { mutableStateOf<List<String>>(emptyList()) }
    var allChannels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var selectedChannelUrl by remember { mutableStateOf<String?>(null) }
    var selectedChannelName by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("Inicializando...") }
    var playbackError by remember { mutableStateOf<Throwable?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerRestartKey by remember { mutableIntStateOf(0) }
    var lastClickedChannelUrl by remember { mutableStateOf<String?>(null) }


    // Detectar regreso de pantalla (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                playerRestartKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Carga de canales M3U
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                statusMessage = "Conectando con el servidor..."
                val m3uContent = Network.fetchM3U("http://10.224.24.232:8081/playlist.m3u")
                statusMessage = "Procesando canales..."
                val channels = M3UParser.parse(m3uContent)
                groups = channels.mapNotNull { it.group }.distinct()
                allChannels = channels
                statusMessage = "Listo. Se cargaron ${channels.size} canales."

                if (selectedChannelUrl == null) {
                    val first = channels.firstOrNull()
                    selectedChannelUrl = first?.url
                    selectedChannelName = first?.name
                    lastClickedChannelUrl = first?.url
                }

                playerRestartKey++
            } catch (e: Exception) {
                statusMessage = "❌ Error al cargar canales: ${e.localizedMessage ?: "desconocido"}"
                groups = listOf("Error al cargar")
                Log.e("PlutoScreen", "Error al cargar M3U", e)
            }
        }
    }

    val selectedGroup = groups.getOrNull(selectedGroupIndex)
    val filteredChannels = allChannels.filter { it.group == selectedGroup }
    val selectedChannel = allChannels.firstOrNull { it.url == selectedChannelUrl }
    val selectedChannelLogo = selectedChannel?.logo

    PlutoTvTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1C2526), MaterialTheme.colorScheme.background)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Panel del reproductor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            BorderStroke(1.dp, Color(0xFF2A2A2A)),
                            RoundedCornerShape(8.dp)
                        )
                ) {

                    // Indicador de carga
                    if (!isPlaying && selectedChannelUrl != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Cargando canal...",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }

                    // Reproductor de video
                    key(playerRestartKey) {
                        VideoPanelWithoutSSL(
                            context = context,
                            videoUrl = selectedChannelUrl,
                            channelName = selectedChannel?.name,
                            onPlaybackStarted = {
                                statusMessage =
                                    "🎬 Reproduciendo canal: ${selectedChannel?.name ?: "Canal"}"
                                playbackError = null
                                isPlaying = true
                            },
                            onPlaybackError = { error ->
                                playbackError = error
                                isPlaying = false
                                statusMessage =
                                    "❌ Error al reproducir: ${error.localizedMessage ?: "desconocido"}"
                                Log.e("VideoPanel", "Error de reproducción", error)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Panel de logo y estado
                if (!selectedChannelLogo.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(1.6f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AsyncImage(
                                model = selectedChannelLogo,
                                contentDescription = "Logo del canal ${selectedChannel.name}",
                                modifier = Modifier
                                    .size(width = 120.dp, height = 68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .border(
                                        BorderStroke(1.dp, Color(0xFF4A4A4A)),
                                        RoundedCornerShape(8.dp)
                                    )
                            )

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isPlaying) {
                                        Text(
                                            text = "▶ ",
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 14.sp
                                        )
                                    } else if (playbackError != null) {
                                        Text(
                                            text = "❌ ",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(
                                        text = statusMessage,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (playbackError != null) {
                                    Text(
                                        text = "Error: ${playbackError?.localizedMessage ?: "desconocido"}",
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFF2A2A2A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Listas de grupos y canales
            Row(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    GroupList02(
                        groups = groups,
                        selectedIndex = selectedGroupIndex,
                        onSelect = { selectedGroupIndex = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    ChannelListGrok(
                        channels = filteredChannels,
                        selectedUrl = selectedChannelUrl,
                        lastClickedUrl = lastClickedChannelUrl,
                        onUpdateLastClicked = { lastClickedChannelUrl = it },
                        onSelect = {
                            selectedChannelUrl = it.url
                            selectedChannelName = it.name
                            statusMessage = "🎬 Cargando canal: ${it.name}..."
                            playbackError = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}