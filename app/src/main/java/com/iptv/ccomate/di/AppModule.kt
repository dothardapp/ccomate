package com.iptv.ccomate.di

import android.content.Context
import androidx.room.Room
import com.iptv.ccomate.data.local.AppDatabase
import com.iptv.ccomate.data.local.ChannelDao
import com.iptv.ccomate.data.local.EPGDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ccomate_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideChannelDao(database: AppDatabase): ChannelDao {
        return database.channelDao()
    }

    @Provides
    @Singleton
    fun provideEPGDao(database: AppDatabase): EPGDao {
        return database.epgDao()
    }
}
