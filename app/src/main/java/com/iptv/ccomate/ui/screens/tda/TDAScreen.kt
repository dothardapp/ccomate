package com.iptv.ccomate.ui.screens.tda

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.iptv.ccomate.data.M3UParser
import com.iptv.ccomate.data.NetworkClient
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.ui.screens.ChannelList
import com.iptv.ccomate.ui.screens.GroupList
import com.iptv.ccomate.ui.video.VideoPanel
import com.iptv.ccomate.util.DeviceIdentifier
import com.iptv.ccomate.util.TimeUtils
import kotlinx.coroutines.launch

@Composable
fun TDAScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var selectedGroupIndex by remember { mutableIntStateOf(0) }
    var groups by remember { mutableStateOf<List<String>>(emptyList()) }
    var allChannels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var selectedChannelUrl by remember { mutableStateOf<String?>(null) }
    var selectedChannelName by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("Inicializando TDA...") }
    var playbackError by remember { mutableStateOf<Throwable?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerRestartKey by remember { mutableIntStateOf(0) }
    var lastClickedChannelUrl by remember { mutableStateOf<String?>(null) }

    val isTimeIncorrect = remember { !TimeUtils.isSystemTimeValid() }
    val currentTimeMessage = remember { TimeUtils.getSystemTimeMessage() }



    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                playerRestartKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        val installationId = DeviceIdentifier.getInstallationId(context)
        val deviceInfo = DeviceIdentifier.getDeviceInfo(context)
        Log.d("DeviceIdentifier", "Device Info: $deviceInfo")
        Log.d("InstallationId", "Installation Info: $installationId")
        coroutineScope.launch {
            try {
                statusMessage = "Conectando con TDA..."
                val m3uContent = NetworkClient.fetchM3U("http://10.224.24.232:8081/tda.m3u")
                statusMessage = "Procesando canales..."
                val channels = M3UParser.parse(m3uContent)
                groups = channels.mapNotNull { it.group }.distinct()
                allChannels = channels
                statusMessage = "Listo. Se cargaron ${channels.size} canales TDA."

                if (selectedChannelUrl == null) {
                    val first = channels.firstOrNull()
                    selectedChannelUrl = first?.url
                    selectedChannelName = first?.name
                    lastClickedChannelUrl = first?.url
                }

                playerRestartKey++

            } catch (e: Exception) {
                statusMessage =
                    "❌ Error al cargar canales TDA: ${e.localizedMessage ?: "desconocido"}"
                groups = listOf("Error al cargar")
                Log.e("TDAScreen", "Error al cargar M3U", e)
            }
        }
    }

    val selectedGroup = groups.getOrNull(selectedGroupIndex)
    val filteredChannels = allChannels.filter { it.group == selectedGroup }
    val selectedChannel = allChannels.firstOrNull { it.url == selectedChannelUrl }
    val selectedChannelLogo = selectedChannel?.logo

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Companion.DarkGray)
        ) {
            Box(modifier = Modifier.Companion.weight(1f).padding(6.dp)) {
                Box(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Companion.Black)
                ) {
                    if (isTimeIncorrect) {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .background(Color(0xFFB71C1C))
                                .padding(8.dp)
                                .align(Alignment.Companion.TopCenter)
                        ) {
                            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                                Text(
                                    text = "⚠️ El reloj del dispositivo está mal configurado.",
                                    color = Color.Companion.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currentTimeMessage,
                                    color = Color.Companion.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    key(playerRestartKey) {
                        VideoPanel(
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
                                Log.e("VideoPanelTDAScreen", "Error de reproducción", error)
                            },
                            modifier = Modifier.Companion.fillMaxSize()
                        )
                    }
                }
            }

            if (!selectedChannelLogo.isNullOrBlank()) {
                Box(
                    modifier = Modifier.Companion
                        .weight(1.6f)
                        .padding(6.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .background(Color(0xFF1C1C1C))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = selectedChannelLogo,
                            contentDescription = "Logo canal",
                            modifier = Modifier.Companion
                                .size(width = 100.dp, height = 56.dp)
                                .background(Color.Companion.Black)
                        )

                        Column {
                            Text(
                                text = statusMessage,
                                color = Color.Companion.White,
                                fontSize = 14.sp
                            )
                            if (playbackError != null) {
                                Text(
                                    text = "Error de reproducción: ${playbackError?.localizedMessage ?: "desconocido"}",
                                    color = Color.Companion.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.Companion.height(3.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.Companion.White)
        Spacer(modifier = Modifier.Companion.height(3.dp))

        Row(
            modifier = Modifier.Companion
                .weight(1.8f)
                .fillMaxSize()
                .background(Color(0xFF101010))
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier.Companion
                    .weight(1f)
                    .background(Color.Companion.Black)
            ) {
                GroupList(
                    groups = groups,
                    selectedIndex = selectedGroupIndex,
                    onSelect = { selectedGroupIndex = it }
                )
            }

            Spacer(modifier = Modifier.Companion.width(15.dp))

            Box(
                modifier = Modifier.Companion
                    .weight(2f)
                    .background(Color.Companion.Black)
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