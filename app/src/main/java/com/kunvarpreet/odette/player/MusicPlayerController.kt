package com.kunvarpreet.odette.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kunvarpreet.odette.domain.model.PlaybackStatus
import com.kunvarpreet.odette.domain.model.PlayerState
import com.kunvarpreet.odette.domain.model.RepeatMode
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.usecase.RecordPlaybackUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordPlaybackUseCase: RecordPlaybackUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var lastRecordedSongId: String? = null

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupControllerListener()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupControllerListener() {
        val controller = mediaController ?: return
        controller.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                updateState()
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED) ||
                    events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
                ) {
                    if (player.isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
                mediaItem?.mediaId?.let { songId ->
                    if (songId.isNotBlank() && songId != lastRecordedSongId) {
                        lastRecordedSongId = songId
                        scope.launch(Dispatchers.IO) {
                            recordPlaybackUseCase(songId)
                        }
                    }
                }
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                updateState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateState()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updateState()
            }
        })

        updateState()
        if (controller.isPlaying) {
            startProgressTracker()
        }
    }

    private fun startProgressTracker() {
        if (progressJob?.isActive == true) return
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                updateProgress()
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
        updateProgress()
    }

    private fun updateProgress() {
        val controller = mediaController ?: return
        val current = _playerState.value
        _playerState.value = current.copy(
            currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
            durationMs = controller.duration.coerceAtLeast(0L)
        )
    }

    fun updateState() {
        val controller = mediaController ?: return
        val count = controller.mediaItemCount
        val queue = if (count > 0) {
            (0 until count).map { index ->
                controller.getMediaItemAt(index).toSong()
            }
        } else {
            emptyList()
        }

        val currentIndex = if (count > 0) controller.currentMediaItemIndex else -1
        val currentSong = if (currentIndex in queue.indices) {
            queue[currentIndex]
        } else {
            controller.currentMediaItem?.toSong()
        }

        val status = when (controller.playbackState) {
            Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
            Player.STATE_READY -> PlaybackStatus.READY
            Player.STATE_ENDED -> PlaybackStatus.ENDED
            else -> PlaybackStatus.IDLE
        }

        _playerState.value = PlayerState(
            currentSong = currentSong,
            isPlaying = controller.isPlaying,
            currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
            durationMs = controller.duration.coerceAtLeast(0L),
            queue = queue,
            currentIndex = currentIndex,
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = RepeatMode.fromMedia3(controller.repeatMode),
            playbackStatus = status,
            hasNext = controller.hasNextMediaItem(),
            hasPrevious = controller.hasPreviousMediaItem()
        )
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        val controller = mediaController ?: return
        if (playlist.isNotEmpty()) {
            val mediaItems = playlist.map { it.toMediaItem() }
            val targetIndex = playlist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            controller.setMediaItems(mediaItems, targetIndex, 0L)
        } else {
            controller.setMediaItem(song.toMediaItem())
        }

        controller.prepare()
        controller.play()
        updateState()
    }

    fun playQueueIndex(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.seekToDefaultPosition(index)
            controller.play()
            updateState()
        }
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        val wasEmpty = controller.mediaItemCount == 0
        controller.addMediaItem(song.toMediaItem())
        if (wasEmpty) {
            controller.prepare()
        }
        updateState()
    }

    fun addToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return
        val wasEmpty = controller.mediaItemCount == 0
        controller.addMediaItems(songs.map { it.toMediaItem() })
        if (wasEmpty) {
            controller.prepare()
        }
        updateState()
    }

    fun playNext(song: Song) {
        val controller = mediaController ?: return
        val count = controller.mediaItemCount
        if (count == 0) {
            controller.setMediaItem(song.toMediaItem())
            controller.prepare()
            controller.play()
        } else {
            val nextIndex = (controller.currentMediaItemIndex + 1).coerceAtMost(count)
            controller.addMediaItem(nextIndex, song.toMediaItem())
        }
        updateState()
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index in 0 until controller.mediaItemCount) {
            controller.removeMediaItem(index)
            updateState()
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val controller = mediaController ?: return
        val count = controller.mediaItemCount
        if (fromIndex in 0 until count && toIndex in 0 until count && fromIndex != toIndex) {
            controller.moveMediaItem(fromIndex, toIndex)
            updateState()
        }
    }

    fun clearQueue() {
        val controller = mediaController ?: return
        controller.clearMediaItems()
        updateState()
    }

    fun setShuffleEnabled(enabled: Boolean) {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = enabled
        updateState()
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        setShuffleEnabled(!controller.shuffleModeEnabled)
    }

    fun setRepeatMode(repeatMode: RepeatMode) {
        val controller = mediaController ?: return
        controller.repeatMode = repeatMode.toMedia3()
        updateState()
    }

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        val currentMode = RepeatMode.fromMedia3(controller.repeatMode)
        setRepeatMode(currentMode.next())
    }

    fun play() {
        mediaController?.play()
        updateState()
    }

    fun pause() {
        mediaController?.pause()
        updateState()
    }

    fun stop() {
        mediaController?.stop()
        updateState()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        updateProgress()
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            updateState()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            updateState()
        } else {
            controller.seekTo(0L)
            updateProgress()
        }
    }

    fun release() {
        stopProgressTracker()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}

