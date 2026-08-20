package com.kunvarpreet.odette.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val songId: String,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1
)
