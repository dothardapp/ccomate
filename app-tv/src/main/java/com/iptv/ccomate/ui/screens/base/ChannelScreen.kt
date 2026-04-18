package com.iptv.ccomate.ui.screens.base

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.ui.screens.ChannelList
import com.iptv.ccomate.ui.screens.GroupList
import com.iptv.ccomate.ui.screens.pluto.PlutoColors
import com.iptv.ccomate.ui.screens.pluto.StyledPanelBox
import com.iptv.ccomate.ui.screens.pluto.TimeWarningBanner
import com.iptv.ccomate.ui.video.FullscreenDPadContainer
import com.iptv.ccomate.ui.video.VideoPanel
import com.iptv.ccomate.util.LocalFullscreenState
import com.iptv.ccomate.util.TimeUtils
import com.iptv.ccomate.viewmodel.ChannelUiState

@Composable
fun ChannelScreen(
    uiState: ChannelUiState,
    onSelectGroup: (Int) -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onUpdateLastClicked: (String) -> Unit,
    onPlaybackStarted: (String?) -> Unit,
    onPlaybackError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    screenGradient: Brush = PlutoColors.ScreenGradient,
    fullscreenBackground: Color = PlutoColors.FullscreenBackground,
    onResume: (() -> Unit)? = null,
    infoPanelContent: @Composable (Channel?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val fullscreenState = LocalFullscreenState.current
    val isFullscreen = fullscreenState.value

    // -- Estado de UI local --
    var playerRestartKey by remember { mutableIntStateOf(0) }
    var restoreFocus by remember { mutableStateOf(false) }
    var hasPlayerError by remember { mutableStateOf(false) }

    // FocusRequesters para navegacion D-Pad entre listas
    val groupListFocusRequester = remember { FocusRequester() }
    val channelListFocusRequester = remember { FocusRequester() }

    // -- Estado de tiempo --
    val isTimeIncorrect = remember { !TimeUtils.isSystemTimeValid() }
    val currentTimeMessage = remember { TimeUtils.getSystemTimeMessage() }

    // -- Derivados (cacheados para evitar recomputo en cada recomposición) --
    val selectedGroup by remember(uiState.groups, uiState.selectedGroupIndex) {
        derivedStateOf { uiState.groups.getOrNull(uiState.selectedGroupIndex) }
    }
    val filteredChannels by remember(uiState.allChannels, selectedGroup) {
        derivedStateOf { uiState.allChannels.filter { it.group == selectedGroup } }
    }
    val selectedChannel by remember(uiState.allChannels, uiState.selectedChannelUrl) {
        derivedStateOf { uiState.allChannels.firstOrNull { it.url == uiState.selectedChannelUrl } }
    }
    val selectedChannelLogo = selectedChannel?.logo

    // -- Lifecycle: reiniciar player al volver --
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                playerRestartKey++
                onResume?.invoke()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Bounds del panel de video en pixeles, relativo al Box raiz.
    // IMPORTANTE: Almacenamos Rect (data class con equals por valor), NO LayoutCoordinates.
    // LayoutCoordinates no implementa equals() por valor — cada layout pass crea un objeto nuevo,
    // lo que dispararia recomposicion infinita si se guardara en mutableStateOf.
    var videoPanelBounds by remember { mutableStateOf(Rect.Zero) }
    // Ref no-reactiva para las coordenadas del root — no dispara recomposicion al cambiar.
    val rootCoordsRef = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val density = LocalDensity.current

    // -- Render --
    // Estrategia: el VideoPanel se renderiza en una posicion FIJA en el arbol de Compose.
    // En modo normal se posiciona sobre el placeholder con offset/size.
    // En fullscreen se expande a fillMaxSize.
    // Al no mover el AndroidView entre padres, la Surface nativa NUNCA se destruye,
    // eliminando la pausa de pantalla negra durante transiciones fullscreen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootCoordsRef[0] = it }
    ) {
        // Layer 1: Layout normal (siempre presente para tracking de bounds)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(screenGradient)
                .padding(horizontal = 48.dp, vertical = 27.dp)
        ) {
            // -- Fila superior: Placeholder de video + Info --
            Row(modifier = Modifier.weight(1.35f).fillMaxWidth()) {
                // Placeholder que captura los bounds para posicionar el video
                StyledPanelBox(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .onGloballyPositioned { panelCoords ->
                            val root = rootCoordsRef[0] ?: return@onGloballyPositioned
                            val relativePos = root.localPositionOf(panelCoords, Offset.Zero)
                            val size = panelCoords.size
                            val newBounds = Rect(
                                relativePos.x,
                                relativePos.y,
                                relativePos.x + size.width,
                                relativePos.y + size.height
                            )
                            if (newBounds != videoPanelBounds) {
                                videoPanelBounds = newBounds
                            }
                        },
                    gradient = PlutoColors.VideoContainerGradient
                ) {
                    // Contenido sobre el video (solo en modo normal)
                    if (!isFullscreen) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            TimeWarningBanner(
                                isVisible = isTimeIncorrect,
                                timeMessage = currentTimeMessage,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }

                // Panel de informacion del canal (slot)
                Box(modifier = Modifier.weight(1.6f).padding(8.dp)) {
                    infoPanelContent(selectedChannel)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = PlutoColors.DividerColor)
            Spacer(modifier = Modifier.height(8.dp))

            // -- Fila inferior: Grupos + Canales --
            Row(modifier = Modifier.weight(1.45f).fillMaxSize().padding(8.dp)) {
                StyledPanelBox(modifier = Modifier.weight(1f).focusRequester(groupListFocusRequester)) {
                    GroupList(
                        groups = uiState.groups,
                        selectedIndex = uiState.selectedGroupIndex,
                        onSelect = onSelectGroup,
                        onNavigateToChannels = {
                            try { channelListFocusRequester.requestFocus() } catch (_: Exception) {}
                        },
                        isLoading = uiState.isLoading
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                StyledPanelBox(modifier = Modifier.weight(2f).focusRequester(channelListFocusRequester)) {
                    ChannelList(
                        channels = filteredChannels,
                        selectedUrl = uiState.selectedChannelUrl,
                        lastClickedUrl = uiState.lastClickedChannelUrl,
                        onUpdateLastClicked = onUpdateLastClicked,
                        onSelect = onSelectChannel,
                        onFullscreenRequest = { fullscreenState.value = true },
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

        // Layer 2: Video — posicion fija en el arbol, bounds dinamicos.
        // En modo normal se posiciona sobre el placeholder; en fullscreen llena la pantalla.
        // Al no re-parentear el AndroidView, la Surface nativa se preserva intacta.
        // Usamos localPositionOf para calcular la posicion relativa al Box raiz,
        // evitando desajustes por el drawer/navigation que desplaza boundsInRoot.
        val videoModifier = if (isFullscreen || videoPanelBounds == Rect.Zero) {
            Modifier.fillMaxSize().background(fullscreenBackground)
        } else {
            with(density) {
                Modifier
                    .offset(
                        x = videoPanelBounds.left.toDp(),
                        y = videoPanelBounds.top.toDp()
                    )
                    .size(
                        width = videoPanelBounds.width.toDp(),
                        height = videoPanelBounds.height.toDp()
                    )
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            }
        }

        Box(modifier = videoModifier) {
            VideoPanel(
                videoUrl = uiState.selectedChannelUrl,
                channelName = selectedChannel?.name,
                onPlaybackStarted = { onPlaybackStarted(selectedChannel?.name) },
                onPlaybackError = { error ->
                    onPlaybackError(error)
                    Log.e("ChannelScreen", "Error de reproduccion", error)
                },
                modifier = Modifier.fillMaxSize(),
                onErrorStateChanged = { hasPlayerError = it },
                currentProgram = uiState.currentProgram,
                isFullscreen = isFullscreen
            )
        }

        // Layer 3: Overlay fullscreen (D-Pad, inmersivo, back handler)
        if (isFullscreen) {
            FullscreenDPadContainer(
                channels = filteredChannels,
                selectedChannelUrl = uiState.selectedChannelUrl,
                currentProgram = uiState.currentProgram,
                hasPlayerError = hasPlayerError,
                backgroundColor = Color.Transparent,
                onChannelChanged = { channel ->
                    onSelectChannel(channel)
                    onUpdateLastClicked(channel.url)
                },
                onExitFullscreen = {
                    restoreFocus = true
                    fullscreenState.value = false
                }
            ) {
                // Vacio — el video esta en Layer 2
            }
        }
    }
}
