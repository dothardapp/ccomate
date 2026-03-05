package com.iptv.ccomate.ui.screens.tda

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.ui.screens.ChannelList
import com.iptv.ccomate.ui.screens.GroupList
import com.iptv.ccomate.ui.video.VideoPanel
import com.iptv.ccomate.util.TimeUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TDAScreen(
    viewModel: TdaViewModel = hiltViewModel()
) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Observar estado desde el ViewModel
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        // ── Estado de UI local ──
        var playerRestartKey by remember { mutableIntStateOf(0) }
        val fullscreenState = com.iptv.ccomate.util.LocalFullscreenState.current
        val isFullscreen = fullscreenState.value
        var restoreFocus by remember { mutableStateOf(false) }

        // P2: FocusRequesters para navegación D-Pad entre listas
        val groupListFocusRequester = remember { FocusRequester() }
        val channelListFocusRequester = remember { FocusRequester() }

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

        // ── Derivados ──
        val selectedGroup = uiState.groups.getOrNull(uiState.selectedGroupIndex)
        val filteredChannels = uiState.allChannels.filter { it.group == selectedGroup }
        val selectedChannel = uiState.allChannels.firstOrNull { it.url == uiState.selectedChannelUrl }
        val selectedChannelLogo = selectedChannel?.logo

        // Define movable video content
        val videoContent =
                remember {
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
                                                        viewModel.onPlaybackStarted(name)
                                                },
                                                onPlaybackError = { error ->
                                                        viewModel.onPlaybackError(error)
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
                                                                        color = Color(0xFFF5F5F5),
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
                // P1: FocusRequester explícita para fullscreen
                val fullscreenFocusRequester = remember { FocusRequester() }

                // Solicitar foco automáticamente al entrar en fullscreen
                LaunchedEffect(Unit) {
                        fullscreenFocusRequester.requestFocus()
                }

                BackHandler {
                        restoreFocus = true
                        fullscreenState.value = false
                }
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(Color(0xFF121212)) // TV-safe: evitar negro puro
                                        .focusRequester(fullscreenFocusRequester)
                                        .onKeyEvent { event ->
                                                if (event.type == KeyEventType.KeyDown) {
                                                        val currentIndex =
                                                                filteredChannels.indexOfFirst {
                                                                        it.url == uiState.selectedChannelUrl
                                                                }
                                                        if (currentIndex != -1) {
                                                                when (event.nativeKeyEvent.keyCode
                                                                ) {
                                                                        KeyEvent.KEYCODE_DPAD_UP -> {
                                                                                // Canal anterior
                                                                                val prevIndex =
                                                                                        if (currentIndex <=
                                                                                                        0
                                                                                        )
                                                                                                filteredChannels
                                                                                                        .size -
                                                                                                        1
                                                                                        else
                                                                                                currentIndex -
                                                                                                        1
                                                                                val nextChannel =
                                                                                        filteredChannels[
                                                                                                prevIndex]
                                                                                viewModel.selectChannel(nextChannel)
                                                                                viewModel.updateLastClickedChannel(nextChannel.url)
                                                                                true
                                                                        }
                                                                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                                                // Canal siguiente
                                                                                val nextIndex =
                                                                                        (currentIndex +
                                                                                                1) %
                                                                                                filteredChannels
                                                                                                        .size
                                                                                val nextChannel =
                                                                                        filteredChannels[
                                                                                                nextIndex]
                                                                                viewModel.selectChannel(nextChannel)
                                                                                viewModel.updateLastClickedChannel(nextChannel.url)
                                                                                true
                                                                        }
                                                                        else -> false
                                                                }
                                                        } else false
                                                } else false
                                        }
                                        .focusable()
                ) { videoContent(uiState.selectedChannelUrl, selectedChannel?.name, true) }
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
                                        .padding(
                                                horizontal = 48.dp,
                                                vertical = 27.dp
                                        ) // Overscan zona segura 5%
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
                                ) { videoContent(uiState.selectedChannelUrl, selectedChannel?.name, false) }

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
                                                                                        Color(
                                                                                                0xFF121212
                                                                                        ) // TV-safe
                                                                                )
                                                                                .clip(
                                                                                        RoundedCornerShape(
                                                                                                6.dp
                                                                                        )
                                                                                )
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                                text = uiState.statusMessage,
                                                                color =
                                                                        if (uiState.playbackError == null)
                                                                                Color(
                                                                                        0xFFF5F5F5
                                                                                ) // TV-safe
                                                                        else Color(0xFFFF5252),
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                textAlign = TextAlign.Center
                                                        )
                                                        if (uiState.playbackError != null) {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        8.dp
                                                                                )
                                                                )
                                                                Text(
                                                                        text =
                                                                                "Error: ${uiState.playbackError?.localizedMessage ?: "desconocido"}",
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
                                                        .focusRequester(groupListFocusRequester)
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
                                                groups = uiState.groups,
                                                selectedIndex = uiState.selectedGroupIndex,
                                                onSelect = { viewModel.selectGroup(it) },
                                                // P2: D-Pad Right → navegar a ChannelList
                                                onNavigateToChannels = {
                                                        try { channelListFocusRequester.requestFocus() } catch (_: Exception) {}
                                                },
                                                isLoading = uiState.isLoading
                                        )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                        modifier =
                                                Modifier.weight(2f)
                                                        .focusRequester(channelListFocusRequester)
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
                                                selectedUrl = uiState.selectedChannelUrl,
                                                lastClickedUrl = uiState.lastClickedChannelUrl,
                                                onUpdateLastClicked = {
                                                        viewModel.updateLastClickedChannel(it)
                                                },
                                                onSelect = {
                                                        viewModel.selectChannel(it)
                                                },
                                                onFullscreenRequest = {
                                                        fullscreenState.value = true
                                                },
                                                // P2: D-Pad Left → navegar a GroupList
                                                onNavigateToGroups = {
                                                        try { groupListFocusRequester.requestFocus() } catch (_: Exception) {}
                                                },
                                                restoreFocus = restoreFocus,
                                                onFocusRestored = { restoreFocus = false },
                                                isLoading = uiState.isLoading
                                        )
                                }
                        }
                }
        }
}
