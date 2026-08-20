package com.kunvarpreet.odette.di

import android.content.Context
import androidx.room.Room
import com.kunvarpreet.odette.data.local.OdetteDatabase
import com.kunvarpreet.odette.data.local.dao.FavoriteDao
import com.kunvarpreet.odette.data.local.dao.PlaybackHistoryDao
import com.kunvarpreet.odette.data.local.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOdetteDatabase(
        @ApplicationContext context: Context
    ): OdetteDatabase {
        return Room.databaseBuilder(
            context,
            OdetteDatabase::class.java,
            OdetteDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFavoriteDao(database: OdetteDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    fun providePlaylistDao(database: OdetteDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun providePlaybackHistoryDao(database: OdetteDatabase): PlaybackHistoryDao {
        return database.playbackHistoryDao()
    }
}
