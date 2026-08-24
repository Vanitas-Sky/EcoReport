package com.ejemplo.ecoreport.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.ejemplo.ecoreport.core.model.Incidence

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDashboardScreen(viewModel: TvIncidenceViewModel) {
    val allIncidences by viewModel.incidences.collectAsState()
    var selectedFilter by remember { mutableStateOf("Todos") }
    var selectedIncidence by remember { mutableStateOf<Incidence?>(null) }

    val filteredIncidences = remember(allIncidences, selectedFilter) {
        when (selectedFilter) {
            "Urgentes" -> allIncidences.filter { it.priority == "Alta" }
            "Pendientes" -> allIncidences.filter { it.status == "Pendiente" }
            else -> allIncidences
        }
    }

    NavigationDrawer(
        drawerContent = {
            Column(
                Modifier
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NavigationDrawerItem(
                    selected = selectedFilter == "Todos",
                    onClick = { selectedFilter = "Todos" },
                    leadingContent = { Icon(Icons.Default.List, contentDescription = null) }
                ) {
                    Text("Todos")
                }
                NavigationDrawerItem(
                    selected = selectedFilter == "Urgentes",
                    onClick = { selectedFilter = "Urgentes" },
                    leadingContent = { Icon(Icons.Default.Warning, contentDescription = null) }
                ) {
                    Text("Urgentes")
                }
                NavigationDrawerItem(
                    selected = selectedFilter == "Pendientes",
                    onClick = { selectedFilter = "Pendientes" },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                ) {
                    Text("Pendientes")
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(horizontal = 48.dp, vertical = 32.dp)) {
                Text(
                    text = "EcoReport - $selectedFilter",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (filteredIncidences.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay reportes en esta sección",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredIncidences) { incidence ->
                            IncidenceTvCard(
                                incidence = incidence,
                                onClick = { selectedIncidence = incidence }
                            )
                        }
                    }
                }
            }

            // Capa de Detalle (Overlay)
            selectedIncidence?.let { incidence ->
                IncidenceDetailOverlay(
                    incidence = incidence,
                    onClose = { selectedIncidence = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IncidenceDetailOverlay(incidence: Incidence, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClose,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Parte Izquierda: Imagen Grande
                AsyncImage(
                    model = incidence.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1.2f),
                    contentScale = ContentScale.Crop
                )

                // Parte Derecha: Información Detallada
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(40.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = incidence.category.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = incidence.title,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val priorityColor = when (incidence.priority) {
                        "Alta" -> Color(0xFFE57373)
                        "Media" -> Color(0xFFFFB74D)
                        else -> Color(0xFF81C784)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(priorityColor, RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prioridad ${incidence.priority}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = incidence.description,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Reportado: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(incidence.timestamp))}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Button(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IncidenceTvCard(incidence: Incidence, onClick: () -> Unit) {
    val priorityColor = when (incidence.priority) {
        "Alta" -> Color(0xFFE57373)
        "Media" -> Color(0xFFFFB74D)
        else -> Color(0xFF81C784)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = incidence.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(priorityColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = incidence.priority,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = incidence.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = incidence.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = if (incidence.status == "Pendiente") Color(0xFFFFD54F) else Color(0xFF81C784)
                    Text(
                        text = incidence.status,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                    
                    Text(
                        text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(incidence.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
