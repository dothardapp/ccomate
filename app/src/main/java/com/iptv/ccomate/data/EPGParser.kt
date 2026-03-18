package com.iptv.ccomate.data

import android.util.Log
import com.iptv.ccomate.model.EPGProgram
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EPGParser @Inject constructor() {
    // Format example: 20260112065500 -0300
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z", Locale.US)

    fun parse(inputStream: InputStream): Map<String, List<EPGProgram>> {
        val epgData = mutableMapOf<String, MutableList<EPGProgram>>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentChannelId: String? = null
            var currentStart: String? = null
            var currentStop: String? = null
            var currentTitle: String? = null
            var currentDesc: String? = null
            var currentIcon: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name == "programme") {
                            currentChannelId = parser.getAttributeValue(null, "channel")
                            currentStart = parser.getAttributeValue(null, "start")
                            currentStop = parser.getAttributeValue(null, "stop")
                            currentTitle = null
                            currentDesc = null
                            currentIcon = null
                        } else if (name == "title") {
                            currentTitle = parser.nextText()
                        } else if (name == "desc") {
                            currentDesc = parser.nextText()
                        } else if (name == "icon") {
                            currentIcon = parser.getAttributeValue(null, "src")
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name == "programme" &&
                                        currentChannelId != null &&
                                        currentStart != null &&
                                        currentStop != null
                        ) {
                            try {
                                val startTime = ZonedDateTime.parse(currentStart, DATE_FORMATTER)
                                val endTime = ZonedDateTime.parse(currentStop, DATE_FORMATTER)

                                val program =
                                        EPGProgram(
                                                channelId = currentChannelId,
                                                title = currentTitle ?: "Sin título",
                                                description = currentDesc,
                                                startTime = startTime,
                                                endTime = endTime,
                                                icon = currentIcon
                                        )

                                if (!epgData.containsKey(currentChannelId)) {
                                    epgData[currentChannelId] = mutableListOf()
                                }
                                epgData[currentChannelId]?.add(program)
                            } catch (e: Exception) {
                                Log.w(
                                        "EPGParser",
                                        "Error parsing program dates: $currentStart - $currentStop",
                                        e
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("EPGParser", "Error parsing EPG XML", e)
        }

        return epgData
    }
}
