package com.kunvarpreet.odette.domain.model

import androidx.media3.common.Player

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE;

    companion object {
        fun fromMedia3(@Player.RepeatMode mode: Int): RepeatMode {
            return when (mode) {
                Player.REPEAT_MODE_ONE -> ONE
                Player.REPEAT_MODE_ALL -> ALL
                else -> OFF
            }
        }
    }

    fun toMedia3(): Int {
        return when (this) {
            OFF -> Player.REPEAT_MODE_OFF
            ONE -> Player.REPEAT_MODE_ONE
            ALL -> Player.REPEAT_MODE_ALL
        }
    }

    fun next(): RepeatMode {
        return when (this) {
            OFF -> ALL
            ALL -> ONE
            ONE -> OFF
        }
    }
}

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackStatus: PlaybackStatus = PlaybackStatus.IDLE,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
) {
    val currentPosition: Long get() = currentPositionMs
    val duration: Long get() = durationMs
}
