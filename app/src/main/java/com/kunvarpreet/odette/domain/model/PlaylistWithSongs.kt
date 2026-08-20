package com.kunvarpreet.odette.domain.model

data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>
)
