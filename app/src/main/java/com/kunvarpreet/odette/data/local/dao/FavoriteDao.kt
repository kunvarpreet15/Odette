package com.kunvarpreet.odette.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kunvarpreet.odette.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT songId FROM favorites ORDER BY favoritedAt DESC")
    fun getFavoriteSongIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavoriteFlow(songId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: String)

    @Transaction
    suspend fun toggleFavorite(songId: String): Boolean {
        return if (isFavorite(songId)) {
            deleteFavorite(songId)
            false
        } else {
            insertFavorite(FavoriteEntity(songId = songId))
            true
        }
    }
}
