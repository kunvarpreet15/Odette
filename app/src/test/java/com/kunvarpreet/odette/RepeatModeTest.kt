package com.kunvarpreet.odette

import androidx.media3.common.Player
import com.kunvarpreet.odette.domain.model.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatModeTest {

    @Test
    fun repeatMode_mapsFromMedia3Correctly() {
        assertEquals(RepeatMode.OFF, RepeatMode.fromMedia3(Player.REPEAT_MODE_OFF))
        assertEquals(RepeatMode.ONE, RepeatMode.fromMedia3(Player.REPEAT_MODE_ONE))
        assertEquals(RepeatMode.ALL, RepeatMode.fromMedia3(Player.REPEAT_MODE_ALL))
        assertEquals(RepeatMode.OFF, RepeatMode.fromMedia3(999)) // unknown fallback
    }

    @Test
    fun repeatMode_mapsToMedia3Correctly() {
        assertEquals(Player.REPEAT_MODE_OFF, RepeatMode.OFF.toMedia3())
        assertEquals(Player.REPEAT_MODE_ONE, RepeatMode.ONE.toMedia3())
        assertEquals(Player.REPEAT_MODE_ALL, RepeatMode.ALL.toMedia3())
    }

    @Test
    fun repeatMode_cyclesCorrectly() {
        var mode = RepeatMode.OFF
        mode = mode.next()
        assertEquals(RepeatMode.ALL, mode)
        mode = mode.next()
        assertEquals(RepeatMode.ONE, mode)
        mode = mode.next()
        assertEquals(RepeatMode.OFF, mode)
    }
}
