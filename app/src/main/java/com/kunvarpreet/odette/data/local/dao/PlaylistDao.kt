package com.kunvarpreet.odette.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kunvarpreet.odette.data.local.entity.PlaylistEntity
import com.kunvarpreet.odette.data.local.entity.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongCount(
    val id: String,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT p.id, p.name, p.createdAt, p.modifiedAt, COUNT(ps.songId) AS songCount
        FROM playlists p
        LEFT JOIN playlist_songs ps ON p.id = ps.playlistId
        GROUP BY p.id
        ORDER BY p.modifiedAt DESC
        """
    )
    fun getPlaylistsWithCount(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :newName, modifiedAt = :modifiedAt WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: String, newName: String, modifiedAt: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET modifiedAt = :modifiedAt WHERE id = :playlistId")
    suspend fun updateModifiedAt(playlistId: String, modifiedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getPlaylistSongs(playlistId: String): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getPlaylistSongsList(playlistId: String): List<PlaylistSongEntity>

    @Query("SELECT MAX(position) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(playlistSongs: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deletePlaylistSong(playlistId: String, songId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistSongs(playlistId: String)

    @Transaction
    suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        val currentMax = getMaxPosition(playlistId) ?: -1
        insertPlaylistSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = currentMax + 1
            )
        )
        updateModifiedAt(playlistId)
    }

    @Transaction
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        deletePlaylistSong(playlistId, songId)
        val remainingSongs = getPlaylistSongsList(playlistId)
        val updated = remainingSongs.mapIndexed { index, song ->
            song.copy(position = index)
        }
        insertPlaylistSongs(updated)
        updateModifiedAt(playlistId)
    }

    @Transaction
    suspend fun reorderPlaylist(playlistId: String, fromPosition: Int, toPosition: Int) {
        val songs = getPlaylistSongsList(playlistId).toMutableList()
        if (fromPosition in songs.indices && toPosition in songs.indices && fromPosition != toPosition) {
            val moved = songs.removeAt(fromPosition)
            songs.add(toPosition, moved)
            val updated = songs.mapIndexed { index, song ->
                song.copy(position = index)
            }
            insertPlaylistSongs(updated)
            updateModifiedAt(playlistId)
        }
    }
}
