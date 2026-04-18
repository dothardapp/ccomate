package com.iptv.ccomate.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.ui.theme.AppColors
import com.iptv.ccomate.ui.theme.AppGradients
import java.time.format.DateTimeFormatter

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun VideoPlayerOverlay(
    visible: Boolean,
    channelName: String?,
    currentProgram: EPGProgram?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && !channelName.isNullOrBlank(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppGradients.videoOverlayGradient)
                .padding(horizontal = 48.dp, vertical = 32.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = currentProgram?.title ?: channelName ?: "",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (currentProgram != null) {
                    val start = currentProgram.startTime.format(TIME_FORMATTER)
                    val end = currentProgram.endTime.format(TIME_FORMATTER)

                    Text(
                        text = "$start - $end",
                        fontSize = 16.sp,
                        color = AppColors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    val description = currentProgram.description
                    if (!description.isNullOrBlank()) {
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = AppColors.textDescription,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
