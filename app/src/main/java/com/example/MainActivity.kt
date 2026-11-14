package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DebloaterScreen
import com.example.ui.DebloaterViewModel
import com.example.ui.theme.Y5DebloaterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Y5DebloaterTheme {
                val viewModel: DebloaterViewModel = viewModel()
                DebloaterScreen(viewModel = viewModel)
            }
        }
    }
}
