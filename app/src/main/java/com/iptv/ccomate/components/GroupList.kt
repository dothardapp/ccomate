package com.iptv.ccomate.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
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
        modifier = Modifier.fillMaxSize().padding(15.dp),
        state = listState
    ) {
        itemsIndexed(groups) { index, group ->
            var hasFocus by remember { mutableStateOf(false) }
            val isSelected = index == selectedIndex

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
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Text("🟡", fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(
                        text = group,
                        fontSize = 16.sp,
                        color = when {
                            hasFocus -> Color.Yellow
                            isSelected -> Color(0xFF9ACD32)
                            else -> Color.White
                        }
                    )
                }
            }
        }
    }
}