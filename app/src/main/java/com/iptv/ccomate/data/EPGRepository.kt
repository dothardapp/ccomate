package com.iptv.ccomate.data

import android.content.Context
import android.util.Log
import com.iptv.ccomate.data.local.AppDatabase
import com.iptv.ccomate.data.local.EPGEntity
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.util.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EPGRepository(private val context: Context) {
    private val epgDao = AppDatabase.getDatabase(context).epgDao()
    private val sharedPrefs = context.getSharedPreferences("epg_prefs", Context.MODE_PRIVATE)
    
    private val CACHE_TIMEOUT_MS = 60 * 60 * 1000 // 60 minutes

    suspend fun getEPGData(forceRefresh: Boolean = false): Map<String, List<EPGProgram>> = withContext(Dispatchers.IO) {
        val lastUpdate = sharedPrefs.getLong("last_update", 0L)
        val currentTime = System.currentTimeMillis()
        val isCacheValid = (currentTime - lastUpdate) < CACHE_TIMEOUT_MS && !forceRefresh
        val hasData = epgDao.getCount() > 0

        if (isCacheValid && hasData) {
            Log.d("EPGRepository", "Using cached EPG data")
            return@withContext loadFromLocal()
        } else {
            Log.d("EPGRepository", "Fetching fresh EPG data")
            return@withContext fetchAndSave()
        }
    }

    private suspend fun loadFromLocal(): Map<String, List<EPGProgram>> {
        val entities = epgDao.getAllPrograms()
        return entities.groupBy { it.channelId }.mapValues { entry ->
            entry.value.map { it.toDomainModel() }
        }
    }

    private suspend fun fetchAndSave(): Map<String, List<EPGProgram>> {
        try {
            val xmlContent = NetworkClient.fetchM3U(AppConfig.EPG_URL)
            val parsedData = EPGParser.parse(xmlContent)
            
            val entities = parsedData.flatMap { (_, programs) ->
                programs.map { it.toEntity() }
            }

            epgDao.deleteAll()
            epgDao.insertAll(entities)
            
            sharedPrefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
            
            return parsedData
        } catch (e: Exception) {
            Log.e("EPGRepository", "Error fetching EPG", e)
            // If fetch fails, try to return whatever we have in local even if expired
            return loadFromLocal()
        }
    }

    private fun EPGEntity.toDomainModel(): EPGProgram {
        return EPGProgram(
            channelId = this.channelId,
            title = this.title,
            description = this.description,
            startTime = ZonedDateTime.parse(this.startTime),
            endTime = ZonedDateTime.parse(this.endTime),
            icon = this.icon
        )
    }

    private fun EPGProgram.toEntity(): EPGEntity {
        return EPGEntity(
            channelId = this.channelId,
            title = this.title,
            description = this.description,
            startTime = this.startTime.toString(), // ISO 8601
            endTime = this.endTime.toString(),     // ISO 8601
            icon = this.icon
        )
    }
}
