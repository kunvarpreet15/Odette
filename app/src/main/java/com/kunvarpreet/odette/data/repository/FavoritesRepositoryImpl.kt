package com.kunvarpreet.odette.data.repository

import com.kunvarpreet.odette.data.local.dao.FavoriteDao
import com.kunvarpreet.odette.data.local.entity.FavoriteEntity
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.FavoritesRepository
import com.kunvarpreet.odette.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val musicRepository: MusicRepository
) : FavoritesRepository {

    override fun getFavoriteSongIds(): Flow<Set<String>> {
        return favoriteDao.getFavoriteSongIds().map { it.toSet() }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return combine(
            favoriteDao.getFavoriteSongIds(),
            musicRepository.getSongs()
        ) { favoriteIds, allSongs ->
            val songMap = allSongs.associateBy { it.id }
            favoriteIds.mapNotNull { id -> songMap[id] }
        }
    }

    override suspend fun isFavorite(songId: String): Boolean {
        return favoriteDao.isFavorite(songId)
    }

    override suspend fun toggleFavorite(songId: String): Boolean {
        return favoriteDao.toggleFavorite(songId)
    }

    override suspend fun addFavorite(songId: String) {
        favoriteDao.insertFavorite(FavoriteEntity(songId = songId))
    }

    override suspend fun removeFavorite(songId: String) {
        favoriteDao.deleteFavorite(songId)
    }
}
