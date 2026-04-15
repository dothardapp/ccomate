package com.iptv.ccomate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface EPGDao {

    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId AND endTime > :currentTime ORDER BY startTime ASC LIMIT :limit")
    suspend fun getProgramsForChannel(channelId: String, currentTime: String, limit: Int = 20): List<EPGEntity>

    @Query("SELECT * FROM epg_programs WHERE endTime > :currentTime AND startTime <= :currentTime ORDER BY channelId, startTime ASC")
    suspend fun getActivePrograms(currentTime: String): List<EPGEntity>

    @Query("SELECT * FROM epg_programs WHERE endTime > :currentTime ORDER BY channelId, startTime ASC")
    suspend fun getActiveAndUpcomingPrograms(currentTime: String): List<EPGEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<EPGEntity>)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM epg_programs")
    suspend fun getCount(): Int

    @Transaction
    suspend fun replaceAll(programs: List<EPGEntity>) {
        deleteAll()
        insertAll(programs)
    }
}
