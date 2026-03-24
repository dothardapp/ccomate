package com.iptv.ccomate.data

import com.iptv.ccomate.model.Channel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor() {
    private val nameRegex = Regex(".*,(.*)")
    private val logoRegex = Regex("""tvg-logo="(.*?)"""")
    private val groupRegex = Regex("""group-title="(.*?)"""")
    private val channelIdRegex = Regex("""channel-id="(.*?)"""")
    private val tvgIdRegex = Regex("""tvg-id="(.*?)"""")

    fun parse(m3uContent: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = m3uContent.lines()
        var currentName = ""
        var currentLogo: String? = null
        var currentGroup: String? = null
        var currentTvgId: String? = null

        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("#EXTINF")) {
                currentName = nameRegex.find(line)?.groupValues?.get(1)?.trim() ?: "Sin nombre"
                currentLogo = logoRegex.find(line)?.groupValues?.get(1)
                currentGroup = groupRegex.find(line)?.groupValues?.get(1)

                // Prioritize channel-id, fallback to tvg-id
                val extractedId = channelIdRegex.find(line)?.groupValues?.get(1)
                currentTvgId = extractedId ?: tvgIdRegex.find(line)?.groupValues?.get(1)
            } else if (!line.startsWith("#") && line.contains("://")) {
                // Acepta cualquier protocolo: http, https, udp, rtp, rtsp, etc.
                // Lineas que empiezan con # son comentarios o directivas M3U.
                channels.add(
                        Channel(currentName, line.trim(), currentLogo, currentGroup, currentTvgId)
                )
            }
        }

        return channels
    }
}
