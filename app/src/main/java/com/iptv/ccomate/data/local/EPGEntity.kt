package com.iptv.ccomate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(tableName = "epg_programs")
data class EPGEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val title: String,
    val description: String?,
    val startTime: String, // Stored as ISO string
    val endTime: String,   // Stored as ISO string
    val icon: String?
)
