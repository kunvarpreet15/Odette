package com.kunvarpreet.odette.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kunvarpreet.odette.data.repository.UserPreferencesRepository
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
    private val recordPlaybackUseCase: RecordPlaybackUseCase,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var lastRecordedSongId: String? = null

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val pendingActions = mutableListOf<(MediaController) -> Unit>()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

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
                val actions = pendingActions.toList()
                pendingActions.clear()
                actions.forEach { action ->
                    mediaController?.let { action(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }

    private fun withController(action: (MediaController) -> Unit) {
        val controller = mediaController
        if (controller != null) {
            action(controller)
        } else {
            pendingActions.add(action)
        }
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
                        saveCurrentPlaybackState()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    saveCurrentPlaybackState()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
                if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                    saveCurrentPlaybackState()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorDesc = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Audio file not found or deleted"
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> "Unsupported or corrupt audio file"
                    PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "Storage permission revoked"
                    else -> error.localizedMessage ?: "Playback error encountered"
                }
                _errorMessage.value = errorDesc

                // Gracefully skip to next track if available
                if (controller.hasNextMediaItem()) {
                    controller.seekToNextMediaItem()
                    controller.play()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
                saveCurrentPlaybackState()
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
                saveCurrentPlaybackState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateState()
                saveCurrentPlaybackState()
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

    fun clearErrorMessage() {
        _errorMessage.value = null
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

    fun saveCurrentPlaybackState() {
        val controller = mediaController ?: return
        val current = _playerState.value
        val queueIds = current.queue.map { it.id }
        val songId = current.currentSong?.id
        val positionMs = controller.currentPosition.coerceAtLeast(0L)
        val shuffle = controller.shuffleModeEnabled
        val repeatOrdinal = RepeatMode.fromMedia3(controller.repeatMode).ordinal

        preferencesRepository.savePlaybackState(
            lastSongId = songId,
            queueSongIds = queueIds,
            positionMs = positionMs,
            shuffleEnabled = shuffle,
            repeatModeOrdinal = repeatOrdinal
        )
    }

    fun restoreSavedPlaybackState(availableSongs: List<Song>) {
        withController { controller ->
            if (controller.mediaItemCount > 0 || availableSongs.isEmpty()) return@withController

            val saved = preferencesRepository.savedPlaybackState.value
            if (saved.lastQueueSongIds.isEmpty() && saved.lastSongId == null) return@withController

            val songMap = availableSongs.associateBy { it.id }
            val restoredQueue = saved.lastQueueSongIds.mapNotNull { songMap[it] }

            if (restoredQueue.isNotEmpty()) {
                val targetIndex = if (saved.lastSongId != null) {
                    restoredQueue.indexOfFirst { it.id == saved.lastSongId }.coerceAtLeast(0)
                } else 0

                val mediaItems = restoredQueue.map { it.toMediaItem() }
                controller.setMediaItems(mediaItems, targetIndex, saved.lastPositionMs)
                controller.shuffleModeEnabled = saved.isShuffleEnabled
                val repeatMode = RepeatMode.entries.getOrElse(saved.repeatModeOrdinal) { RepeatMode.OFF }
                controller.repeatMode = repeatMode.toMedia3()
                controller.prepare()
                updateState()
            } else if (saved.lastSongId != null) {
                val lastSong = songMap[saved.lastSongId]
                if (lastSong != null) {
                    controller.setMediaItem(lastSong.toMediaItem(), saved.lastPositionMs)
                    controller.shuffleModeEnabled = saved.isShuffleEnabled
                    val repeatMode = RepeatMode.entries.getOrElse(saved.repeatModeOrdinal) { RepeatMode.OFF }
                    controller.repeatMode = repeatMode.toMedia3()
                    controller.prepare()
                    updateState()
                }
            }
        }
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        withController { controller ->
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
            saveCurrentPlaybackState()
        }
    }

    fun playQueueIndex(index: Int) {
        withController { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.seekToDefaultPosition(index)
                controller.play()
                updateState()
                saveCurrentPlaybackState()
            }
        }
    }

    fun addToQueue(song: Song) {
        withController { controller ->
            val wasEmpty = controller.mediaItemCount == 0
            controller.addMediaItem(song.toMediaItem())
            if (wasEmpty) {
                controller.prepare()
            }
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun addToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        withController { controller ->
            val wasEmpty = controller.mediaItemCount == 0
            controller.addMediaItems(songs.map { it.toMediaItem() })
            if (wasEmpty) {
                controller.prepare()
            }
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun playNext(song: Song) {
        withController { controller ->
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
            saveCurrentPlaybackState()
        }
    }

    fun removeFromQueue(index: Int) {
        withController { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.removeMediaItem(index)
                updateState()
                saveCurrentPlaybackState()
            }
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        withController { controller ->
            val count = controller.mediaItemCount
            if (fromIndex in 0 until count && toIndex in 0 until count && fromIndex != toIndex) {
                controller.moveMediaItem(fromIndex, toIndex)
                updateState()
                saveCurrentPlaybackState()
            }
        }
    }

    fun clearQueue() {
        withController { controller ->
            controller.clearMediaItems()
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun setShuffleEnabled(enabled: Boolean) {
        withController { controller ->
            controller.shuffleModeEnabled = enabled
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun toggleShuffle() {
        withController { controller ->
            setShuffleEnabled(!controller.shuffleModeEnabled)
        }
    }

    fun setRepeatMode(repeatMode: RepeatMode) {
        withController { controller ->
            controller.repeatMode = repeatMode.toMedia3()
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun toggleRepeatMode() {
        withController { controller ->
            val currentMode = RepeatMode.fromMedia3(controller.repeatMode)
            setRepeatMode(currentMode.next())
        }
    }

    fun play() {
        withController { controller ->
            controller.play()
            updateState()
        }
    }

    fun pause() {
        withController { controller ->
            controller.pause()
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun stop() {
        withController { controller ->
            controller.stop()
            updateState()
            saveCurrentPlaybackState()
        }
    }

    fun seekTo(positionMs: Long) {
        withController { controller ->
            controller.seekTo(positionMs)
            updateProgress()
            saveCurrentPlaybackState()
        }
    }

    fun seekForward(seconds: Int) {
        withController { controller ->
            val currentPos = controller.currentPosition
            val duration = controller.duration
            val target = (currentPos + seconds * 1000L).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
            controller.seekTo(target)
            updateProgress()
            saveCurrentPlaybackState()
        }
    }

    fun seekBackward(seconds: Int) {
        withController { controller ->
            val currentPos = controller.currentPosition
            val target = (currentPos - seconds * 1000L).coerceAtLeast(0L)
            controller.seekTo(target)
            updateProgress()
            saveCurrentPlaybackState()
        }
    }

    fun skipToNext() {
        withController { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
                updateState()
                saveCurrentPlaybackState()
            }
        }
    }

    fun skipToPrevious() {
        withController { controller ->
            if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
                updateState()
                saveCurrentPlaybackState()
            } else {
                controller.seekTo(0L)
                updateProgress()
                saveCurrentPlaybackState()
            }
        }
    }

    fun release() {
        stopProgressTracker()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}
