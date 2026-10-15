package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.InjectorScreen
import com.example.ui.InjectorViewModel
import com.example.ui.theme.FFDataInjectorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: InjectorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FFDataInjectorTheme {
                InjectorScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh package status whenever activity regains focus
        viewModel.refreshGameStatus()
    }
}
