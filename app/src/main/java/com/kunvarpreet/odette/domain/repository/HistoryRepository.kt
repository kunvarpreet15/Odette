package com.kunvarpreet.odette.domain.repository

import com.kunvarpreet.odette.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<Song>>
    suspend fun recordPlayback(songId: String)
    suspend fun clearHistory()
}
