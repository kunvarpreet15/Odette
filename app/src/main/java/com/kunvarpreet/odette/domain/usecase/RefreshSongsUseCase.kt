package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.MusicRepository
import javax.inject.Inject

class RefreshSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke() = repository.refresh()
}
