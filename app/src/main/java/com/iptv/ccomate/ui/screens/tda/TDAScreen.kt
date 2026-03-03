package com.iptv.ccomate.ui.screens.tda

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        val fullscreenState = com.iptv.ccomate.util.LocalFullscreenState.current
        val isFullscreen = fullscreenState.value
        var restoreFocus by remember { mutableStateOf(false) }

        val isTimeIncorrect = remember { !TimeUtils.isSystemTimeValid() }
        val currentTimeMessage = remember { TimeUtils.getSystemTimeMessage() }

        DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                                // Keep the restart key logic but minimize impact
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
                                val m3uContent =
                                        NetworkClient.fetchM3U("http://10.224.24.232:8081/tda.m3u")
                                statusMessage = "Procesando canales..."
                                val channels = M3UParser.parse(m3uContent)
                                groups = channels.mapNotNull { it.group }.distinct()
                                allChannels = channels
                                statusMessage = "✅ Listo. Se cargaron ${channels.size} canales TDA."

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

        // Define movable video content
        val videoContent =
                remember(playerRestartKey) {
                        movableContentOf { url: String?, name: String?, isFull: Boolean ->
                                Box(
                                        modifier =
                                                Modifier.fillMaxSize().clickable {
                                                        if (fullscreenState.value) {
                                                                restoreFocus = true
                                                                fullscreenState.value = false
                                                        } else {
                                                                fullscreenState.value = true
                                                        }
                                                }
                                ) {
                                        VideoPanel(
                                                context = context,
                                                videoUrl = url,
                                                channelName = name,
                                                onPlaybackStarted = {
                                                        statusMessage =
                                                                "🎬 Reproduciendo canal: ${name ?: "Canal"}"
                                                        playbackError = null
                                                        isPlaying = true
                                                },
                                                onPlaybackError = { error ->
                                                        playbackError = error
                                                        isPlaying = false
                                                        statusMessage =
                                                                "❌ Error al reproducir: ${error.localizedMessage ?: "desconocido"}"
                                                        Log.e(
                                                                "VideoPanelTDAScreen",
                                                                "Error de reproducción",
                                                                error
                                                        )
                                                },
                                                modifier = Modifier.fillMaxSize()
                                        )

                                        // Time warning overlay
                                        if (!isFull) {
                                                androidx.compose.animation.AnimatedVisibility(
                                                        visible = isTimeIncorrect,
                                                        enter =
                                                                fadeIn() +
                                                                        scaleIn(
                                                                                animationSpec =
                                                                                        tween(300)
                                                                        ),
                                                        exit =
                                                                fadeOut() +
                                                                        scaleOut(
                                                                                animationSpec =
                                                                                        tween(300)
                                                                        ),
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .align(Alignment.TopCenter)
                                                                        .background(
                                                                                Color(0xE6FF6F00)
                                                                        )
                                                                        .border(
                                                                                1.dp,
                                                                                Color(0xFFF5F5F5),
                                                                                RoundedCornerShape(
                                                                                        6.dp
                                                                                )
                                                                        )
                                                                        .padding(12.dp)
                                                ) {
                                                        Column(
                                                                horizontalAlignment =
                                                                        Alignment
                                                                                .CenterHorizontally,
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "⚠️ Reloj del dispositivo mal configurado",
                                                                        color = Color.White,
                                                                        fontSize = 18.sp,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        textAlign = TextAlign.Center
                                                                )
                                                                Text(
                                                                        text = currentTimeMessage,
                                                                        color = Color(0xFFCFD8DC),
                                                                        fontSize = 15.sp,
                                                                        textAlign = TextAlign.Center
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }

        if (isFullscreen) {
                BackHandler {
                        restoreFocus = true
                        fullscreenState.value = false
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        videoContent(selectedChannelUrl, selectedChannel?.name, true)
                }
        } else {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(
                                                Brush.verticalGradient(
                                                        listOf(
                                                                Color(0xFFD3D3D3),
                                                                Color(0xFF808080),
                                                                Color(0xFF4A4A4A)
                                                        )
                                                )
                                        )
                                        .padding(10.dp)
                ) {
                        Row(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
                                // Video Container
                                Box(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .padding(8.dp)
                                                        .shadow(2.dp, RoundedCornerShape(12.dp))
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                                Brush.verticalGradient(
                                                                        listOf(
                                                                                Color(0xFF696969),
                                                                                Color(0xFF2F4F4F)
                                                                        )
                                                                )
                                                        )
                                                        .border(
                                                                0.5.dp,
                                                                Color(0xFFB0B0B0),
                                                                RoundedCornerShape(12.dp)
                                                        )
                                ) { videoContent(selectedChannelUrl, selectedChannel?.name, false) }

                                // Info Panel
                                Box(modifier = Modifier.weight(1.6f).padding(8.dp)) {
                                        androidx.compose.animation.AnimatedVisibility(
                                                visible = !selectedChannelLogo.isNullOrBlank(),
                                                enter =
                                                        fadeIn() +
                                                                scaleIn(animationSpec = tween(300)),
                                                exit =
                                                        fadeOut() +
                                                                scaleOut(
                                                                        animationSpec = tween(300)
                                                                ),
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .shadow(
                                                                        2.dp,
                                                                        RoundedCornerShape(12.dp)
                                                                )
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(Color(0xCC696969))
                                                                .border(
                                                                        0.5.dp,
                                                                        Color(0xFFB0B0B0),
                                                                        RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(16.dp)
                                        ) {
                                                Column(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        AsyncImage(
                                                                model = selectedChannelLogo,
                                                                contentDescription =
                                                                        "Logo del canal",
                                                                modifier =
                                                                        Modifier.size(
                                                                                        width =
                                                                                                130.dp,
                                                                                        height =
                                                                                                72.dp
                                                                                )
                                                                                .background(
                                                                                        Color.Black
                                                                                )
                                                                                .clip(
                                                                                        RoundedCornerShape(
                                                                                                6.dp
                                                                                        )
                                                                                )
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                                text = statusMessage,
                                                                color =
                                                                        if (playbackError == null)
                                                                                Color.White
                                                                        else Color(0xFFFF5252),
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                textAlign = TextAlign.Center
                                                        )
                                                        if (playbackError != null) {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        8.dp
                                                                                )
                                                                )
                                                                Text(
                                                                        text =
                                                                                "Error: ${playbackError?.localizedMessage ?: "desconocido"}",
                                                                        color = Color(0xFFFF5252),
                                                                        fontSize = 15.sp,
                                                                        textAlign = TextAlign.Center
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFB0B0B0))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.weight(1.6f).fillMaxSize().padding(8.dp)) {
                                Box(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .shadow(2.dp, RoundedCornerShape(12.dp))
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                                Brush.verticalGradient(
                                                                        listOf(
                                                                                Color(0xFF696969),
                                                                                Color(0xFF4A4A4A)
                                                                        )
                                                                )
                                                        )
                                                        .border(
                                                                0.5.dp,
                                                                Color(0xFFB0B0B0),
                                                                RoundedCornerShape(12.dp)
                                                        )
                                ) {
                                        GroupList(
                                                groups = groups,
                                                selectedIndex = selectedGroupIndex,
                                                onSelect = { selectedGroupIndex = it },
                                        )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                        modifier =
                                                Modifier.weight(2f)
                                                        .shadow(2.dp, RoundedCornerShape(12.dp))
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                                Brush.verticalGradient(
                                                                        listOf(
                                                                                Color(0xFF696969),
                                                                                Color(0xFF4A4A4A)
                                                                        )
                                                                )
                                                        )
                                                        .border(
                                                                0.5.dp,
                                                                Color(0xFFB0B0B0),
                                                                RoundedCornerShape(12.dp)
                                                        )
                                ) {
                                        ChannelList(
                                                channels = filteredChannels,
                                                selectedUrl = selectedChannelUrl,
                                                lastClickedUrl = lastClickedChannelUrl,
                                                onUpdateLastClicked = {
                                                        lastClickedChannelUrl = it
                                                },
                                                onSelect = {
                                                        selectedChannelUrl = it.url
                                                        selectedChannelName = it.name
                                                        statusMessage =
                                                                "🎬 Cargando canal: ${it.name}..."
                                                        playbackError = null
                                                },
                                                onFullscreenRequest = {
                                                        fullscreenState.value = true
                                                },
                                                restoreFocus = restoreFocus,
                                                onFocusRestored = { restoreFocus = false }
                                        )
                                }
                        }
                }
        }
}
