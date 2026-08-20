package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import javax.inject.Inject

class ReorderPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String, fromPosition: Int, toPosition: Int) =
        playlistRepository.reorderPlaylist(playlistId, fromPosition, toPosition)
}
