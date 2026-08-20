package com.kunvarpreet.odette.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kunvarpreet.odette.ui.home.HomeScreen
import com.kunvarpreet.odette.ui.library.LibraryScreen
import com.kunvarpreet.odette.ui.main.MainViewModel
import com.kunvarpreet.odette.ui.playlists.PlaylistsScreen
import com.kunvarpreet.odette.ui.search.SearchScreen
import com.kunvarpreet.odette.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playerState = playerState,
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission,
                onSongSelected = { song -> viewModel.onSongSelected(song, songs) },
                onAlbumSelected = { album ->
                    val albumSongs = songs.filter { it.album.equals(album.title, ignoreCase = true) }
                    if (albumSongs.isNotEmpty()) viewModel.onSongSelected(albumSongs.first(), albumSongs)
                },
                onArtistSelected = { artist ->
                    val artistSongs = songs.filter { it.artist.equals(artist.name, ignoreCase = true) }
                    if (artistSongs.isNotEmpty()) viewModel.onSongSelected(artistSongs.first(), artistSongs)
                },
                onRefresh = { viewModel.refreshLibrary() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                searchResults = searchResults,
                allAlbums = albums,
                allArtists = artists,
                playerState = playerState,
                onSongSelected = { song -> viewModel.onSongSelected(song, songs) }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                genres = genres,
                playerState = playerState,
                onSongSelected = { song -> viewModel.onSongSelected(song, songs) }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                songs = songs,
                onPlayAll = { playlist ->
                    if (playlist.isNotEmpty()) viewModel.onSongSelected(playlist.first(), playlist)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                songCount = songs.size,
                albumCount = albums.size,
                artistCount = artists.size,
                onRescanLibrary = { viewModel.refreshLibrary() }
            )
        }
    }
}
