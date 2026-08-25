package com.ejemplo.ecoreport.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ejemplo.ecoreport.tv.ui.TvDashboardScreen
import com.ejemplo.ecoreport.tv.ui.TvIncidenceViewModel
import com.ejemplo.ecoreport.tv.ui.theme.EcoReportTvTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicialización segura de Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("Firebase", "Error initializing Firebase: ${e.message}")
        }

        setContent {
            EcoReportTvTheme {
                val viewModel: TvIncidenceViewModel = viewModel()
                TvDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
