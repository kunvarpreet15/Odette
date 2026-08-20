package com.kunvarpreet.odette.domain.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: String? = null
)
