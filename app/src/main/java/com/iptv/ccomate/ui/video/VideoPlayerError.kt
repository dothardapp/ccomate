package com.iptv.ccomate.ui.video

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.iptv.ccomate.ui.theme.AppColors

@Composable
fun VideoPlayerError(
    visible: Boolean,
    errorMessage: String,
    channelName: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val retryFocusRequester = remember { FocusRequester() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(AppColors.overlayDarker),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = AppColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!channelName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channelName,
                        color = AppColors.textSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                var isRetryFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .focusRequester(retryFocusRequester)
                        .onFocusChanged { isRetryFocused = it.isFocused }
                        .clickable {
                            Log.d("VideoPlayerError", "Retry clicked")
                            onRetry()
                        }
                        .background(
                            if (isRetryFocused) AppColors.accentBlueFocused else AppColors.accentBlue,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = if (isRetryFocused) 2.dp else 1.dp,
                            color = if (isRetryFocused) Color.White else AppColors.accentBlueBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reintentar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Request focus when error becomes visible — wait one frame for layout
        LaunchedEffect(visible) {
            if (visible) {
                withFrameNanos { }
                try {
                    retryFocusRequester.requestFocus()
                    Log.d("VideoPlayerError", "Retry button focused")
                } catch (e: Exception) {
                    Log.w("VideoPlayerError", "Focus request failed: ${e.message}")
                }
            }
        }
    }
}
