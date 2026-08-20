package com.kunvarpreet.odette.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunvarpreet.odette.domain.model.Album
import com.kunvarpreet.odette.domain.model.Artist
import com.kunvarpreet.odette.domain.model.Genre
import com.kunvarpreet.odette.domain.model.PlayerState
import com.kunvarpreet.odette.domain.model.Playlist
import com.kunvarpreet.odette.domain.model.PlaylistWithSongs
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.domain.usecase.AddSongToPlaylistUseCase
import com.kunvarpreet.odette.domain.usecase.CreatePlaylistUseCase
import com.kunvarpreet.odette.domain.usecase.DeletePlaylistUseCase
import com.kunvarpreet.odette.domain.usecase.GetAlbumsUseCase
import com.kunvarpreet.odette.domain.usecase.GetArtistsUseCase
import com.kunvarpreet.odette.domain.usecase.GetFavoriteSongIdsUseCase
import com.kunvarpreet.odette.domain.usecase.GetFavoriteSongsUseCase
import com.kunvarpreet.odette.domain.usecase.GetGenresUseCase
import com.kunvarpreet.odette.domain.usecase.GetPlaylistWithSongsUseCase
import com.kunvarpreet.odette.domain.usecase.GetPlaylistsUseCase
import com.kunvarpreet.odette.domain.usecase.GetRecentlyPlayedSongsUseCase
import com.kunvarpreet.odette.domain.usecase.GetSongsUseCase
import com.kunvarpreet.odette.domain.usecase.RefreshSongsUseCase
import com.kunvarpreet.odette.domain.usecase.RemoveSongFromPlaylistUseCase
import com.kunvarpreet.odette.domain.usecase.RenamePlaylistUseCase
import com.kunvarpreet.odette.domain.usecase.ReorderPlaylistUseCase
import com.kunvarpreet.odette.domain.usecase.SearchMusicUseCase
import com.kunvarpreet.odette.domain.usecase.ToggleFavoriteUseCase
import com.kunvarpreet.odette.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getSongsUseCase: GetSongsUseCase,
    getAlbumsUseCase: GetAlbumsUseCase,
    getArtistsUseCase: GetArtistsUseCase,
    getGenresUseCase: GetGenresUseCase,
    private val searchMusicUseCase: SearchMusicUseCase,
    private val refreshSongsUseCase: RefreshSongsUseCase,
    private val playerController: MusicPlayerController,
    getFavoriteSongIdsUseCase: GetFavoriteSongIdsUseCase,
    getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val getPlaylistWithSongsUseCase: GetPlaylistWithSongsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val renamePlaylistUseCase: RenamePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val reorderPlaylistUseCase: ReorderPlaylistUseCase,
    getRecentlyPlayedSongsUseCase: GetRecentlyPlayedSongsUseCase
) : ViewModel() {

    private val _hasPermission = MutableStateFlow(checkMediaPermission())
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    val songs: StateFlow<List<Song>> = getSongsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val albums: StateFlow<List<Album>> = getAlbumsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val artists: StateFlow<List<Artist>> = getArtistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val genres: StateFlow<List<Genre>> = getGenresUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteSongIds: StateFlow<Set<String>> = getFavoriteSongIdsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val favoriteSongs: StateFlow<List<Song>> = getFavoriteSongsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists: StateFlow<List<Playlist>> = getPlaylistsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentlyPlayed: StateFlow<List<Song>> = getRecentlyPlayedSongsUseCase(50)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerState: StateFlow<PlayerState> = playerController.playerState

    // Search with Debounce
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Song>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else searchMusicUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun requiredPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun checkMediaPermission(): Boolean {
        val permission = requiredPermission()
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionResult(isGranted: Boolean) {
        _hasPermission.value = isGranted
        if (isGranted) {
            refreshLibrary()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            refreshSongsUseCase()
        }
    }

    // Favorites
    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(songId)
        }
    }

    // Playlists
    fun createPlaylist(name: String, initialSongId: String? = null, onCreated: ((String) -> Unit)? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val playlistId = createPlaylistUseCase(name.trim())
            if (initialSongId != null) {
                addSongToPlaylistUseCase(playlistId, initialSongId)
            }
            onCreated?.invoke(playlistId)
        }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            renamePlaylistUseCase(playlistId, newName.trim())
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            deletePlaylistUseCase(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            addSongToPlaylistUseCase(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            removeSongFromPlaylistUseCase(playlistId, songId)
        }
    }

    fun reorderPlaylist(playlistId: String, fromPosition: Int, toPosition: Int) {
        viewModelScope.launch {
            reorderPlaylistUseCase(playlistId, fromPosition, toPosition)
        }
    }

    fun getPlaylistWithSongs(playlistId: String): Flow<PlaylistWithSongs?> {
        return getPlaylistWithSongsUseCase(playlistId)
    }

    // Playback actions
    fun onSongSelected(song: Song, playlist: List<Song> = songs.value) {
        playerController.playSong(song, playlist)
    }

    fun playPlaylist(playlistSongs: List<Song>, startIndex: Int = 0) {
        if (playlistSongs.isEmpty()) return
        val target = if (startIndex in playlistSongs.indices) playlistSongs[startIndex] else playlistSongs.first()
        playerController.playSong(target, playlistSongs)
    }

    fun shufflePlaylist(playlistSongs: List<Song>) {
        if (playlistSongs.isEmpty()) return
        val shuffled = playlistSongs.shuffled()
        playerController.playSong(shuffled.first(), shuffled)
    }

    fun playPlaylistById(playlistId: String, shuffle: Boolean = false) {
        viewModelScope.launch {
            getPlaylistWithSongsUseCase(playlistId).collect { playlistWithSongs ->
                val pSongs = playlistWithSongs?.songs ?: emptyList()
                if (pSongs.isNotEmpty()) {
                    if (shuffle) shufflePlaylist(pSongs) else playPlaylist(pSongs)
                }
            }
        }
    }

    fun onPlayQueueIndex(index: Int) {
        playerController.playQueueIndex(index)
    }

    fun onPlayPauseToggled() {
        val currentState = playerState.value
        if (currentState.isPlaying) {
            playerController.pause()
        } else {
            playerController.play()
        }
    }

    fun onStopClicked() {
        playerController.stop()
    }

    fun onNextClicked() {
        playerController.skipToNext()
    }

    fun onPreviousClicked() {
        playerController.skipToPrevious()
    }

    fun onSeekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun onToggleShuffle() {
        playerController.toggleShuffle()
    }

    fun onToggleRepeat() {
        playerController.toggleRepeatMode()
    }
}
