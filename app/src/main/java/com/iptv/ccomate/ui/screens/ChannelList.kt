package com.iptv.ccomate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.util.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChannelList(
        channels: List<Channel>,
        selectedUrl: String?,
        lastClickedUrl: String?,
        onUpdateLastClicked: (String) -> Unit,
        onSelect: (Channel) -> Unit,
        onFullscreenRequest: () -> Unit = {},
        restoreFocus: Boolean = false,
        onFocusRestored: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Mapa persistente de FocusRequesters para evitar pérdida en scroll
    val focusRequesters = remember(channels) { mutableMapOf<Int, FocusRequester>() }

    // Lógica Global de Restauración de Foco
    LaunchedEffect(restoreFocus, channels) {
        if (restoreFocus && selectedUrl != null) {
            val index = channels.indexOfFirst { it.url == selectedUrl }
            if (index != -1) {
                // 1. Forzar scroll para asegurar que el item existe en la composición
                listState.scrollToItem(index)
                // 2. Esperar un frame pequeño para que el sistema reconozca el nuevo estado
                delay(100)
                // 3. Pedir foco
                focusRequesters[index]?.requestFocus()
            }
            onFocusRestored()
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(6.dp), state = listState) {
        itemsIndexed(channels) { index, channel ->
            var hasFocus by remember { mutableStateOf(false) }
            val isPlaying = selectedUrl == channel.url
            var showHint by remember { mutableStateOf(false) }
            
            // Vincular el FocusRequester del mapa
            val itemFocusRequester = remember { focusRequesters.getOrPut(index) { FocusRequester() } }

            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isPlaying && hasFocus -> Color(0xFF4CAF50) // Verde vibrante para canal activo enfocado
                                            isPlaying -> Color(0xFF2E7D32) // Verde oscuro para canal activo no enfocado
                                            hasFocus -> Color.DarkGray // Gris para foco en canal inactivo
                                            else -> Color(0xFF1C1C1C)
                                        }
                                    )
                                    .border(
                                        width = if (hasFocus) 2.dp else 0.dp,
                                        color = if (hasFocus) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .focusRequester(itemFocusRequester)
                                    .onFocusChanged {
                                        hasFocus = it.isFocused
                                        if (hasFocus) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(index)
                                            }
                                        }
                                    }
                                    .focusable()
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
                                    modifier = Modifier.background(Color.Black).size(80.dp, 45.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                    text = channel.name,
                                    fontSize = 18.sp,
                                    color = if (hasFocus) Color.Yellow else Color.White
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
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
