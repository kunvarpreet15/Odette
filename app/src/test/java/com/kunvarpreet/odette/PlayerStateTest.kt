package com.kunvarpreet.odette

import com.kunvarpreet.odette.domain.model.PlaybackStatus
import com.kunvarpreet.odette.domain.model.PlayerState
import com.kunvarpreet.odette.domain.model.RepeatMode
import com.kunvarpreet.odette.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    @Test
    fun playerState_defaultsAreCorrect() {
        val state = PlayerState()
        assertNull(state.currentSong)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPositionMs)
        assertEquals(0L, state.currentPosition)
        assertEquals(0L, state.durationMs)
        assertEquals(0L, state.duration)
        assertTrue(state.queue.isEmpty())
        assertEquals(-1, state.currentIndex)
        assertFalse(state.shuffleEnabled)
        assertEquals(RepeatMode.OFF, state.repeatMode)
        assertEquals(PlaybackStatus.IDLE, state.playbackStatus)
        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test
    fun playerState_customValues() {
        val song = Song(
            id = "101",
            title = "Test Track",
            artist = "Test Artist",
            album = "Test Album",
            durationMs = 240000L,
            mediaUri = "content://media/external/audio/media/101"
        )
        val song2 = Song(
            id = "102",
            title = "Second Track",
            artist = "Test Artist 2",
            album = "Test Album 2",
            durationMs = 180000L,
            mediaUri = "content://media/external/audio/media/102"
        )

        val queue = listOf(song, song2)
        val state = PlayerState(
            currentSong = song,
            isPlaying = true,
            currentPositionMs = 45000L,
            durationMs = 240000L,
            queue = queue,
            currentIndex = 0,
            shuffleEnabled = true,
            repeatMode = RepeatMode.ALL,
            playbackStatus = PlaybackStatus.READY,
            hasNext = true,
            hasPrevious = false
        )

        assertEquals(song, state.currentSong)
        assertTrue(state.isPlaying)
        assertEquals(45000L, state.currentPosition)
        assertEquals(240000L, state.duration)
        assertEquals(2, state.queue.size)
        assertEquals(0, state.currentIndex)
        assertTrue(state.shuffleEnabled)
        assertEquals(RepeatMode.ALL, state.repeatMode)
        assertEquals(PlaybackStatus.READY, state.playbackStatus)
        assertTrue(state.hasNext)
        assertFalse(state.hasPrevious)
    }
}
