package com.iptv.ccomate.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("url_prefs", Context.MODE_PRIVATE)

    var tdaPlaylistUrl: String
        get() = prefs.getString(KEY_TDA_PLAYLIST, null) ?: AppConfig.TDA_PLAYLIST_URL
        set(value) = prefs.edit().putString(KEY_TDA_PLAYLIST, value).apply()

    var tdaEpgUrl: String
        get() = prefs.getString(KEY_TDA_EPG, null) ?: ""
        set(value) = prefs.edit().putString(KEY_TDA_EPG, value).apply()

    var plutoPlaylistUrl: String
        get() = prefs.getString(KEY_PLUTO_PLAYLIST, null) ?: AppConfig.PLUTO_PLAYLIST_URL
        set(value) = prefs.edit().putString(KEY_PLUTO_PLAYLIST, value).apply()

    var plutoEpgUrl: String
        get() = prefs.getString(KEY_PLUTO_EPG, null) ?: AppConfig.EPG_URL
        set(value) = prefs.edit().putString(KEY_PLUTO_EPG, value).apply()

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TDA_PLAYLIST = "tda_playlist_url"
        private const val KEY_TDA_EPG = "tda_epg_url"
        private const val KEY_PLUTO_PLAYLIST = "pluto_playlist_url"
        private const val KEY_PLUTO_EPG = "pluto_epg_url"

        private val PLAYLIST_REGEX = Regex(
            "^https?://.+\\.(m3u8?)(\\?.*)?$",
            RegexOption.IGNORE_CASE
        )
        private val EPG_REGEX = Regex(
            "^https?://.+\\.(xml|xmltv)(\\?.*)?$",
            RegexOption.IGNORE_CASE
        )

        fun validatePlaylistUrl(url: String): String? {
            if (url.isBlank()) return "La URL no puede estar vacía"
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                return "La URL debe comenzar con http:// o https://"
            if (!PLAYLIST_REGEX.matches(url))
                return "La URL debe terminar en .m3u o .m3u8"
            return null
        }

        fun validateEpgUrl(url: String): String? {
            if (url.isBlank()) return null // EPG vacío es válido (sin EPG)
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                return "La URL debe comenzar con http:// o https://"
            if (!EPG_REGEX.matches(url))
                return "La URL debe terminar en .xml o .xmltv"
            return null
        }
    }
}
