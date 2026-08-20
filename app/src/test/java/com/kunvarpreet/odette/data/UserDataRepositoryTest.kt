package com.kunvarpreet.odette.data

import com.kunvarpreet.odette.data.local.dao.FavoriteDao
import com.kunvarpreet.odette.data.local.dao.PlaybackHistoryDao
import com.kunvarpreet.odette.data.local.dao.PlaylistDao
import com.kunvarpreet.odette.data.local.dao.PlaylistWithSongCount
import com.kunvarpreet.odette.data.local.entity.FavoriteEntity
import com.kunvarpreet.odette.data.local.entity.PlaybackHistoryEntity
import com.kunvarpreet.odette.data.local.entity.PlaylistEntity
import com.kunvarpreet.odette.data.local.entity.PlaylistSongEntity
import com.kunvarpreet.odette.data.repository.FavoritesRepositoryImpl
import com.kunvarpreet.odette.data.repository.HistoryRepositoryImpl
import com.kunvarpreet.odette.data.repository.PlaylistRepositoryImpl
import com.kunvarpreet.odette.domain.model.Album
import com.kunvarpreet.odette.domain.model.Artist
import com.kunvarpreet.odette.domain.model.Genre
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserDataRepositoryTest {

    private lateinit var fakeMusicRepository: FakeMusicRepository
    private lateinit var fakeFavoriteDao: FakeFavoriteDao
    private lateinit var fakePlaylistDao: FakePlaylistDao
    private lateinit var fakeHistoryDao: FakePlaybackHistoryDao

    private lateinit var favoritesRepository: FavoritesRepositoryImpl
    private lateinit var playlistRepository: PlaylistRepositoryImpl
    private lateinit var historyRepository: HistoryRepositoryImpl

    private val sampleSongs = listOf(
        Song(id = "1", title = "Song A", artist = "Artist 1", album = "Album 1", durationMs = 180000, mediaUri = "content://1"),
        Song(id = "2", title = "Song B", artist = "Artist 2", album = "Album 2", durationMs = 210000, mediaUri = "content://2"),
        Song(id = "3", title = "Song C", artist = "Artist 1", album = "Album 1", durationMs = 240000, mediaUri = "content://3")
    )

    @Before
    fun setUp() {
        fakeMusicRepository = FakeMusicRepository(sampleSongs)
        fakeFavoriteDao = FakeFavoriteDao()
        fakePlaylistDao = FakePlaylistDao()
        fakeHistoryDao = FakePlaybackHistoryDao()

        favoritesRepository = FavoritesRepositoryImpl(fakeFavoriteDao, fakeMusicRepository)
        playlistRepository = PlaylistRepositoryImpl(fakePlaylistDao, fakeMusicRepository)
        historyRepository = HistoryRepositoryImpl(fakeHistoryDao, fakeMusicRepository)
    }

    @Test
    fun testFavoriteToggleAndRetrieve() = runBlocking {
        assertFalse(favoritesRepository.isFavorite("1"))
        
        val toggledOn = favoritesRepository.toggleFavorite("1")
        assertTrue(toggledOn)
        assertTrue(favoritesRepository.isFavorite("1"))

        val favoriteIds = favoritesRepository.getFavoriteSongIds().first()
        assertTrue(favoriteIds.contains("1"))

        val favoriteSongs = favoritesRepository.getFavoriteSongs().first()
        assertEquals(1, favoriteSongs.size)
        assertEquals("Song A", favoriteSongs.first().title)

        val toggledOff = favoritesRepository.toggleFavorite("1")
        assertFalse(toggledOff)
        assertFalse(favoritesRepository.isFavorite("1"))
    }

    @Test
    fun testPlaylistCreationAndSongManagement() = runBlocking {
        val playlistId = playlistRepository.createPlaylist("Chill Vibes")
        val playlists = playlistRepository.getPlaylists().first()
        assertEquals(1, playlists.size)
        assertEquals("Chill Vibes", playlists.first().name)

        playlistRepository.addSongToPlaylist(playlistId, "1")
        playlistRepository.addSongToPlaylist(playlistId, "3")

        val playlistWithSongs = playlistRepository.getPlaylistWithSongs(playlistId).first()
        assertNotNull(playlistWithSongs)
        assertEquals(2, playlistWithSongs!!.songs.size)
        assertEquals("Song A", playlistWithSongs.songs[0].title)
        assertEquals("Song C", playlistWithSongs.songs[1].title)

        // Reorder
        playlistRepository.reorderPlaylist(playlistId, 0, 1)
        val reordered = playlistRepository.getPlaylistWithSongs(playlistId).first()
        assertEquals("Song C", reordered!!.songs[0].title)
        assertEquals("Song A", reordered.songs[1].title)

        // Remove song
        playlistRepository.removeSongFromPlaylist(playlistId, "3")
        val afterRemoval = playlistRepository.getPlaylistWithSongs(playlistId).first()
        assertEquals(1, afterRemoval!!.songs.size)
        assertEquals("Song A", afterRemoval.songs[0].title)

        // Rename
        playlistRepository.renamePlaylist(playlistId, "Relaxing Mix")
        val renamed = playlistRepository.getPlaylistById(playlistId)
        assertEquals("Relaxing Mix", renamed?.name)

        // Delete
        playlistRepository.deletePlaylist(playlistId)
        val afterDelete = playlistRepository.getPlaylists().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun testPlaybackHistoryTracking() = runBlocking {
        historyRepository.recordPlayback("2")
        historyRepository.recordPlayback("1")
        historyRepository.recordPlayback("2") // played twice, latest timestamp

        val recentSongs = historyRepository.getRecentlyPlayedSongs(10).first()
        assertEquals(2, recentSongs.size)
        // Most recent should be song 2
        assertEquals("Song B", recentSongs[0].title)
        assertEquals("Song A", recentSongs[1].title)

        historyRepository.clearHistory()
        val cleared = historyRepository.getRecentlyPlayedSongs(10).first()
        assertTrue(cleared.isEmpty())
    }

    // Fake Implementations for unit testing repository layer
    private class FakeMusicRepository(private val songs: List<Song>) : MusicRepository {
        private val flow = MutableStateFlow(songs)
        override fun getSongs(): Flow<List<Song>> = flow
        override suspend fun getSong(id: String): Song? = songs.find { it.id == id }
        override fun search(query: String): Flow<List<Song>> = flow
        override fun getAlbums(): Flow<List<Album>> = MutableStateFlow(emptyList())
        override fun getArtists(): Flow<List<Artist>> = MutableStateFlow(emptyList())
        override fun getGenres(): Flow<List<Genre>> = MutableStateFlow(emptyList())
        override suspend fun refresh() {}
    }

    private class FakeFavoriteDao : FavoriteDao {
        private val favorites = mutableMapOf<String, FavoriteEntity>()
        private val flow = MutableStateFlow<List<String>>(emptyList())

        private fun updateFlow() {
            flow.value = favorites.values.sortedByDescending { it.favoritedAt }.map { it.songId }
        }

        override fun getFavoriteSongIds(): Flow<List<String>> = flow
        override fun isFavoriteFlow(songId: String): Flow<Boolean> = MutableStateFlow(favorites.containsKey(songId))
        override suspend fun isFavorite(songId: String): Boolean = favorites.containsKey(songId)
        override suspend fun insertFavorite(favorite: FavoriteEntity) {
            favorites[favorite.songId] = favorite
            updateFlow()
        }
        override suspend fun deleteFavorite(songId: String) {
            favorites.remove(songId)
            updateFlow()
        }
    }

    private class FakePlaylistDao : PlaylistDao {
        private val playlists = mutableMapOf<String, PlaylistEntity>()
        private val playlistSongs = mutableListOf<PlaylistSongEntity>()
        private val playlistsFlow = MutableStateFlow<List<PlaylistWithSongCount>>(emptyList())
        private val songsFlows = mutableMapOf<String, MutableStateFlow<List<PlaylistSongEntity>>>()

        private fun updatePlaylistsFlow() {
            playlistsFlow.value = playlists.values.map { p ->
                val count = playlistSongs.count { it.playlistId == p.id }
                PlaylistWithSongCount(p.id, p.name, p.createdAt, p.modifiedAt, count)
            }
        }

        private fun updateSongsFlow(playlistId: String) {
            val list = playlistSongs.filter { it.playlistId == playlistId }.sortedBy { it.position }
            songsFlows.getOrPut(playlistId) { MutableStateFlow(emptyList()) }.value = list
        }

        override fun getPlaylistsWithCount(): Flow<List<PlaylistWithSongCount>> = playlistsFlow
        override suspend fun getPlaylistById(id: String): PlaylistEntity? = playlists[id]
        override suspend fun insertPlaylist(playlist: PlaylistEntity) {
            playlists[playlist.id] = playlist
            updatePlaylistsFlow()
        }
        override suspend fun updatePlaylist(playlist: PlaylistEntity) {
            playlists[playlist.id] = playlist
            updatePlaylistsFlow()
        }
        override suspend fun renamePlaylist(playlistId: String, newName: String, modifiedAt: Long) {
            playlists[playlistId]?.let {
                playlists[playlistId] = it.copy(name = newName, modifiedAt = modifiedAt)
                updatePlaylistsFlow()
            }
        }
        override suspend fun updateModifiedAt(playlistId: String, modifiedAt: Long) {
            playlists[playlistId]?.let {
                playlists[playlistId] = it.copy(modifiedAt = modifiedAt)
                updatePlaylistsFlow()
            }
        }
        override suspend fun deletePlaylist(playlistId: String) {
            playlists.remove(playlistId)
            playlistSongs.removeAll { it.playlistId == playlistId }
            updatePlaylistsFlow()
            updateSongsFlow(playlistId)
        }
        override fun getPlaylistSongs(playlistId: String): Flow<List<PlaylistSongEntity>> {
            return songsFlows.getOrPut(playlistId) {
                MutableStateFlow(playlistSongs.filter { it.playlistId == playlistId }.sortedBy { it.position })
            }
        }
        override suspend fun getPlaylistSongsList(playlistId: String): List<PlaylistSongEntity> {
            return playlistSongs.filter { it.playlistId == playlistId }.sortedBy { it.position }
        }
        override suspend fun getMaxPosition(playlistId: String): Int? {
            return playlistSongs.filter { it.playlistId == playlistId }.maxOfOrNull { it.position }
        }
        override suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity) {
            playlistSongs.removeAll { it.playlistId == playlistSong.playlistId && it.songId == playlistSong.songId }
            playlistSongs.add(playlistSong)
            updatePlaylistsFlow()
            updateSongsFlow(playlistSong.playlistId)
        }
        override suspend fun insertPlaylistSongs(songs: List<PlaylistSongEntity>) {
            songs.forEach { insertPlaylistSong(it) }
        }
        override suspend fun deletePlaylistSong(playlistId: String, songId: String) {
            playlistSongs.removeAll { it.playlistId == playlistId && it.songId == songId }
            updatePlaylistsFlow()
            updateSongsFlow(playlistId)
        }
        override suspend fun deleteAllPlaylistSongs(playlistId: String) {
            playlistSongs.removeAll { it.playlistId == playlistId }
            updatePlaylistsFlow()
            updateSongsFlow(playlistId)
        }
    }

    private class FakePlaybackHistoryDao : PlaybackHistoryDao {
        private val history = mutableMapOf<String, PlaybackHistoryEntity>()
        private val flow = MutableStateFlow<List<PlaybackHistoryEntity>>(emptyList())
        private var timeOffset = 0L

        private fun updateFlow() {
            flow.value = history.values.sortedByDescending { it.lastPlayedAt }
        }

        override fun getRecentHistory(limit: Int): Flow<List<PlaybackHistoryEntity>> = flow
        override suspend fun getHistoryItem(songId: String): PlaybackHistoryEntity? = history[songId]
        override suspend fun insertHistory(entity: PlaybackHistoryEntity) {
            history[entity.songId] = entity
            updateFlow()
        }
        override suspend fun recordPlayback(songId: String, timestamp: Long) {
            timeOffset += 1000L
            val existing = history[songId]
            val newPlayCount = (existing?.playCount ?: 0) + 1
            history[songId] = PlaybackHistoryEntity(songId, timestamp + timeOffset, newPlayCount)
            updateFlow()
        }
        override suspend fun deleteHistoryItem(songId: String) {
            history.remove(songId)
            updateFlow()
        }
        override suspend fun clearHistory() {
            history.clear()
            updateFlow()
        }
    }
}
