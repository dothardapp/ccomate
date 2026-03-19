package com.iptv.ccomate.ui.screens

import android.view.KeyEvent
import com.iptv.ccomate.ui.components.GroupSkeletonItem
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.tv.material3.Text

@Composable
fun GroupList(
        groups: List<String>,
        selectedIndex: Int,
        onSelect: (Int) -> Unit,
        onNavigateToChannels: () -> Unit = {},
        isLoading: Boolean = false
) {
    val listState = rememberLazyListState()
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    // Factor de escala para foco
    val scaleFactor = 1.1f

    LazyColumn(
            modifier = Modifier.Companion.fillMaxSize().clipToBounds().padding(15.dp),
            state = listState,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        if (isLoading) {
            items(10) {
                GroupSkeletonItem()
            }
        } else {
            itemsIndexed(groups) { index, group ->
            var hasFocus by remember { mutableStateOf(false) }
            val isSelected = index == selectedIndex

            val itemFocusRequester = remember {
                focusRequesters.getOrPut(index) { FocusRequester() }
            }

            // Escala animada para foco TV (1.1x)
            val focusScale by
                    animateFloatAsState(
                            targetValue = if (hasFocus) scaleFactor else 1f,
                            animationSpec = tween(durationMillis = 200)
                    )

            Box(
                    modifier =
                            Modifier.Companion.fillMaxWidth(
                                            1f / scaleFactor
                                    ) // ~90.9%: al escalar 1.1x llena 100%
                                    .padding(vertical = 4.dp)
                                    .scale(focusScale)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                            if (hasFocus) Color.Companion.DarkGray
                                            else Color(0xFF1C1C1C)
                                    )
                                    .border(
                                            width = if (hasFocus) 2.dp else 0.dp,
                                            color =
                                                    if (hasFocus) Color(0xFFF5F5F5)
                                                    else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                    )
                                    .focusRequester(itemFocusRequester)
                                    .onFocusChanged { hasFocus = it.isFocused }
                                    .onKeyEvent { event ->
                                        if (event.type == KeyEventType.KeyDown &&
                                                event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                            onNavigateToChannels()
                                            true
                                        } else false
                                    }
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
                            color =
                                    when {
                                        hasFocus -> Color(0xFFFFEB3B) // Amarillo TV-safe
                                        isSelected -> Color(0xFF9ACD32)
                                        else -> Color(0xFFF5F5F5) // TV-safe: evitar blanco puro
                                    }
                    )
                }
            }
            }
        }
    }
}
