package com.iptv.ccomate.util

object AppConfig {
    // Servidor de Contenido (IP 232)
    private const val CONTENT_SERVER = "http://10.224.24.232:8081"
    const val BANNER_URL = "$CONTENT_SERVER/iptvbanner.png"
    const val TDA_PLAYLIST_URL = "$CONTENT_SERVER/tda.m3u"
    const val PLUTO_PLAYLIST_URL = "$CONTENT_SERVER/tuner-1-playlist.m3u"
    const val PLUTO_PLAYLIST_GROK_URL = "$CONTENT_SERVER/playlist.m3u"
    const val EPG_URL = "$CONTENT_SERVER/epg.xml"

    // Servidor de API (IP 233)
    private const val API_BASE_URL = "http://10.224.24.233:8000/api"
    const val REGISTER_DEVICE_URL = "$API_BASE_URL/register-device"
    const val CHECK_SUBSCRIPTION_URL = "$API_BASE_URL/check-subscription"

    // Headers y Seguridad
    const val VIDEO_REFERER = "https://ccomate.iptv.com"
    const val VIDEO_ORIGIN = "https://ccomate.iptv.com"
}
