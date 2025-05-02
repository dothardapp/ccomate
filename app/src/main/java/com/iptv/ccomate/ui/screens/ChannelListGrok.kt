package com.iptv.ccomate.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptv.ccomate.R
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.activity.PlayerActivityMedia3
import com.iptv.ccomate.ui.theme.PlutoTvTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChannelListGrok(
    channels: List<Channel>,
    selectedUrl: String?,
    lastClickedUrl: String?,
    onUpdateLastClicked: (String) -> Unit,
    onSelect: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequesters = remember(channels) { List(channels.size) { FocusRequester() } }
    var showHint by remember { mutableStateOf(false) }
    var hintChannelUrl by remember { mutableStateOf<String?>(null) }

    PlutoTvTheme {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surface),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(channels) { index, channel ->
                val isPlaying = selectedUrl == channel.url
                var hasFocus by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (hasFocus) 1.05f else 1.0f,
                    animationSpec = tween(durationMillis = 200)
                )

                Card(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .scale(scale)
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
                                context.startActivity(intent)
                            } else {
                                // Primer clic, reproducir
                                onSelect(channel)
                                onUpdateLastClicked(channel.url)
                                showHint = true
                                hintChannelUrl = channel.url
                                coroutineScope.launch {
                                    delay(3000)
                                    showHint = false
                                    hintChannelUrl = null
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasFocus) Color(0xFF3A3A3A) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (hasFocus) 6.dp else 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = channel.logo
                                    ?: "https://media.istockphoto.com/id/1409329028/vector/no-picture-available-placeholder-thumbnail-icon-illustration-design.jpg",
                                contentDescription = "Logo del canal ${channel.name}",
                                modifier = Modifier.Companion
                                    .size(width = 80.dp, height = 45.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(Color.Companion.Black),
                                placeholder = painterResource(id = R.drawable.ic_action_name),
                                error = painterResource(id = R.drawable.tv_abierta)
                            )
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (hasFocus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (hasFocus || isPlaying) FontWeight.Companion.Bold else FontWeight.Companion.Normal
                            )
                        }

                        if (isPlaying) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Canal en reproducción",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.Companion.size(28.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showHint && hintChannelUrl == channel.url,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Presiona de nuevo para ver en pantalla completa",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                modifier = Modifier.Companion.align(Alignment.Companion.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}