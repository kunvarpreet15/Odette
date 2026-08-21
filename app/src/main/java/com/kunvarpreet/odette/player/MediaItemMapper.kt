package com.kunvarpreet.odette.player

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.kunvarpreet.odette.domain.model.Song

object MediaItemMapper {
    const val EXTRA_DURATION_MS = "extra_duration_ms"
    const val EXTRA_ALBUM_ID = "extra_album_id"
    const val EXTRA_ARTWORK_URI = "extra_artwork_uri"
    const val EXTRA_ALBUM_ARTIST = "extra_album_artist"
    const val EXTRA_YEAR = "extra_year"
    const val EXTRA_TRACK_NUMBER = "extra_track_number"
    const val EXTRA_GENRE = "extra_genre"
    const val EXTRA_MEDIA_URI = "extra_media_uri"

    fun songToMediaItem(song: Song): MediaItem {
        val extras = Bundle().apply {
            putLong(EXTRA_DURATION_MS, song.durationMs)
            song.albumId?.let { putLong(EXTRA_ALBUM_ID, it) }
            song.artworkUri?.let { putString(EXTRA_ARTWORK_URI, it) }
            song.albumArtist?.let { putString(EXTRA_ALBUM_ARTIST, it) }
            song.year?.let { putInt(EXTRA_YEAR, it) }
            song.trackNumber?.let { putInt(EXTRA_TRACK_NUMBER, it) }
            song.genre?.let { putString(EXTRA_GENRE, it) }
            putString(EXTRA_MEDIA_URI, song.mediaUri)
        }

        val artworkUri = runCatching { song.artworkUri?.let { Uri.parse(it) } }.getOrNull()
        val mediaUri = runCatching { Uri.parse(song.mediaUri) }.getOrNull()

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(artworkUri)
            .setAlbumArtist(song.albumArtist)
            .setReleaseYear(song.year)
            .setTrackNumber(song.trackNumber)
            .setGenre(song.genre)
            .setExtras(extras)
            .build()

        val requestMetadata = MediaItem.RequestMetadata.Builder()
            .setMediaUri(mediaUri)
            .build()

        val builder = MediaItem.Builder()
            .setMediaId(song.id)
            .setRequestMetadata(requestMetadata)
            .setMediaMetadata(metadata)

        if (mediaUri != null) {
            builder.setUri(mediaUri)
        }

        return builder.build()
    }

    fun mediaItemToSong(mediaItem: MediaItem): Song {
        val metadata = mediaItem.mediaMetadata
        val extras = metadata.extras

        val title = metadata.title?.toString() ?: "Unknown Title"
        val artist = metadata.artist?.toString() ?: "Unknown Artist"
        val album = metadata.albumTitle?.toString() ?: "Unknown Album"
        val artworkUri = metadata.artworkUri?.toString() ?: extras?.getString(EXTRA_ARTWORK_URI)
        val albumArtist = metadata.albumArtist?.toString() ?: extras?.getString(EXTRA_ALBUM_ARTIST)
        val year = metadata.releaseYear ?: extras?.getInt(EXTRA_YEAR)?.takeIf { it > 0 }
        val trackNumber = metadata.trackNumber ?: extras?.getInt(EXTRA_TRACK_NUMBER)?.takeIf { it > 0 }
        val genre = metadata.genre?.toString() ?: extras?.getString(EXTRA_GENRE)
        val durationMs = extras?.getLong(EXTRA_DURATION_MS, 0L) ?: 0L
        val albumId = if (extras?.containsKey(EXTRA_ALBUM_ID) == true) extras.getLong(EXTRA_ALBUM_ID) else null
        val mediaUri = mediaItem.localConfiguration?.uri?.toString()
            ?: extras?.getString(EXTRA_MEDIA_URI)
            ?: mediaItem.requestMetadata.mediaUri?.toString()
            ?: ""

        return Song(
            id = mediaItem.mediaId,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            mediaUri = mediaUri,
            artworkUri = artworkUri,
            albumArtist = albumArtist,
            year = year,
            trackNumber = trackNumber,
            genre = genre,
            albumId = albumId
        )
    }
}

fun Song.toMediaItem(): MediaItem = MediaItemMapper.songToMediaItem(this)

fun MediaItem.toSong(): Song = MediaItemMapper.mediaItemToSong(this)
