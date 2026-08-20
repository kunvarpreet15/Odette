package com.kunvarpreet.odette.domain.repository

import com.kunvarpreet.odette.domain.model.Playlist
import com.kunvarpreet.odette.domain.model.PlaylistWithSongs
import com.kunvarpreet.odette.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: String): Playlist?
    fun getPlaylistWithSongs(playlistId: String): Flow<PlaylistWithSongs?>
    suspend fun createPlaylist(name: String): String
    suspend fun renamePlaylist(playlistId: String, newName: String)
    suspend fun deletePlaylist(playlistId: String)
    suspend fun addSongToPlaylist(playlistId: String, songId: String)
    suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>)
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    suspend fun reorderPlaylist(playlistId: String, fromPosition: Int, toPosition: Int)
}
