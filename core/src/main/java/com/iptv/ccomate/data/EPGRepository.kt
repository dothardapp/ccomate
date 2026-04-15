package com.iptv.ccomate.data

import android.content.Context
import android.util.Log
import com.iptv.ccomate.data.local.EPGDao
import com.iptv.ccomate.data.local.EPGEntity
import com.iptv.ccomate.model.EPGProgram
import com.iptv.ccomate.util.UrlPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

class EPGRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val epgDao: EPGDao,
    private val networkClient: NetworkClient,
    private val epgParser: EPGParser,
    private val urlPreferences: UrlPreferences
) {
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
            return@withContext fetchAndSaveAll()
        }
    }

    private suspend fun loadFromLocal(): Map<String, List<EPGProgram>> {
        val now = ZonedDateTime.now().toString()
        val entities = epgDao.getActiveAndUpcomingPrograms(now)
        return entities.groupBy { it.channelId }.mapValues { entry ->
            entry.value.map { it.toDomainModel() }
        }
    }

    private suspend fun fetchAndSaveAll(): Map<String, List<EPGProgram>> {
        try {
            val allParsedData = mutableMapOf<String, List<EPGProgram>>()

            val epgUrls = listOfNotNull(
                urlPreferences.plutoEpgUrl.ifBlank { null },
                urlPreferences.tdaEpgUrl.ifBlank { null }
            )

            for (url in epgUrls) {
                try {
                    val response = networkClient.client.get(url)
                    val parsedData = response.bodyAsChannel().toInputStream().use { stream ->
                        epgParser.parse(stream)
                    }
                    allParsedData.putAll(parsedData)
                    Log.d("EPGRepository", "Fetched EPG from $url: ${parsedData.size} channels")
                } catch (e: Exception) {
                    Log.w("EPGRepository", "Error fetching EPG from $url: ${e.message}")
                }
            }

            if (allParsedData.isNotEmpty()) {
                val entities = allParsedData.flatMap { (_, programs) ->
                    programs.map { it.toEntity() }
                }
                epgDao.replaceAll(entities)
                sharedPrefs.edit().putLong("last_update", System.currentTimeMillis()).apply()
            }

            return allParsedData.ifEmpty { loadFromLocal() }
        } catch (e: Exception) {
            Log.e("EPGRepository", "Error fetching EPG", e)
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
