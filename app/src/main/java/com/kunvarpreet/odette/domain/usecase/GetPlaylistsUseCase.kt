package com.kunvarpreet.odette.domain.usecase

import com.kunvarpreet.odette.domain.model.Playlist
import com.kunvarpreet.odette.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = playlistRepository.getPlaylists()
}
