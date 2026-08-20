package com.kunvarpreet.odette.data.repository

import com.kunvarpreet.odette.data.local.dao.PlaybackHistoryDao
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.HistoryRepository
import com.kunvarpreet.odette.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val musicRepository: MusicRepository
) : HistoryRepository {

    override fun getRecentlyPlayedSongs(limit: Int): Flow<List<Song>> {
        return combine(
            playbackHistoryDao.getRecentHistory(limit),
            musicRepository.getSongs()
        ) { historyItems, allSongs ->
            val songMap = allSongs.associateBy { it.id }
            historyItems.mapNotNull { item -> songMap[item.songId] }
        }
    }

    override suspend fun recordPlayback(songId: String) {
        playbackHistoryDao.recordPlayback(songId)
    }

    override suspend fun clearHistory() {
        playbackHistoryDao.clearHistory()
    }
}
