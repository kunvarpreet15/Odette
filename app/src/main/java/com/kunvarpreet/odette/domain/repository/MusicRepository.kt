package com.kunvarpreet.odette.domain.repository

import com.kunvarpreet.odette.domain.model.Album
import com.kunvarpreet.odette.domain.model.Artist
import com.kunvarpreet.odette.domain.model.Genre
import com.kunvarpreet.odette.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getSongs(): Flow<List<Song>>
    suspend fun getSong(id: String): Song?
    fun search(query: String): Flow<List<Song>>
    fun getAlbums(): Flow<List<Album>>
    fun getArtists(): Flow<List<Artist>>
    fun getGenres(): Flow<List<Genre>>
    suspend fun refresh()
}
