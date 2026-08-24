package com.ejemplo.ecoreport.tv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.ejemplo.ecoreport.core.model.Incidence
import androidx.tv.foundation.lazy.grid.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TvDashboardScreen(viewModel: TvIncidenceViewModel) {
    val incidences by viewModel.incidences.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text(
                text = "EcoReport - Panel de Control Comunal",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (incidences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No hay reportes activos", style = MaterialTheme.typography.headlineMedium)
                }
            } else {
                TvLazyVerticalGrid(
                    columns = TvGridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(incidences) { incidence ->
                        IncidenceTvCard(incidence)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IncidenceTvCard(incidence: Incidence) {
    Surface(
        onClick = { /* Acción al seleccionar */ },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = incidence.status,
                style = MaterialTheme.typography.labelSmall,
                color = if (incidence.status == "Pendiente") Color.Yellow else Color.Green
            )
            Text(text = incidence.title, style = MaterialTheme.typography.headlineSmall)
            Text(text = incidence.category, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Prioridad: ${incidence.priority}",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}