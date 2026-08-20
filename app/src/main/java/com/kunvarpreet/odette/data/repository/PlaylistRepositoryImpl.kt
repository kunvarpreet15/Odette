package com.kunvarpreet.odette.data.repository

import com.kunvarpreet.odette.data.local.dao.PlaylistDao
import com.kunvarpreet.odette.data.local.entity.PlaylistEntity
import com.kunvarpreet.odette.data.local.entity.PlaylistSongEntity
import com.kunvarpreet.odette.domain.model.Playlist
import com.kunvarpreet.odette.domain.model.PlaylistWithSongs
import com.kunvarpreet.odette.domain.repository.MusicRepository
import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val musicRepository: MusicRepository
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylistsWithCount().map { list ->
            list.map { item ->
                Playlist(
                    id = item.id,
                    name = item.name,
                    songCount = item.songCount,
                    createdAt = item.createdAt,
                    modifiedAt = item.modifiedAt
                )
            }
        }
    }

    override suspend fun getPlaylistById(id: String): Playlist? {
        val entity = playlistDao.getPlaylistById(id) ?: return null
        val count = playlistDao.getPlaylistSongsList(id).size
        return Playlist(
            id = entity.id,
            name = entity.name,
            songCount = count,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt
        )
    }

    override fun getPlaylistWithSongs(playlistId: String): Flow<PlaylistWithSongs?> {
        return combine(
            playlistDao.getPlaylistSongs(playlistId),
            musicRepository.getSongs()
        ) { playlistSongs, allSongs ->
            val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return@combine null
            val songMap = allSongs.associateBy { it.id }
            val sortedSongs = playlistSongs
                .sortedBy { it.position }
                .mapNotNull { ps -> songMap[ps.songId] }

            PlaylistWithSongs(
                playlist = Playlist(
                    id = playlistEntity.id,
                    name = playlistEntity.name,
                    songCount = sortedSongs.size,
                    createdAt = playlistEntity.createdAt,
                    modifiedAt = playlistEntity.modifiedAt
                ),
                songs = sortedSongs
            )
        }
    }

    override suspend fun createPlaylist(name: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        playlistDao.insertPlaylist(
            PlaylistEntity(
                id = id,
                name = name.trim(),
                createdAt = now,
                modifiedAt = now
            )
        )
        return id
    }

    override suspend fun renamePlaylist(playlistId: String, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName.trim())
    }

    override suspend fun deletePlaylist(playlistId: String) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        playlistDao.addSongToPlaylist(playlistId, songId)
    }

    override suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>) {
        val currentMax = playlistDao.getMaxPosition(playlistId) ?: -1
        var pos = currentMax + 1
        val list = songIds.map { songId ->
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = pos++
            )
        }
        playlistDao.insertPlaylistSongs(list)
        playlistDao.updateModifiedAt(playlistId)
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun reorderPlaylist(playlistId: String, fromPosition: Int, toPosition: Int) {
        playlistDao.reorderPlaylist(playlistId, fromPosition, toPosition)
    }
}
