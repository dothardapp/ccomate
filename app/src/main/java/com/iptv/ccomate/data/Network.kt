package com.iptv.ccomate.data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText

object Network {
    val client = HttpClient(CIO)

    suspend fun fetchM3U(url: String): String {
        return client.get(url).bodyAsText()
    }
}
