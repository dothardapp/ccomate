package com.iptv.ccomate.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iptv.ccomate.screens.pluto.PlutoTvTheme
import kotlinx.coroutines.launch

@Composable
fun GroupList02(
    groups: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequesters = remember(groups) { List(groups.size) { FocusRequester() } }

    PlutoTvTheme {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surface),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(groups) { index, group ->
                val isSelected = index == selectedIndex
                var hasFocus by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (hasFocus) 1.05f else 1.0f,
                    animationSpec = tween(durationMillis = 200)
                )

                Card(
                    modifier = Modifier
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
                        .clickable { onSelect(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasFocus) Color(0xFF3A3A3A) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (hasFocus) 6.dp else 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                        Text(
                            text = group,
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                hasFocus -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (hasFocus || isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}