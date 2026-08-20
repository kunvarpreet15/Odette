package com.kunvarpreet.odette.data.repository

import com.kunvarpreet.odette.data.datasource.MediaStoreDataSource
import com.kunvarpreet.odette.domain.model.Album
import com.kunvarpreet.odette.domain.model.Artist
import com.kunvarpreet.odette.domain.model.Genre
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource
) : MusicRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    private val _albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    private val _artistsFlow = MutableStateFlow<List<Artist>>(emptyList())
    private val _genresFlow = MutableStateFlow<List<Genre>>(emptyList())

    init {
        scope.launch {
            refresh()
        }
    }

    override fun getSongs(): Flow<List<Song>> = _songsFlow.asStateFlow()

    override suspend fun getSong(id: String): Song? {
        return _songsFlow.value.find { it.id == id }
    }

    override fun search(query: String): Flow<List<Song>> {
        return _songsFlow.map { songs ->
            if (query.isBlank()) songs
            else songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true) ||
                        (it.genre?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override fun getAlbums(): Flow<List<Album>> = _albumsFlow.asStateFlow()

    override fun getArtists(): Flow<List<Artist>> = _artistsFlow.asStateFlow()

    override fun getGenres(): Flow<List<Genre>> = _genresFlow.asStateFlow()

    override suspend fun refresh() {
        _songsFlow.value = mediaStoreDataSource.querySongs()
        _albumsFlow.value = mediaStoreDataSource.queryAlbums()
        _artistsFlow.value = mediaStoreDataSource.queryArtists()
        _genresFlow.value = mediaStoreDataSource.queryGenres()
    }
}
