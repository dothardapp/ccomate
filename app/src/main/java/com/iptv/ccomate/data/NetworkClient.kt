package com.iptv.ccomate.data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor() {
    val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        engine {
            maxConnectionsCount = 4
        }
    }

    suspend fun fetchM3U(url: String): String {
        return client.get(url).bodyAsText()
    }

    fun close() {
        client.close()
    }
}
