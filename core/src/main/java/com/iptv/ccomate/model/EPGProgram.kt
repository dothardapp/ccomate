package com.iptv.ccomate.model

import java.time.ZonedDateTime

data class EPGProgram(
        val channelId: String,
        val title: String,
        val description: String?,
        val startTime: ZonedDateTime,
        val endTime: ZonedDateTime,
        val icon: String?
)
