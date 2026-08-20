package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import javax.inject.Inject

class RemoveSongFromPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String, songId: String) =
        playlistRepository.removeSongFromPlaylist(playlistId, songId)
}
