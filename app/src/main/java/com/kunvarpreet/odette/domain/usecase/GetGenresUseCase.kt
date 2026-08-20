package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.model.Genre
import com.kunvarpreet.odette.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<List<Genre>> = repository.getGenres()
}
