package com.iptv.ccomate.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.activity.PlayerActivityMedia3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChannelList(
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
                            val intent = Intent(context, PlayerActivityMedia3::class.java)
                            intent.putExtra("url", channel.url)
                            intent.putExtra("name", channel.name)
                            intent.putExtra("logo", channel.logo ?: "")
                            intent.putParcelableArrayListExtra("channels", ArrayList(channels))
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