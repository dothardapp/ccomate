package com.iptv.ccomate.di

import android.content.Context
import com.iptv.ccomate.data.local.AppDatabase
import com.iptv.ccomate.data.EPGRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideEPGRepository(@ApplicationContext context: Context, database: AppDatabase): EPGRepository {
        return EPGRepository(context, database)
    }
}
