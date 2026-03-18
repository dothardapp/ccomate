package com.iptv.ccomate.data

import android.util.Log
import com.iptv.ccomate.data.local.ChannelDao
import com.iptv.ccomate.data.local.ChannelEntity
import com.iptv.ccomate.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChannelRepository @Inject constructor(
    private val channelDao: ChannelDao,
    private val networkClient: NetworkClient,
    private val m3uParser: M3UParser
) {
    suspend fun getChannels(source: String, playlistUrl: String): List<Channel> =
        withContext(Dispatchers.IO) {
            // Cache-first: intentar desde Room
            val cached = channelDao.getChannelsBySource(source)
            if (cached.isNotEmpty()) {
                Log.d("ChannelRepository", "Cache hit for $source: ${cached.size} channels")
                // Lanzar actualización de fondo sin bloquear la UI
                refreshInBackground(source, playlistUrl)
                return@withContext cached.map { it.toChannel() }
            }

            // Cache miss: cargar desde red
            Log.d("ChannelRepository", "Cache miss for $source, fetching from network")
            fetchAndCache(source, playlistUrl)
        }

    private suspend fun fetchAndCache(source: String, playlistUrl: String): List<Channel> {
        val m3uContent = networkClient.fetchM3U(playlistUrl)
        val channels = m3uParser.parse(m3uContent)
        val entities = channels.map { it.toEntity(source) }
        channelDao.replaceChannels(source, entities)
        Log.d("ChannelRepository", "Cached ${channels.size} channels for $source")
        return channels
    }

    private suspend fun refreshInBackground(source: String, playlistUrl: String) {
        try {
            fetchAndCache(source, playlistUrl)
        } catch (e: Exception) {
            Log.w("ChannelRepository", "Background refresh failed for $source: ${e.message}")
        }
    }
}

private fun ChannelEntity.toChannel() = Channel(
    name = name,
    url = url,
    logo = logo,
    group = group,
    tvgId = tvgId
)

private fun Channel.toEntity(source: String) = ChannelEntity(
    name = name,
    url = url,
    logo = logo,
    group = group,
    tvgId = tvgId,
    source = source
)
