package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMusicUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(query: String): Flow<List<Song>> = repository.search(query)
}
