package com.iptv.ccomate.ui.screens.pluto

import android.util.Log
import android.view.KeyEvent
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
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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
import com.iptv.ccomate.util.AppConfig
import com.iptv.ccomate.util.TimeUtils
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private const val ENABLE_EPG_SIDE_PANEL = true

@Composable
fun PlutoTvScreen() {
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
        val fullscreenState = com.iptv.ccomate.util.LocalFullscreenState.current
        val isFullscreen = fullscreenState.value
        var restoreFocus by remember { mutableStateOf(false) }

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
                coroutineScope.launch {
                        try {
                                statusMessage = "Conectando con el servidor..."
                                val m3uContent =
                                        NetworkClient.fetchM3U(AppConfig.PLUTO_PLAYLIST_URL)
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
                                statusMessage =
                                        "❌ Error al cargar canales: ${e.localizedMessage ?: "desconocido"}"
                                groups = listOf("Error al cargar")
                                Log.e("PlutoScreen", "Error al cargar M3U", e)
                        }
                }
        }

        val selectedGroup = groups.getOrNull(selectedGroupIndex)
        val filteredChannels = allChannels.filter { it.group == selectedGroup }
        val selectedChannel = allChannels.firstOrNull { it.url == selectedChannelUrl }
        val selectedChannelLogo = selectedChannel?.logo

        // EPG State
        var epgData by remember {
                mutableStateOf<Map<String, List<com.iptv.ccomate.model.EPGProgram>>>(emptyMap())
        }
        var currentProgram by remember { mutableStateOf<com.iptv.ccomate.model.EPGProgram?>(null) }

        // Fetch EPG
        LaunchedEffect(Unit) {
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                                val epgContent =
                                        NetworkClient.fetchM3U(AppConfig.EPG_URL)
                                val parsedEpg = com.iptv.ccomate.data.EPGParser.parse(epgContent)
                                epgData = parsedEpg
                                Log.d("PlutoTvScreen", "EPG Loaded: ${parsedEpg.size} channels")
                        } catch (e: Exception) {
                                Log.e("PlutoTvScreen", "Error loading EPG", e)
                        }
                }
        }

        // Update Current Program
        LaunchedEffect(selectedChannel, epgData) {
                if (selectedChannel != null && selectedChannel.tvgId != null) {
                        val programs = epgData[selectedChannel.tvgId]
                        if (programs != null) {
                                val now = java.time.ZonedDateTime.now()
                                currentProgram =
                                        programs.find {
                                                now.isAfter(it.startTime) &&
                                                        now.isBefore(it.endTime)
                                        }
                        } else {
                                currentProgram = null
                        }
                } else {
                        currentProgram = null
                }
        }

        val videoContent =
                remember(playerRestartKey) {
                        movableContentOf {
                                url: String?,
                                name: String?,
                                isFull: Boolean,
                                program: com.iptv.ccomate.model.EPGProgram? ->
                                Box(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .focusable() // Foco para el mando
                                                        .clickable {
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
                                                                "VideoPanel",
                                                                "Error de reproducción",
                                                                error
                                                        )
                                                },
                                                modifier = Modifier.fillMaxSize(),
                                                currentProgram = program,
                                                isFullscreen = isFull
                                        )

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
                                                                        Alignment.CenterHorizontally
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "⚠️ El reloj del dispositivo está mal configurado.",
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                val currentIndex = filteredChannels.indexOfFirst { it.url == selectedChannelUrl }
                                if (currentIndex != -1) {
                                    when (event.nativeKeyEvent.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_UP -> {
                                            val prevIndex = if (currentIndex <= 0) filteredChannels.size - 1 else currentIndex - 1
                                            val nextChannel = filteredChannels[prevIndex]
                                            selectedChannelUrl = nextChannel.url
                                            selectedChannelName = nextChannel.name
                                            lastClickedChannelUrl = nextChannel.url
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            val nextIndex = (currentIndex + 1) % filteredChannels.size
                                            val nextChannel = filteredChannels[nextIndex]
                                            selectedChannelUrl = nextChannel.url
                                            selectedChannelName = nextChannel.name
                                            lastClickedChannelUrl = nextChannel.url
                                            true
                                        }
                                        else -> false
                                    }
                                } else false
                            } else false
                        }
                        .focusable()
                ) {
                        videoContent(
                                selectedChannelUrl,
                                selectedChannel?.name,
                                true,
                                currentProgram
                        )
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
                                ) {
                                        videoContent(
                                                selectedChannelUrl,
                                                selectedChannel?.name,
                                                false,
                                                currentProgram
                                        )
                                }

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
                                                                contentDescription = "Logo canal",
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

                                                        // EPG Info in Side Panel
                                                        if (ENABLE_EPG_SIDE_PANEL &&
                                                                        currentProgram != null
                                                        ) {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        12.dp
                                                                                )
                                                                )
                                                                Text(
                                                                        text = currentProgram?.title
                                                                                        ?: "",
                                                                        color = Color.White,
                                                                        fontSize = 20.sp,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        textAlign = TextAlign.Center
                                                                )

                                                                val timeFormatter =
                                                                        DateTimeFormatter.ofPattern(
                                                                                "HH:mm"
                                                                        )
                                                                val start =
                                                                        currentProgram?.startTime
                                                                                ?.format(
                                                                                        timeFormatter
                                                                                )
                                                                                ?: ""
                                                                val end =
                                                                        currentProgram?.endTime
                                                                                ?.format(
                                                                                        timeFormatter
                                                                                )
                                                                                ?: ""

                                                                Text(
                                                                        text = "$start - $end",
                                                                        color = Color(0xFFB0B0B0),
                                                                        fontSize = 16.sp,
                                                                        textAlign =
                                                                                TextAlign.Center,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        top = 4.dp
                                                                                )
                                                                )
                                                        }

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
                                                onSelect = { selectedGroupIndex = it }
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
