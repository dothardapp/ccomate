package com.iptv.ccomate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val logo: String?,
    val group: String?,
    val tvgId: String?,
    val source: String // "PLUTO" or "TDA"
)
