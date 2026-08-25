package com.ejemplo.ecoreport.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
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
            "En Proceso" -> allIncidences.filter { it.status == "En Proceso" }
            "Resueltos" -> allIncidences.filter { it.status == "Resuelto" }
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
                DrawerItem(selectedFilter == "Todos", "Todos", Icons.Default.List) { selectedFilter = "Todos" }
                DrawerItem(selectedFilter == "Urgentes", "Urgentes", Icons.Default.Warning) { selectedFilter = "Urgentes" }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DrawerItem(selectedFilter == "Pendientes", "Pendientes", Icons.Default.Info) { selectedFilter = "Pendientes" }
                DrawerItem(selectedFilter == "En Proceso", "En Proceso", Icons.Default.Build) { selectedFilter = "En Proceso" }
                DrawerItem(selectedFilter == "Resueltos", "Resueltos", Icons.Default.Check) { selectedFilter = "Resueltos" }
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay reportes en esta sección", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredIncidences) { incidence ->
                            IncidenceTvCard(incidence) { selectedIncidence = incidence }
                        }
                    }
                }
            }

            // Detalle con botones de gestión
            selectedIncidence?.let { incidence ->
                IncidenceDetailOverlay(
                    incidence = incidence,
                    onClose = { selectedIncidence = null },
                    onUpdateStatus = { newStatus ->
                        viewModel.updateStatus(incidence.id, newStatus)
                        selectedIncidence = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NavigationDrawerScope.DrawerItem(selected: Boolean, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        leadingContent = { Icon(icon, contentDescription = null) }
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IncidenceDetailOverlay(
    incidence: Incidence, 
    onClose: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
            colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = incidence.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxHeight().weight(1.2f),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f).padding(40.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = incidence.category.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(text = incidence.title, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusTag(incidence.status)
                        if (incidence.location.isNotBlank()) {
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text(incidence.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(text = incidence.description, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.weight(1f))

                    Text("GESTIONAR REPORTE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (incidence.status != "En Proceso") {
                            Button(onClick = { onUpdateStatus("En Proceso") }) {
                                Icon(Icons.Default.Build, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Atender")
                            }
                        }
                        if (incidence.status != "Resuelto") {
                            Button(
                                onClick = { onUpdateStatus("Resuelto") },
                                colors = ButtonDefaults.colors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.Check, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Resolver")
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Reportado: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(incidence.timestamp))}", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = onClose) { Text("Cerrar") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatusTag(status: String) {
    val color = when (status) {
        "Pendiente" -> Color(0xFFFFD54F)
        "En Proceso" -> Color(0xFF64B5F6)
        else -> Color(0xFF81C784)
    }
    Surface(
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = color.copy(alpha = 0.2f)),
        onClick = {}
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
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
        modifier = Modifier.fillMaxWidth().height(280.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(model = incidence.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.padding(8.dp).background(priorityColor, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp).align(Alignment.TopEnd)) {
                    Text(text = incidence.priority, style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                Text(text = incidence.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = incidence.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val statusColor = when (incidence.status) {
                        "Pendiente" -> Color(0xFFFFD54F)
                        "En Proceso" -> Color(0xFF64B5F6)
                        else -> Color(0xFF81C784)
                    }
                    Text(text = incidence.status, style = MaterialTheme.typography.labelMedium, color = statusColor)
                    Text(text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(incidence.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
