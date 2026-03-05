
package com.iptv.ccomate.data.local

import androidx.room.*

@Dao
interface EPGDao {
    @Query("SELECT * FROM epg_programs")
    suspend fun getAllPrograms(): List<EPGEntity>

    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId")
    suspend fun getProgramsForChannel(channelId: String): List<EPGEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<EPGEntity>)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM epg_programs")
    suspend fun getCount(): Int
}
