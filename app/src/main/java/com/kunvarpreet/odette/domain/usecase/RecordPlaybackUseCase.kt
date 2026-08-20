package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.HistoryRepository
import javax.inject.Inject

class RecordPlaybackUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(songId: String) {
        historyRepository.recordPlayback(songId)
    }
}
