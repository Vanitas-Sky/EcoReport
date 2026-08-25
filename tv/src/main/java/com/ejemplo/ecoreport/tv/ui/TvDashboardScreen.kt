package com.ejemplo.ecoreport.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.ejemplo.ecoreport.core.model.Incidence
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDashboardScreen(viewModel: TvIncidenceViewModel) {
    val allIncidences by viewModel.incidences.collectAsState()
    var selectedFilter by remember { mutableStateOf("Todos") }
    var selectedIncidence by remember { mutableStateOf<Incidence?>(null) }
    
    val menuFocusRequesters = remember { List(5) { FocusRequester() } }

    val filteredIncidences = remember(allIncidences, selectedFilter) {
        when (selectedFilter) {
            "Urgentes" -> allIncidences.filter { it.priority == "Alta" }
            "Pendientes" -> allIncidences.filter { it.status == "Pendiente" }
            "En Proceso" -> allIncidences.filter { it.status == "En Proceso" }
            "Resueltos" -> allIncidences.filter { it.status == "Resuelto" }
            else -> allIncidences
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        
        // MENÚ LATERAL
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Eco, null, tint = Color(0xFF81C784), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("ECO ADMIN", color = Color(0xFF81C784), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(20.dp))
            
            val menuOptions = listOf("Todos", "Urgentes", "Pendientes", "En Proceso", "Resueltos")
            menuOptions.forEachIndexed { index, option ->
                val isSelected = selectedFilter == option
                Surface(
                    onClick = { 
                        selectedFilter = option
                        selectedIncidence = null 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .focusRequester(menuFocusRequesters[index]),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.Transparent, 
                        focusedContainerColor = Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(
                            text = option, 
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.alpha(if (isSelected) 1f else 0.6f)
                        )
                    }
                }
            }
        }

        // DASHBOARD
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = "Gestión: $selectedFilter",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(Modifier.height(20.dp))

                if (filteredIncidences.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay registros", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredIncidences, key = { it.id }) { incidence ->
                            IncidenceTvCard(incidence) { selectedIncidence = incidence }
                        }
                    }
                }
            }

            // OVERLAY DE DETALLE CON FOCO AUTOMÁTICO
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
fun IncidenceDetailOverlay(incidence: Incidence, onClose: () -> Unit, onUpdateStatus: (String) -> Unit) {
    val initialFocusRequester = remember { FocusRequester() }

    // Forzamos el foco inicial al abrir el detalle
    LaunchedEffect(Unit) {
        delay(100) // Pequeño retardo para que la UI termine de renderizarse
        initialFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
        ) {
            AsyncImage(
                model = incidence.imageUrl, 
                contentDescription = null, 
                modifier = Modifier.fillMaxHeight().weight(1f), 
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .padding(24.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()), 
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = incidence.category.uppercase(), 
                    color = Color(0xFF81C784), 
                    style = MaterialTheme.typography.labelMedium, 
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = incidence.title, 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.Black, 
                    color = Color.White
                )
                
                Text(
                    text = "ESTADO: ${incidence.status.uppercase()}", 
                    color = Color.Gray, 
                    style = MaterialTheme.typography.labelSmall
                )
                
                Text(
                    text = incidence.description, 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = Color.LightGray
                )
                
                Spacer(Modifier.height(12.dp))
                
                Text("ACCIONES DISPONIBLES", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (incidence.status != "En Proceso") {
                        Button(
                            onClick = { onUpdateStatus("En Proceso") },
                            modifier = Modifier.focusRequester(initialFocusRequester) // FOCO INICIAL AQUÍ
                        ) {
                            Icon(Icons.Default.PendingActions, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("GESTIONAR", fontSize = 14.sp)
                        }
                    }
                    if (incidence.status != "Resuelto") {
                        Button(
                            onClick = { onUpdateStatus("Resuelto") },
                            colors = ButtonDefaults.colors(containerColor = Color(0xFF43A047), contentColor = Color.White),
                            modifier = if (incidence.status == "En Proceso") Modifier.focusRequester(initialFocusRequester) else Modifier
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("FINALIZAR", fontSize = 14.sp)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.padding(top = 8.dp).then(
                        if (incidence.status == "Resuelto") Modifier.focusRequester(initialFocusRequester) else Modifier
                    )
                ) {
                    Text("REGRESAR AL PANEL", fontSize = 14.sp)
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Reportado: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(incidence.timestamp))}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IncidenceTvCard(incidence: Incidence, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF81C784))))
    ) {
        Column {
            AsyncImage(model = incidence.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(12.dp).fillMaxSize()) {
                Text(
                    text = incidence.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis, 
                    color = Color.White
                )
                Text(text = incidence.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                val statusColor = when(incidence.status) {
                    "Pendiente" -> Color(0xFFFFB300)
                    "En Proceso" -> Color(0xFF1E88E5)
                    else -> Color(0xFF43A047)
                }
                Text(text = incidence.status.uppercase(), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Black)
            }
        }
    }
}
