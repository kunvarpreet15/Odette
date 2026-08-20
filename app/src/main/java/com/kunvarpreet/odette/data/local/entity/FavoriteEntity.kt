package com.kunvarpreet.odette.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val songId: String,
    val favoritedAt: Long = System.currentTimeMillis()
)
