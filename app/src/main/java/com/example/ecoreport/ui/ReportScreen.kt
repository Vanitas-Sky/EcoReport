package com.example.ecoreport.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ejemplo.ecoreport.core.model.Incidence
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: MobileIncidenceViewModel) {
    val allIncidences by viewModel.incidences.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredIncidences = remember(allIncidences, selectedStatusFilter) {
        if (selectedStatusFilter == "Todos") allIncidences
        else allIncidences.filter { it.status == selectedStatusFilter }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("EcoReport") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Reporte")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Pendiente", "En Proceso", "Resuelto").forEach { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status) }
                    )
                }
            }

            if (filteredIncidences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (allIncidences.isEmpty()) "No has enviado reportes aún" else "No hay reportes con este filtro",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Text(
                    "Mis Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(filteredIncidences) { incidence ->
                        IncidenceItem(incidence)
                    }
                }
            }
        }

        if (showForm) {
            ReportFormDialog(
                viewModel = viewModel,
                onDismiss = { showForm = false },
                onSuccess = {
                    showForm = false
                    scope.launch {
                        snackbarHostState.showSnackbar("¡Reporte enviado con éxito!")
                    }
                }
            )
        }
    }
}

@Composable
fun IncidenceItem(incidence: Incidence) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = incidence.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = incidence.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(text = incidence.location.ifBlank { "Sin ubicación" }, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.weight(1f))
                val (statusColor, statusIcon) = when(incidence.status) {
                    "Pendiente" -> Color(0xFFFFB300) to Icons.Default.Schedule
                    "En Proceso" -> Color(0xFF2196F3) to Icons.Default.Build
                    else -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, null, Modifier.size(14.dp), tint = statusColor)
                    Spacer(Modifier.width(4.dp))
                    Text(text = incidence.status, color = statusColor, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormDialog(viewModel: MobileIncidenceViewModel, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Media") }
    var category by remember { mutableStateOf("Infraestructura") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isSaving by viewModel.isSaving.collectAsState()
    
    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("Infraestructura", "Basura", "Alumbrado", "Seguridad", "Vialidad")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                Text("Nuevo Reporte", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            
            Box {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                )
                Box(Modifier.matchParentSize().clickable { expandedCategory = true })
                DropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expandedCategory = false })
                    }
                }
            }

            OutlinedTextField(
                value = location, 
                onValueChange = { location = it }, 
                label = { Text("Ubicación / Dirección") }, 
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
            )

            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            
            Text("Prioridad", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Baja", "Media", "Alta").forEach { p ->
                    FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p) })
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(0.5f), RoundedCornerShape(50)).padding(4.dp)) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                    }
                } else {
                    TextButton(onClick = { launcher.launch("image/*") }) {
                        Icon(Icons.Default.CameraAlt, modifier = Modifier.padding(end = 8.dp), contentDescription = null)
                        Text("Añadir Foto")
                    }
                }
            }

            Button(
                onClick = { 
                    viewModel.submitReport(title, description, category, priority, location, imageUri) {
                        onSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                enabled = !isSaving && title.isNotBlank() && description.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Enviar Reporte")
                }
            }
        }
    }
}
