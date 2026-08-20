package com.kunvarpreet.odette.ui.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kunvarpreet.odette.ui.components.MiniPlayer
import com.kunvarpreet.odette.ui.navigation.BottomNavBar
import com.kunvarpreet.odette.ui.navigation.NavGraph
import com.kunvarpreet.odette.ui.player.FullPlayerSheet

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val playerState by viewModel.playerState.collectAsState()
    var showFullPlayer by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
        }
    )

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MiniPlayer(
                    playerState = playerState,
                    onPlayPauseClicked = { viewModel.onPlayPauseToggled() },
                    onNextClicked = { viewModel.onNextClicked() },
                    onExpand = { showFullPlayer = true }
                )
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            viewModel = viewModel,
            onRequestPermission = {
                permissionLauncher.launch(viewModel.requiredPermission())
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }

    if (showFullPlayer && playerState.currentSong != null) {
        FullPlayerSheet(
            playerState = playerState,
            onDismiss = { showFullPlayer = false },
            onPlayPauseClicked = { viewModel.onPlayPauseToggled() },
            onNextClicked = { viewModel.onNextClicked() },
            onPreviousClicked = { viewModel.onPreviousClicked() },
            onSeekTo = { viewModel.onSeekTo(it) },
            onToggleShuffle = { viewModel.onToggleShuffle() },
            onToggleRepeat = { viewModel.onToggleRepeat() },
            onQueueItemClicked = { index -> viewModel.onPlayQueueIndex(index) }
        )
    }
}
