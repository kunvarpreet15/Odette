package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddSongToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String, songId: String) =
        playlistRepository.addSongToPlaylist(playlistId, songId)

    suspend fun addMultiple(playlistId: String, songIds: List<String>) =
        playlistRepository.addSongsToPlaylist(playlistId, songIds)
}
