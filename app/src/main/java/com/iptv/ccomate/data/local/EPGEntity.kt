package com.iptv.ccomate.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["channelId"]),
        Index(value = ["channelId", "endTime"])
    ]
)
data class EPGEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val title: String,
    val description: String?,
    val startTime: String, // Stored as ISO string
    val endTime: String,   // Stored as ISO string
    val icon: String?
)
