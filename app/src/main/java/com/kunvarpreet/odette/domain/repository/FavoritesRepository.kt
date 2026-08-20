package com.kunvarpreet.odette.domain.repository

import com.kunvarpreet.odette.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavoriteSongIds(): Flow<Set<String>>
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun isFavorite(songId: String): Boolean
    suspend fun toggleFavorite(songId: String): Boolean
    suspend fun addFavorite(songId: String)
    suspend fun removeFavorite(songId: String)
}
