package com.example.ecoreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecoreport.ui.MobileIncidenceViewModel
import com.example.ecoreport.ui.ReportScreen
import com.example.ecoreport.ui.theme.EcoReportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoReportTheme {
                val viewModel: MobileIncidenceViewModel = viewModel()
                ReportScreen(viewModel = viewModel)
            }
        }
    }
}
