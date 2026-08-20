package com.kunvarpreet.odette.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kunvarpreet.odette.data.local.dao.FavoriteDao
import com.kunvarpreet.odette.data.local.dao.PlaybackHistoryDao
import com.kunvarpreet.odette.data.local.dao.PlaylistDao
import com.kunvarpreet.odette.data.local.entity.FavoriteEntity
import com.kunvarpreet.odette.data.local.entity.PlaybackHistoryEntity
import com.kunvarpreet.odette.data.local.entity.PlaylistEntity
import com.kunvarpreet.odette.data.local.entity.PlaylistSongEntity

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        PlaybackHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OdetteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        const val DATABASE_NAME = "odette_user_data.db"
    }
}
