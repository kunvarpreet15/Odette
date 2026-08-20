package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import javax.inject.Inject

class RenamePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String, newName: String) =
        playlistRepository.renamePlaylist(playlistId, newName)
}
