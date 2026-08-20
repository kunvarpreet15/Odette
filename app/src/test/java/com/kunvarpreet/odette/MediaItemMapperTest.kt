package com.kunvarpreet.odette

import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.player.toMediaItem
import com.kunvarpreet.odette.player.toSong
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaItemMapperTest {

    @Test
    fun songToMediaItem_preservesMetadata() {
        val song = Song(
            id = "42",
            title = "Clair de Lune",
            artist = "Claude Debussy",
            album = "Suite bergamasque",
            durationMs = 300000L,
            mediaUri = "https://example.com/audio.mp3",
            artworkUri = "https://example.com/artwork.jpg",
            albumArtist = "Claude Debussy",
            year = 1905,
            trackNumber = 3,
            genre = "Classical",
            albumId = 12345L
        )

        val mediaItem = song.toMediaItem()

        assertEquals("42", mediaItem.mediaId)
        assertEquals("Clair de Lune", mediaItem.mediaMetadata.title?.toString())
        assertEquals("Claude Debussy", mediaItem.mediaMetadata.artist?.toString())
        assertEquals("Suite bergamasque", mediaItem.mediaMetadata.albumTitle?.toString())
    }
}
