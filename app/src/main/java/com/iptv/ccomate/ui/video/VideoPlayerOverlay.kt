package com.iptv.ccomate.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.iptv.ccomate.model.EPGProgram
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
                .padding(24.dp)
                .background(
                    Color(0xFF121212).copy(alpha = 0.7f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = currentProgram?.title ?: channelName ?: "",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF5F5F5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (currentProgram != null) {
                    val start = currentProgram.startTime.format(TIME_FORMATTER)
                    val end = currentProgram.endTime.format(TIME_FORMATTER)

                    Text(
                        text = "$start - $end",
                        fontSize = 16.sp,
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (!currentProgram.description.isNullOrBlank()) {
                        Text(
                            text = currentProgram.description,
                            fontSize = 14.sp,
                            color = Color(0xFFF5F5F5).copy(alpha = 0.8f),
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
