package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteSongIdsUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(): Flow<Set<String>> = favoritesRepository.getFavoriteSongIds()
}
