package com.iptv.ccomate.ui.screens

import android.view.KeyEvent
import com.iptv.ccomate.ui.components.ChannelSkeletonItem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.util.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

@Composable
fun ChannelList(
        channels: List<Channel>,
        selectedUrl: String?,
        lastClickedUrl: String?,
        onUpdateLastClicked: (String) -> Unit,
        onSelect: (Channel) -> Unit,
        onFullscreenRequest: () -> Unit = {},
        onNavigateToGroups: () -> Unit = {},
        restoreFocus: Boolean = false,
        onFocusRestored: () -> Unit = {},
        isLoading: Boolean = false
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Mapa persistente de FocusRequesters para evitar pérdida en scroll
    val focusRequesters = remember(channels) { mutableMapOf<Int, FocusRequester>() }

    // Flag para controlar el foco inicial (solo una vez)
    var initialFocusRequested by remember { mutableStateOf(false) }

    // ── P0: Foco inicial al cargar canales por primera vez ──
    LaunchedEffect(channels) {
        if (channels.isNotEmpty() && !initialFocusRequested) {
            initialFocusRequested = true
            // Determinar el índice objetivo: canal activo o el primero
            val targetIndex = if (selectedUrl != null) {
                channels.indexOfFirst { it.url == selectedUrl }.coerceAtLeast(0)
            } else {
                0
            }
            listState.scrollToItem(targetIndex)
            // Esperar a que Compose materialice el item tras el scroll
            snapshotFlow {
                listState.layoutInfo.visibleItemsInfo.any { it.index == targetIndex }
            }.filter { it }.first()
            focusRequesters[targetIndex]?.requestFocus()
        }
    }

    // Lógica Global de Restauración de Foco (post-fullscreen)
    LaunchedEffect(restoreFocus, channels) {
        if (restoreFocus && selectedUrl != null) {
            val index = channels.indexOfFirst { it.url == selectedUrl }
            if (index != -1) {
                // 1. Forzar scroll para asegurar que el item existe en la composicion
                listState.scrollToItem(index)
                // 2. Esperar a que Compose materialice el item tras el scroll
                snapshotFlow {
                    listState.layoutInfo.visibleItemsInfo.any { it.index == index }
                }.filter { it }.first()
                // 3. Pedir foco
                focusRequesters[index]?.requestFocus()
            }
            onFocusRestored()
        }
    }

    // Factor de escala para foco
    val scaleFactor = 1.1f

    LazyColumn(
            modifier = Modifier.fillMaxSize().clipToBounds().padding(6.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            items(10) {
                ChannelSkeletonItem()
            }
        } else {
            itemsIndexed(channels) { index, channel ->
            var hasFocus by remember { mutableStateOf(false) }
            val isPlaying = selectedUrl == channel.url
            var showHint by remember { mutableStateOf(false) }

            // Escala animada para foco TV (1.1x)
            val focusScale by
                    animateFloatAsState(
                            targetValue = if (hasFocus) scaleFactor else 1f,
                            animationSpec = tween(durationMillis = 200)
                    )

            // Vincular el FocusRequester del mapa
            val itemFocusRequester = remember {
                focusRequesters.getOrPut(index) { FocusRequester() }
            }

            Box(
                    modifier =
                            Modifier.fillMaxWidth(
                                            1f / scaleFactor
                                    ) // ~90.9%: al escalar 1.1x llena 100%
                                    .padding(vertical = 4.dp)
                                    .scale(focusScale)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                            when {
                                                isPlaying && hasFocus -> Color(0xFF4CAF50)
                                                isPlaying -> Color(0xFF2E7D32)
                                                hasFocus -> Color.DarkGray
                                                else -> Color(0xFF1C1C1C)
                                            }
                                    )
                                    .border(
                                            width = if (hasFocus) 2.dp else 0.dp,
                                            color =
                                                    if (hasFocus) Color(0xFFF5F5F5)
                                                    else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                    )
                                    .focusRequester(itemFocusRequester)
                                    .onFocusChanged {
                                        hasFocus = it.isFocused
                                        if (it.isFocused) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(index)
                                            }
                                        }
                                    }
                                    .onKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown &&
                                                event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                            onNavigateToGroups()
                                            true
                                        } else false
                                    }
                                    .clickable {
                                        if (isPlaying) {
                                            onFullscreenRequest()
                                        } else {
                                            onSelect(channel)
                                            onUpdateLastClicked(channel.url)
                                            showHint = true
                                            coroutineScope.launch {
                                                delay(3000)
                                                showHint = false
                                            }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                    model = channel.logo ?: AppConfig.DEFAULT_CHANNEL_LOGO,
                                    contentDescription = "Logo canal",
                                    modifier =
                                            Modifier.background(Color(0xFF121212))
                                                    .size(80.dp, 45.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                    text = channel.name,
                                    fontSize = 18.sp,
                                    color = if (hasFocus) Color(0xFFFFEB3B) else Color(0xFFF5F5F5)
                            )
                        }

                        if (isPlaying) {
                            Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproduciendo",
                                    tint = Color(0xFF9ACD32),
                                    modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (showHint && isPlaying) {
                        Text(
                                text = "Presioná de nuevo para ver en pantalla completa",
                                color = Color(0xFFBDBDBD),
                                fontSize = 14.sp, // Mínimo 14sp para TV
                                modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
            }
        }
    }
}
