package com.ejemplo.ecoreport.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejemplo.ecoreport.tv.ui.TvDashboardScreen
import com.ejemplo.ecoreport.tv.ui.TvIncidenceViewModel
import androidx.tv.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: TvIncidenceViewModel = viewModel()
                TvDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
