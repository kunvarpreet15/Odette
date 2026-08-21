package com.kunvarpreet.odette.player

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.kunvarpreet.odette.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player = exoPlayer

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sessionCallback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val availablePlayerCommands = connectionResult.availablePlayerCommands
                    .buildUpon()
                    .addAllCommands()
                    .build()
                val sessionCommands = connectionResult.availableSessionCommands
                    .buildUpon()
                    .build()
                return MediaSession.ConnectionResult.accept(
                    sessionCommands,
                    availablePlayerCommands
                )
            }

            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: List<MediaItem>
            ): ListenableFuture<List<MediaItem>> {
                val updatedMediaItems = mediaItems.map { mediaItem ->
                    if (mediaItem.localConfiguration == null) {
                        val uriString = mediaItem.mediaMetadata.extras?.getString(MediaItemMapper.EXTRA_MEDIA_URI)
                        val uri = if (!uriString.isNullOrBlank()) {
                            Uri.parse(uriString)
                        } else if (mediaItem.requestMetadata.mediaUri != null) {
                            mediaItem.requestMetadata.mediaUri
                        } else {
                            val id = mediaItem.mediaId.toLongOrNull()
                            if (id != null) {
                                android.content.ContentUris.withAppendedId(
                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            } else null
                        }

                        if (uri != null) {
                            mediaItem.buildUpon()
                                .setUri(uri)
                                .build()
                        } else {
                            mediaItem
                        }
                    } else {
                        mediaItem
                    }
                }
                return Futures.immediateFuture(updatedMediaItems)
            }

            override fun onSetMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: List<MediaItem>,
                startIndex: Int,
                startPositionMs: Long
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                val updatedMediaItems = mediaItems.map { mediaItem ->
                    if (mediaItem.localConfiguration == null) {
                        val uriString = mediaItem.mediaMetadata.extras?.getString(MediaItemMapper.EXTRA_MEDIA_URI)
                        val uri = if (!uriString.isNullOrBlank()) {
                            Uri.parse(uriString)
                        } else if (mediaItem.requestMetadata.mediaUri != null) {
                            mediaItem.requestMetadata.mediaUri
                        } else {
                            val id = mediaItem.mediaId.toLongOrNull()
                            if (id != null) {
                                android.content.ContentUris.withAppendedId(
                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )
                            } else null
                        }

                        if (uri != null) {
                            mediaItem.buildUpon()
                                .setUri(uri)
                                .build()
                        } else {
                            mediaItem
                        }
                    } else {
                        mediaItem
                    }
                }
                return Futures.immediateFuture(
                    MediaSession.MediaItemsWithStartPosition(updatedMediaItems, startIndex, startPositionMs)
                )
            }
        }

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(sessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        player?.let {
            if (!it.playWhenReady || it.mediaItemCount == 0) {
                stopSelf()
            }
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        player = null
        super.onDestroy()
    }
}
