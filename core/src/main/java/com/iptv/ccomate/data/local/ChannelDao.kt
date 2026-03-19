package com.iptv.ccomate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE source = :source")
    suspend fun getChannelsBySource(source: String): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Transaction
    suspend fun replaceChannels(source: String, channels: List<ChannelEntity>) {
        deleteBySource(source)
        insertAll(channels)
    }

    @Query("SELECT COUNT(*) FROM channels WHERE source = :source")
    suspend fun getCountBySource(source: String): Int
}
