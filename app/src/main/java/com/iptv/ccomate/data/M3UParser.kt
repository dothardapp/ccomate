package com.iptv.ccomate.data

import com.iptv.ccomate.model.Channel

object M3UParser {
    fun parse(m3uContent: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = m3uContent.lines()
        var currentName = ""
        var currentLogo: String? = null
        var currentGroup: String? = null

        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("#EXTINF")) {
                // Extraer el nombre después de la última coma
                val nameMatch = Regex(".*,(.*)").find(line)
                currentName = nameMatch?.groupValues?.get(1)?.trim() ?: "Sin nombre"

                val logoMatch = Regex("tvg-logo=\"(.*?)\"").find(line)
                currentLogo = logoMatch?.groupValues?.get(1)

                val groupMatch = Regex("group-title=\"(.*?)\"").find(line)
                currentGroup = groupMatch?.groupValues?.get(1)
            } else if (line.startsWith("http")) {
                channels.add(Channel(currentName, line.trim(), currentLogo, currentGroup))
            }
        }

        return channels
    }
}