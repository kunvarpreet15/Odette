package com.kunvarpreet.odette.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class PlaybackSavedState(
    val lastSongId: String? = null,
    val lastQueueSongIds: List<String> = emptyList(),
    val lastPositionMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatModeOrdinal: Int = 0
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("odette_preferences", Context.MODE_PRIVATE)

    // Theme Mode
    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Dynamic Color
    private val _dynamicColor = MutableStateFlow(loadDynamicColor())
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    // Skip Intervals (in seconds)
    private val _skipForwardSeconds = MutableStateFlow(loadSkipForwardSeconds())
    val skipForwardSeconds: StateFlow<Int> = _skipForwardSeconds.asStateFlow()

    private val _skipBackwardSeconds = MutableStateFlow(loadSkipBackwardSeconds())
    val skipBackwardSeconds: StateFlow<Int> = _skipBackwardSeconds.asStateFlow()

    // Saved Playback State
    private val _savedPlaybackState = MutableStateFlow(loadSavedPlaybackState())
    val savedPlaybackState: StateFlow<PlaybackSavedState> = _savedPlaybackState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }

    private fun loadDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }

    fun setSkipForwardSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_SKIP_FORWARD, seconds).apply()
        _skipForwardSeconds.value = seconds
    }

    private fun loadSkipForwardSeconds(): Int {
        return prefs.getInt(KEY_SKIP_FORWARD, 10)
    }

    fun setSkipBackwardSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_SKIP_BACKWARD, seconds).apply()
        _skipBackwardSeconds.value = seconds
    }

    private fun loadSkipBackwardSeconds(): Int {
        return prefs.getInt(KEY_SKIP_BACKWARD, 10)
    }

    fun savePlaybackState(
        lastSongId: String?,
        queueSongIds: List<String>,
        positionMs: Long,
        shuffleEnabled: Boolean,
        repeatModeOrdinal: Int
    ) {
        val serializedQueue = queueSongIds.joinToString(separator = ",")
        prefs.edit()
            .putString(KEY_LAST_SONG_ID, lastSongId)
            .putString(KEY_LAST_QUEUE, serializedQueue)
            .putLong(KEY_LAST_POSITION, positionMs)
            .putBoolean(KEY_SHUFFLE_ENABLED, shuffleEnabled)
            .putInt(KEY_REPEAT_MODE, repeatModeOrdinal)
            .apply()

        _savedPlaybackState.value = PlaybackSavedState(
            lastSongId = lastSongId,
            lastQueueSongIds = queueSongIds,
            lastPositionMs = positionMs,
            isShuffleEnabled = shuffleEnabled,
            repeatModeOrdinal = repeatModeOrdinal
        )
    }

    private fun loadSavedPlaybackState(): PlaybackSavedState {
        val lastSongId = prefs.getString(KEY_LAST_SONG_ID, null)
        val rawQueue = prefs.getString(KEY_LAST_QUEUE, "") ?: ""
        val queueIds = if (rawQueue.isNotBlank()) rawQueue.split(",").filter { it.isNotBlank() } else emptyList()
        val positionMs = prefs.getLong(KEY_LAST_POSITION, 0L)
        val shuffle = prefs.getBoolean(KEY_SHUFFLE_ENABLED, false)
        val repeatMode = prefs.getInt(KEY_REPEAT_MODE, 0)

        return PlaybackSavedState(
            lastSongId = lastSongId,
            lastQueueSongIds = queueIds,
            lastPositionMs = positionMs,
            isShuffleEnabled = shuffle,
            repeatModeOrdinal = repeatMode
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_SKIP_FORWARD = "pref_skip_forward"
        private const val KEY_SKIP_BACKWARD = "pref_skip_backward"
        private const val KEY_LAST_SONG_ID = "pref_last_song_id"
        private const val KEY_LAST_QUEUE = "pref_last_queue"
        private const val KEY_LAST_POSITION = "pref_last_position"
        private const val KEY_SHUFFLE_ENABLED = "pref_shuffle_enabled"
        private const val KEY_REPEAT_MODE = "pref_repeat_mode"
    }
}
