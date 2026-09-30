package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.InvestmentTrackerTheme
import com.example.ui.viewmodel.InvestmentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvestmentTrackerTheme {
                val viewModel: InvestmentViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
