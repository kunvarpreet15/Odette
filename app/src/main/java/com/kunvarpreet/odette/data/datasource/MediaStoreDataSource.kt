package com.kunvarpreet.odette.data.datasource

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.kunvarpreet.odette.domain.model.Album
import com.kunvarpreet.odette.domain.model.Artist
import com.kunvarpreet.odette.domain.model.Genre
import com.kunvarpreet.odette.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun querySongs(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val contentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.GENRE)
            }
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.TRACK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.Audio.Media.ALBUM_ARTIST)
            }
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.DURATION} >= 1000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val genreColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                } else -1
                val albumArtistColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
                } else -1

                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn) ?: "Unknown Title"
                        val artist = cursor.getString(artistColumn) ?: "<unknown>"
                        val album = cursor.getString(albumColumn) ?: "Unknown Album"
                        val durationMs = cursor.getLong(durationColumn)
                        val albumId = cursor.getLong(albumIdColumn)

                        val year = if (yearColumn != -1 && !cursor.isNull(yearColumn)) cursor.getInt(yearColumn) else null
                        val track = if (trackColumn != -1 && !cursor.isNull(trackColumn)) cursor.getInt(trackColumn) else null
                        val genre = if (genreColumn != -1 && !cursor.isNull(genreColumn)) cursor.getString(genreColumn) else null
                        val albumArtist = if (albumArtistColumn != -1 && !cursor.isNull(albumArtistColumn)) cursor.getString(albumArtistColumn) else null

                        val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        val artworkUri: Uri = ContentUris.withAppendedId(
                            "content://media/external/audio/albumart".toUri(),
                            albumId
                        )

                        songList.add(
                            Song(
                                id = id.toString(),
                                title = title,
                                artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                                album = album,
                                durationMs = durationMs,
                                mediaUri = contentUri.toString(),
                                artworkUri = artworkUri.toString(),
                                albumArtist = albumArtist,
                                year = year,
                                trackNumber = track,
                                genre = genre,
                                albumId = albumId
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songList
    }

    suspend fun queryAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albumList = mutableListOf<Album>()
        val contentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )

        try {
            contentResolver.query(collection, projection, null, null, "${MediaStore.Audio.Albums.ALBUM} ASC")?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(albumColumn) ?: "Unknown Album"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val songCount = cursor.getInt(songCountColumn)

                    val artworkUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        id
                    )

                    albumList.add(
                        Album(
                            id = id.toString(),
                            title = title,
                            artist = artist,
                            songCount = songCount,
                            artworkUri = artworkUri.toString()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        albumList
    }

    suspend fun queryArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val artistList = mutableListOf<Artist>()
        val contentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Artists.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        try {
            contentResolver.query(collection, projection, null, null, "${MediaStore.Audio.Artists.ARTIST} ASC")?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
                val trackCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val trackCount = cursor.getInt(trackCountColumn)

                    artistList.add(
                        Artist(
                            id = id.toString(),
                            name = name,
                            songCount = trackCount
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        artistList
    }

    suspend fun queryGenres(): List<Genre> = withContext(Dispatchers.IO) {
        val genreList = mutableListOf<Genre>()
        val contentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Genres.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME
        )

        try {
            contentResolver.query(collection, projection, null, null, "${MediaStore.Audio.Genres.NAME} ASC")?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown Genre"

                    genreList.add(
                        Genre(
                            id = id.toString(),
                            name = name,
                            songCount = 0
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        genreList
    }
}
