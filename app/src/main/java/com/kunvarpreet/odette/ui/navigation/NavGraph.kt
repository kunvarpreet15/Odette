package com.kunvarpreet.odette.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kunvarpreet.odette.domain.model.Song
import com.kunvarpreet.odette.ui.home.HomeScreen
import com.kunvarpreet.odette.ui.library.LibraryScreen
import com.kunvarpreet.odette.ui.main.MainViewModel
import com.kunvarpreet.odette.ui.playlists.FavoritesScreen
import com.kunvarpreet.odette.ui.playlists.PlaylistDetailScreen
import com.kunvarpreet.odette.ui.playlists.PlaylistsScreen
import com.kunvarpreet.odette.ui.playlists.RecentlyPlayedScreen
import com.kunvarpreet.odette.ui.search.SearchScreen
import com.kunvarpreet.odette.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
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
                recentlyPlayed = recentlyPlayed,
                favoriteSongIds = favoriteSongIds,
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
                onToggleFavorite = { songId -> viewModel.toggleFavorite(songId) },
                onAddToPlaylist = onAddToPlaylist,
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
                favoriteSongIds = favoriteSongIds,
                playerState = playerState,
                onSongSelected = { song -> viewModel.onSongSelected(song, songs) },
                onAlbumSelected = { album ->
                    val albumSongs = songs.filter { it.album.equals(album.title, ignoreCase = true) }
                    if (albumSongs.isNotEmpty()) viewModel.onSongSelected(albumSongs.first(), albumSongs)
                },
                onArtistSelected = { artist ->
                    val artistSongs = songs.filter { it.artist.equals(artist.name, ignoreCase = true) }
                    if (artistSongs.isNotEmpty()) viewModel.onSongSelected(artistSongs.first(), artistSongs)
                },
                onGenreSelected = { genre ->
                    val genreSongs = songs.filter { it.genre?.equals(genre.name, ignoreCase = true) == true }
                    if (genreSongs.isNotEmpty()) viewModel.onSongSelected(genreSongs.first(), genreSongs)
                },
                onToggleFavorite = { songId -> viewModel.toggleFavorite(songId) },
                onAddToPlaylist = onAddToPlaylist
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                songs = songs,
                favoriteSongs = favoriteSongs,
                recentlyPlayedSongs = recentlyPlayed,
                playlists = playlists,
                onPlayAll = { playlistSongs ->
                    if (playlistSongs.isNotEmpty()) viewModel.onSongSelected(playlistSongs.first(), playlistSongs)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                },
                onNavigateToRecentlyPlayed = {
                    navController.navigate(Screen.RecentlyPlayed.route)
                },
                onNavigateToPlaylistDetail = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onCreatePlaylist = { name ->
                    viewModel.createPlaylist(name)
                },
                onRenamePlaylist = { playlistId, newName ->
                    viewModel.renamePlaylist(playlistId, newName)
                },
                onDeletePlaylist = { playlistId ->
                    viewModel.deletePlaylist(playlistId)
                },
                onPlayPlaylistById = { playlistId ->
                    viewModel.playPlaylistById(playlistId, shuffle = false)
                },
                onShufflePlaylistById = { playlistId ->
                    viewModel.playPlaylistById(playlistId, shuffle = true)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                favoriteSongs = favoriteSongs,
                playerState = playerState,
                onBack = { navController.popBackStack() },
                onSongSelected = { song, list -> viewModel.onSongSelected(song, list) },
                onPlayAll = { list -> viewModel.playPlaylist(list) },
                onShuffle = { list -> viewModel.shufflePlaylist(list) },
                onToggleFavorite = { songId -> viewModel.toggleFavorite(songId) },
                onAddToPlaylist = onAddToPlaylist
            )
        }

        composable(Screen.RecentlyPlayed.route) {
            RecentlyPlayedScreen(
                recentlyPlayedSongs = recentlyPlayed,
                favoriteSongIds = favoriteSongIds,
                playerState = playerState,
                onBack = { navController.popBackStack() },
                onSongSelected = { song, list -> viewModel.onSongSelected(song, list) },
                onPlayAll = { list -> viewModel.playPlaylist(list) },
                onShuffle = { list -> viewModel.shufflePlaylist(list) },
                onToggleFavorite = { songId -> viewModel.toggleFavorite(songId) },
                onAddToPlaylist = onAddToPlaylist
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            val playlistWithSongsFlow = remember(playlistId) {
                viewModel.getPlaylistWithSongs(playlistId)
            }

            PlaylistDetailScreen(
                playlistId = playlistId,
                playlistWithSongsFlow = playlistWithSongsFlow,
                allSongs = songs,
                favoriteSongIds = favoriteSongIds,
                playerState = playerState,
                onBack = { navController.popBackStack() },
                onSongSelected = { song, list -> viewModel.onSongSelected(song, list) },
                onPlayAll = { list -> viewModel.playPlaylist(list) },
                onShuffle = { list -> viewModel.shufflePlaylist(list) },
                onRenamePlaylist = { id, newName -> viewModel.renamePlaylist(id, newName) },
                onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                onAddSongToPlaylist = { id, songId -> viewModel.addSongToPlaylist(id, songId) },
                onRemoveSongFromPlaylist = { id, songId -> viewModel.removeSongFromPlaylist(id, songId) },
                onReorderPlaylist = { id, fromPos, toPos -> viewModel.reorderPlaylist(id, fromPos, toPos) },
                onToggleFavorite = { songId -> viewModel.toggleFavorite(songId) },
                onAddToOtherPlaylist = onAddToPlaylist
            )
        }

        composable(Screen.Settings.route) {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()
            val skipForwardSeconds by viewModel.skipForwardSeconds.collectAsState()
            val skipBackwardSeconds by viewModel.skipBackwardSeconds.collectAsState()

            SettingsScreen(
                songCount = songs.size,
                albumCount = albums.size,
                artistCount = artists.size,
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                skipForwardSeconds = skipForwardSeconds,
                skipBackwardSeconds = skipBackwardSeconds,
                onThemeModeChanged = { viewModel.onThemeModeChanged(it) },
                onDynamicColorChanged = { viewModel.onDynamicColorChanged(it) },
                onSkipForwardChanged = { viewModel.onSkipForwardChanged(it) },
                onSkipBackwardChanged = { viewModel.onSkipBackwardChanged(it) },
                onRescanLibrary = { viewModel.refreshLibrary() }
            )
        }
    }
}
