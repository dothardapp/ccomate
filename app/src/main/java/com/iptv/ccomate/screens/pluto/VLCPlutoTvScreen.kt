package com.iptv.ccomate.screens.pluto

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.components.*
import com.iptv.ccomate.components.vlc.VLCVideoPanel
import com.iptv.ccomate.data.M3UParser
import com.iptv.ccomate.data.Network
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.util.TimeUtils
import kotlinx.coroutines.launch

@Composable
fun VLCPlutoTvScreen() {
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

    val isTimeIncorrect = remember { !TimeUtils.isSystemTimeValid() }
    val currentTimeMessage = remember { TimeUtils.getSystemTimeMessage() }

    // 👁️ Detectar regreso de pantalla (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                playerRestartKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 📡 Carga de canales M3U
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
                Log.e("VLCPlutoScreen", "Error al cargar M3U", e)
            }
        }
    }

    val selectedGroup = groups.getOrNull(selectedGroupIndex)
    val filteredChannels = allChannels.filter { it.group == selectedGroup }
    val selectedChannel = allChannels.firstOrNull { it.url == selectedChannelUrl }
    val selectedChannelLogo = selectedChannel?.logo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.LightGray)
        ) {
            Box(modifier = Modifier.weight(1f).padding(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black)
                ) {
                    // ⏰ Banner de advertencia de hora incorrecta (visible arriba del video)
                    if (isTimeIncorrect) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFB71C1C))
                                .padding(8.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚠️ El reloj del dispositivo está mal configurado.",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currentTimeMessage,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // 🎬 VLCVideoPanel
                    key(playerRestartKey) {
                        VLCVideoPanel(
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
                                Log.e("VLCVideoPanel", "Error de reproducción", error)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (!selectedChannelLogo.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .weight(1.6f)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1C))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = selectedChannelLogo,
                            contentDescription = "Logo canal",
                            modifier = Modifier
                                .size(width = 100.dp, height = 56.dp)
                                .background(Color.Black)
                        )

                        Column {
                            Text(
                                text = statusMessage,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (playbackError != null) {
                                Text(
                                    text = "Error de reproducción: ${playbackError?.localizedMessage ?: "desconocido"}",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.White)
        Spacer(modifier = Modifier.height(3.dp))

        Row(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxSize()
                .background(Color(0xAB030301))
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
            ) {
                GroupList(
                    groups = groups,
                    selectedIndex = selectedGroupIndex,
                    onSelect = { selectedGroupIndex = it }
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Box(
                modifier = Modifier
                    .weight(2f)
                    .background(Color.Black)
            ) {
                ChannelList(
                    channels = filteredChannels,
                    selectedUrl = selectedChannelUrl,
                    lastClickedUrl = lastClickedChannelUrl,
                    onUpdateLastClicked = { lastClickedChannelUrl = it },
                    onSelect = {
                        selectedChannelUrl = it.url
                        selectedChannelName = it.name
                        statusMessage = "🎬 Cargando canal: ${it.name}..."
                        playbackError = null
                    }
                )
            }
        }
    }
}