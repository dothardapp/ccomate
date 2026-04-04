package com.iptv.ccomate.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// P2.3: OkHttpClient singleton — una sola instancia compartida en toda la app.
// Crear un OkHttpClient por ViewModel (o por playUrl) levanta un thread pool
// propio cada vez, lo que consume RAM innecesaria en dispositivos con ~9MB heap.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
}
