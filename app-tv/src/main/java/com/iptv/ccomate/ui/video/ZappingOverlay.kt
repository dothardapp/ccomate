package com.iptv.ccomate.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iptv.ccomate.model.Channel
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.ui.DesignTokens
import java.time.format.DateTimeFormatter

@Composable
fun ZappingCard(channel: Channel, program: EPGProgram?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .widthIn(min = 280.dp, max = 360.dp)
            .shadow(DesignTokens.Elevation.high, RoundedCornerShape(12.dp))
            .background(DesignTokens.Colors.bgElevated, RoundedCornerShape(12.dp))
            .border(1.dp, DesignTokens.Colors.divider, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        val context = LocalContext.current
        if (!channel.logo.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(channel.logo)
                    .size(160, 96)
                    .crossfade(true)
                    .build(),
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(80.dp, 44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp, 44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column {
            Text(
                text = channel.name,
                color = DesignTokens.Colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (program != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val start = program.startTime.format(timeFormatter)
                Text(
                    text = "▸ $start ${program.title}",
                    color = DesignTokens.Colors.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                val group = channel.group
                if (!group.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group,
                        color = DesignTokens.Colors.textTertiary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
