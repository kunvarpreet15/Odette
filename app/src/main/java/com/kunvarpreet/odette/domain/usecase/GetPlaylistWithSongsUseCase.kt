package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.model.PlaylistWithSongs
import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistWithSongsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(playlistId: String): Flow<PlaylistWithSongs?> =
        playlistRepository.getPlaylistWithSongs(playlistId)
}
