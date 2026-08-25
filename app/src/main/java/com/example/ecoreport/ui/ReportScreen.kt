package com.example.ecoreport.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ejemplo.ecoreport.core.model.Incidence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: MobileIncidenceViewModel) {
    val incidences by viewModel.incidences.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("EcoReport") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Reporte")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (incidences.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No has enviado reportes aún", color = Color.Gray)
                }
            } else {
                Text(
                    "Mis Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(incidences) { incidence ->
                        IncidenceItem(incidence)
                    }
                }
            }
        }

        if (showForm) {
            ReportFormDialog(
                viewModel = viewModel,
                onDismiss = { showForm = false }
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
                Text(text = incidence.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = incidence.category, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                val statusColor = if (incidence.status == "Pendiente") Color(0xFFFFB300) else Color(0xFF4CAF50)
                Text(text = incidence.status, color = statusColor, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormDialog(viewModel: MobileIncidenceViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Media") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val isSaving by viewModel.isSaving.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Nuevo Reporte", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Prioridad: ", modifier = Modifier.padding(end = 8.dp))
                listOf("Baja", "Media", "Alta").forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (imageUri != null) Color.Transparent else Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd)) {
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
                    viewModel.submitReport(title, description, "Infraestructura", priority, imageUri) {
                        onDismiss()
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
