package com.iptv.ccomate.mobile.ui.screens

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.iptv.ccomate.mobile.ui.theme.MobileColors
import com.iptv.ccomate.mobile.ui.video.VideoPlayer
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.util.AppConfig
import com.iptv.ccomate.viewmodel.ChannelListViewModel
import com.iptv.ccomate.viewmodel.ChannelUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MobileChannelScreen(viewModel: ChannelListViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val selectedGroup = remember(uiState.groups, uiState.selectedGroupIndex) {
        uiState.groups.getOrNull(uiState.selectedGroupIndex)
    }
    val filteredChannels = remember(uiState.allChannels, selectedGroup) {
        uiState.allChannels.filter { it.group == selectedGroup }
    }

    // Immersive system bars control
    ImmersiveMode(isLandscape)

    // movableContentOf preserva el VideoPlayer (VLC) al cambiar de layout
    val videoContent = remember {
        movableContentOf { url: String?, name: String? ->
            if (url != null) {
                VideoPlayer(
                    videoUrl = url,
                    channelName = name,
                    onPlaybackStarted = { viewModel.onPlaybackStarted(name) },
                    onPlaybackError = { viewModel.onPlaybackError(it) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MobileColors.primary)
                    } else {
                        Text(
                            text = uiState.statusMessage,
                            color = MobileColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (isLandscape) {
        // ── Landscape: solo video fullscreen ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MobileColors.playerBackground)
        ) {
            videoContent(uiState.selectedChannelUrl, uiState.selectedChannelName)
        }
    } else {
        // ── Portrait: video + info + canales ──
        Column(modifier = Modifier.fillMaxSize()) {
            // Video player (16:9)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MobileColors.playerBackground)
            ) {
                videoContent(uiState.selectedChannelUrl, uiState.selectedChannelName)
            }

            // Channel name + status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MobileColors.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = uiState.selectedChannelName ?: "Sin canal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MobileColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MobileColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Group chips (horizontal scroll)
            if (uiState.groups.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(uiState.groups) { index, group ->
                        FilterChip(
                            selected = index == uiState.selectedGroupIndex,
                            onClick = { viewModel.selectGroup(index) },
                            label = {
                                Text(
                                    text = group,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MobileColors.primary,
                                selectedLabelColor = MobileColors.onPrimary
                            )
                        )
                    }
                }
            }

            // Channel list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredChannels, key = { it.url }) { channel ->
                    ChannelRow(
                        channel = channel,
                        isSelected = channel.url == uiState.selectedChannelUrl,
                        onClick = {
                            viewModel.selectChannel(channel)
                            viewModel.updateLastClickedChannel(channel.url)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImmersiveMode(enabled: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val window = activity.window

    DisposableEffect(enabled) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (enabled) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) MobileColors.selectedItem else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = channel.logo ?: AppConfig.DEFAULT_CHANNEL_LOGO,
            contentDescription = channel.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MobileColors.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MobileColors.primary else MobileColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            channel.group?.let { group ->
                Text(
                    text = group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MobileColors.textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
