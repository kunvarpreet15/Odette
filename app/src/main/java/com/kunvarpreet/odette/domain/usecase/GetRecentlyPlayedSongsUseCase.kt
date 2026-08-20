package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyPlayedSongsUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<Song>> =
        historyRepository.getRecentlyPlayedSongs(limit)
}
