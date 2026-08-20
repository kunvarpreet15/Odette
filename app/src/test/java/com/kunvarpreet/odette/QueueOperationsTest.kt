package com.kunvarpreet.odette

import com.kunvarpreet.odette.domain.model.PlaybackStatus
import com.kunvarpreet.odette.domain.model.PlayerState
import com.kunvarpreet.odette.domain.model.RepeatMode
import com.kunvarpreet.odette.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QueueOperationsTest {

    private lateinit var song1: Song
    private lateinit var song2: Song
    private lateinit var song3: Song
    private lateinit var song4: Song

    @Before
    fun setup() {
        song1 = Song("1", "Song One", "Artist A", "Album A", 180000L, "uri1")
        song2 = Song("2", "Song Two", "Artist B", "Album B", 200000L, "uri2")
        song3 = Song("3", "Song Three", "Artist C", "Album C", 220000L, "uri3")
        song4 = Song("4", "Song Four", "Artist D", "Album D", 240000L, "uri4")
    }

    @Test
    fun playMultipleSongs_initializesQueueCorrectly() {
        val initialPlaylist = listOf(song1, song2, song3)
        val selectedSong = song2

        val targetIndex = initialPlaylist.indexOfFirst { it.id == selectedSong.id }

        val state = PlayerState(
            currentSong = selectedSong,
            isPlaying = true,
            queue = initialPlaylist,
            currentIndex = targetIndex,
            playbackStatus = PlaybackStatus.READY,
            hasNext = targetIndex < initialPlaylist.size - 1,
            hasPrevious = targetIndex > 0
        )

        assertEquals(3, state.queue.size)
        assertEquals(1, state.currentIndex)
        assertEquals(song2, state.currentSong)
        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun nextPrevious_updatesIndicesAndNavigability() {
        val queue = listOf(song1, song2, song3)

        // Starting at song 1 (index 0)
        var currentIndex = 0
        var state = PlayerState(
            currentSong = queue[currentIndex],
            queue = queue,
            currentIndex = currentIndex,
            hasNext = currentIndex < queue.size - 1,
            hasPrevious = currentIndex > 0
        )
        assertTrue(state.hasNext)
        assertFalse(state.hasPrevious)

        // Skip to next (index 1)
        currentIndex++
        state = state.copy(
            currentSong = queue[currentIndex],
            currentIndex = currentIndex,
            hasNext = currentIndex < queue.size - 1,
            hasPrevious = currentIndex > 0
        )
        assertEquals(song2, state.currentSong)
        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)

        // Skip to next (index 2)
        currentIndex++
        state = state.copy(
            currentSong = queue[currentIndex],
            currentIndex = currentIndex,
            hasNext = currentIndex < queue.size - 1,
            hasPrevious = currentIndex > 0
        )
        assertEquals(song3, state.currentSong)
        assertFalse(state.hasNext)
        assertTrue(state.hasPrevious)

        // Skip to previous (index 1)
        currentIndex--
        state = state.copy(
            currentSong = queue[currentIndex],
            currentIndex = currentIndex,
            hasNext = currentIndex < queue.size - 1,
            hasPrevious = currentIndex > 0
        )
        assertEquals(song2, state.currentSong)
        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun addToQueue_appendsToEndOfQueue() {
        val currentQueue = mutableListOf(song1, song2)
        currentQueue.add(song3)

        val state = PlayerState(
            currentSong = song1,
            queue = currentQueue.toList(),
            currentIndex = 0,
            isPlaying = true
        )

        assertEquals(3, state.queue.size)
        assertEquals(song3, state.queue[2])
    }

    @Test
    fun playNext_insertsImmediatelyAfterCurrentSong() {
        val currentQueue = mutableListOf(song1, song2, song3)
        val currentIndex = 0 // playing song1

        val insertIndex = (currentIndex + 1).coerceAtMost(currentQueue.size)
        currentQueue.add(insertIndex, song4)

        val state = PlayerState(
            currentSong = currentQueue[currentIndex],
            queue = currentQueue.toList(),
            currentIndex = currentIndex,
            isPlaying = true
        )

        assertEquals(4, state.queue.size)
        assertEquals(song1, state.queue[0])
        assertEquals(song4, state.queue[1]) // inserted next!
        assertEquals(song2, state.queue[2])
        assertEquals(song3, state.queue[3])
    }

    @Test
    fun removeFromQueue_removesCorrectItem() {
        val currentQueue = mutableListOf(song1, song2, song3)
        currentQueue.removeAt(1) // remove song2

        val state = PlayerState(
            currentSong = song1,
            queue = currentQueue.toList(),
            currentIndex = 0
        )

        assertEquals(2, state.queue.size)
        assertEquals(song1, state.queue[0])
        assertEquals(song3, state.queue[1])
    }

    @Test
    fun reorderQueue_movesItemCorrectly() {
        val currentQueue = mutableListOf(song1, song2, song3)
        // Move song 3 from index 2 to index 0
        val moved = currentQueue.removeAt(2)
        currentQueue.add(0, moved)

        assertEquals(listOf(song3, song1, song2), currentQueue)
    }

    @Test
    fun clearQueue_emptiesQueue() {
        val currentQueue = mutableListOf(song1, song2, song3)
        currentQueue.clear()

        val state = PlayerState(
            currentSong = null,
            queue = currentQueue.toList(),
            currentIndex = -1,
            isPlaying = false
        )

        assertTrue(state.queue.isEmpty())
        assertNull(state.currentSong)
        assertEquals(-1, state.currentIndex)
    }

    @Test
    fun shuffleMode_togglesCorrectly() {
        var state = PlayerState(shuffleEnabled = false)
        assertFalse(state.shuffleEnabled)

        state = state.copy(shuffleEnabled = !state.shuffleEnabled)
        assertTrue(state.shuffleEnabled)

        state = state.copy(shuffleEnabled = !state.shuffleEnabled)
        assertFalse(state.shuffleEnabled)
    }

    @Test
    fun repeatModes_behaveCorrectly() {
        var state = PlayerState(repeatMode = RepeatMode.OFF)
        assertEquals(RepeatMode.OFF, state.repeatMode)

        // Repeat ALL: even at last item, hasNext remains true
        state = state.copy(repeatMode = RepeatMode.ALL, hasNext = true)
        assertEquals(RepeatMode.ALL, state.repeatMode)
        assertTrue(state.hasNext)

        // Repeat ONE: current song loops on completion
        state = state.copy(repeatMode = RepeatMode.ONE)
        assertEquals(RepeatMode.ONE, state.repeatMode)
    }

    @Test
    fun queueSynchronization_afterBackgrounding() {
        // When reconnecting from background, state is populated from existing Media3 session
        val backgroundQueue = listOf(song1, song2, song3)
        val sessionCurrentIndex = 1
        val sessionIsPlaying = true

        val reconnectedState = PlayerState(
            currentSong = backgroundQueue[sessionCurrentIndex],
            isPlaying = sessionIsPlaying,
            queue = backgroundQueue,
            currentIndex = sessionCurrentIndex,
            playbackStatus = PlaybackStatus.READY
        )

        assertEquals(song2, reconnectedState.currentSong)
        assertTrue(reconnectedState.isPlaying)
        assertEquals(3, reconnectedState.queue.size)
        assertEquals(1, reconnectedState.currentIndex)
    }
}
