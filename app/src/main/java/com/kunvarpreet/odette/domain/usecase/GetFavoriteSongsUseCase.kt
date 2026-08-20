package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteSongsUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<Song>> = favoritesRepository.getFavoriteSongs()
}
