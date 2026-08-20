package com.kunvarpreet.odette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            OdetteTheme {
                val viewModel: MainViewModel = hiltViewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}