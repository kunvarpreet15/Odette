package com.kunvarpreet.odette.domain.model

data class Genre(
    val id: String,
    val name: String,
    val songCount: Int = 0
)
