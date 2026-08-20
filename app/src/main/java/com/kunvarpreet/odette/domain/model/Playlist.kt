package com.kunvarpreet.odette.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)
