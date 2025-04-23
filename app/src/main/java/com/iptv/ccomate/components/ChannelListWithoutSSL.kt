package com.iptv.ccomate.components

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.ui.PlayerActivityMedia3WithoutSSL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChannelListWithoutSSL(
    channels: List<Channel>,
    selectedUrl: String?,
    lastClickedUrl: String?,
    onUpdateLastClicked: (String) -> Unit,
    onSelect: (Channel) -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequesters = remember(channels) { List(channels.size) { FocusRequester() } }
    var showHint by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        state = listState
    ) {
        itemsIndexed(channels) { index, channel ->
            var hasFocus by remember { mutableStateOf(false) }
            val isPlaying = selectedUrl == channel.url

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (hasFocus) Color.DarkGray else Color(0xFF1C1C1C))
                    .focusRequester(focusRequesters[index])
                    .onFocusChanged {
                        hasFocus = it.isFocused
                        if (hasFocus) {
                            coroutineScope.launch { listState.animateScrollToItem(index) }
                        }
                    }
                    .focusable()
                    .clickable {
                        if (lastClickedUrl == channel.url) {
                            // Segundo clic, ir a fullscreen
                            val intent = Intent(context, PlayerActivityMedia3WithoutSSL::class.java)
                            intent.putExtra("url", channel.url)
                            intent.putExtra("name", channel.name)
                            intent.putExtra("logo", channel.logo ?: "")
                            context.startActivity(intent)
                        } else {
                            // Primer clic, reproducir
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
                                model = channel.logo
                                    ?: "https://media.istockphoto.com/id/1409329028/vector/no-picture-available-placeholder-thumbnail-icon-illustration-design.jpg",
                                contentDescription = "Logo canal",
                                modifier = Modifier
                                    .background(Color.Black)
                                    .size(80.dp, 45.dp)
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

                    if (showHint && lastClickedUrl == channel.url) {
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