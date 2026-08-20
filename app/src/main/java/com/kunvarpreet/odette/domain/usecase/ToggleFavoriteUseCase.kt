package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke(songId: String): Boolean = favoritesRepository.toggleFavorite(songId)
}
