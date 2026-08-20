package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String): String = playlistRepository.createPlaylist(name)
}
