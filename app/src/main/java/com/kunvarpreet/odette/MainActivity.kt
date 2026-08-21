package com.kunvarpreet.odette

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kunvarpreet.odette.ui.main.MainScreen
import com.kunvarpreet.odette.ui.main.MainViewModel
import com.kunvarpreet.odette.ui.theme.OdetteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()

            // Request Audio Permission and Notification Permission on Launch
            val mediaPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    viewModel.onPermissionResult(isGranted)
                }
            )

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { /* Notification permission result */ }
            )

            LaunchedEffect(Unit) {
                val mediaPermission = viewModel.requiredPermission()
                val hasMediaPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    mediaPermission
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasMediaPermission) {
                    mediaPermissionLauncher.launch(mediaPermission)
                } else {
                    viewModel.onPermissionResult(true)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasNotificationPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            OdetteTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}