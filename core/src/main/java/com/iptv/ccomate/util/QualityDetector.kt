package com.iptv.ccomate.util

/**
 * Utilidad para detectar la calidad de video a partir del nombre y URL del canal.
 *
 * Busca patrones conocidos (4K, FHD, HD, SD) con regex case-insensitive
 * en una combinación de nombre + URL del canal.
 *
 * Prioriza la calidad más alta encontrada (4K > FHD > HD).
 *
 * @see <a href="https://github.com/dothardapp/ccomate/issues/84">Issue #84</a>
 */
object QualityDetector {

    /**
     * Niveles de calidad detectables, con su label para mostrar en chips UI.
     */
    enum class Quality(val label: String) {
        HD("HD"),
        FHD("FHD"),
        UHD_4K("4K")
    }

    /**
     * Patrones ordenados por prioridad (mayor calidad primero).
     * Se evalúan en orden: la primera coincidencia gana.
     */
    private val patterns = listOf(
        Quality.UHD_4K to Regex("""(?i)\b(4K|UHD|2160)\b"""),
        Quality.FHD    to Regex("""(?i)\b(FHD|FULLHD|FULL[_\-\s]?HD|1080)\b"""),
        Quality.HD     to Regex("""(?i)\b(HD|720)\b"""),
    )

    /**
     * Detecta la calidad del canal combinando nombre y URL.
     *
     * @param name  Nombre del canal (ej: "ESPN HD", "Discovery FHD")
     * @param url   URL del stream (ej: "...1080.m3u8", "...4k/...")
     * @return La [Quality] más alta detectada, o `null` si no se detecta ninguna.
     */
    fun detect(name: String, url: String): Quality? {
        val combined = "$name $url"
        return patterns.firstOrNull { (_, regex) ->
            regex.containsMatchIn(combined)
        }?.first
    }
}
