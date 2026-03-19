package com.iptv.ccomate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [EPGEntity::class, ChannelEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun epgDao(): EPGDao
    abstract fun channelDao(): ChannelDao
}
