package com.kunvarpreet.odette.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kunvarpreet.odette.data.local.entity.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {

    @Query("SELECT * FROM playback_history ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE songId = :songId")
    suspend fun getHistoryItem(songId: String): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: PlaybackHistoryEntity)

    @Transaction
    suspend fun recordPlayback(songId: String, timestamp: Long = System.currentTimeMillis()) {
        val existing = getHistoryItem(songId)
        val newPlayCount = (existing?.playCount ?: 0) + 1
        insertHistory(
            PlaybackHistoryEntity(
                songId = songId,
                lastPlayedAt = timestamp,
                playCount = newPlayCount
            )
        )
    }

    @Query("DELETE FROM playback_history WHERE songId = :songId")
    suspend fun deleteHistoryItem(songId: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
