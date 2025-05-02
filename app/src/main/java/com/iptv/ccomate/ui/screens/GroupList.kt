package com.iptv.ccomate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.launch

@Composable
fun GroupList(
    groups: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequesters = remember(groups) { List(groups.size) { FocusRequester() } }

    LazyColumn(
        modifier = Modifier.Companion.fillMaxSize().padding(15.dp),
        state = listState
    ) {
        itemsIndexed(groups) { index, group ->
            var hasFocus by remember { mutableStateOf(false) }
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (hasFocus) Color.Companion.DarkGray else Color(0xFF1C1C1C))
                    .focusRequester(focusRequesters[index])
                    .onFocusChanged {
                        hasFocus = it.isFocused
                        if (hasFocus) {
                            coroutineScope.launch { listState.animateScrollToItem(index) }
                        }
                    }
                    .focusable()
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                    if (isSelected) {
                        Text(
                            "🟡",
                            fontSize = 14.sp,
                            modifier = Modifier.Companion.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = group,
                        fontSize = 16.sp,
                        color = when {
                            hasFocus -> Color.Companion.Yellow
                            isSelected -> Color(0xFF9ACD32)
                            else -> Color.Companion.White
                        }
                    )
                }
            }
        }
    }
}