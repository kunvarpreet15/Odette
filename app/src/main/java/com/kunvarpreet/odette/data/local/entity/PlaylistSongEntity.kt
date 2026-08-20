package com.kunvarpreet.odette.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["songId"]),
        Index(value = ["playlistId", "position"])
    ]
)
data class PlaylistSongEntity(
    val playlistId: String,
    val songId: String,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
)
