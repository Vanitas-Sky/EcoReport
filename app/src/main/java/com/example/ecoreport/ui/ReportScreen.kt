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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "EcoReport",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showForm = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("NUEVO REPORTE")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = listOf("Todos", "Pendiente", "En Proceso", "Resuelto").indexOf(selectedStatusFilter).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                listOf("Todos", "Pendiente", "En Proceso", "Resuelto").forEach { status ->
                    val isSelected = selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status) },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filteredIncidences.isEmpty()) {
                EmptyStateView(allIncidences.isEmpty())
            } else {
                Text(
                    "Mis Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        snackbarHostState.showSnackbar("¡Gracias! Reporte enviado correctamente.")
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyStateView(isInitial: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Icon(
                    if (isInitial) Icons.Default.LibraryAdd else Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.padding(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = if (isInitial) "Empieza a cuidar tu comunidad" else "No hay resultados",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isInitial) "Toca el botón inferior para enviar tu primer reporte ciudadano." else "Intenta cambiar el filtro de estado.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun IncidenceItem(incidence: Incidence) {
    val statusColor = when(incidence.status) {
        "Pendiente" -> Color(0xFFFFB300)
        "En Proceso" -> Color(0xFF1E88E5)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = incidence.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                val prioColor = when(incidence.priority) {
                    "Alta" -> Color.Red
                    "Media" -> Color(0xFFF57C00)
                    else -> Color.Green
                }
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(8.dp)
                        .background(prioColor, CircleShape)
                        .align(Alignment.TopStart)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = incidence.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = incidence.location.ifBlank { "Ciudad" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = incidence.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
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
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Crear nuevo reporte",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("¿Qué está pasando?") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                )
                Box(Modifier.matchParentSize().clickable { expandedCategory = true })
                DropdownMenu(
                    expanded = expandedCategory, 
                    onDismissRequest = { expandedCategory = false },
                    modifier = Modifier.fillMaxWidth(0.8f).background(MaterialTheme.colorScheme.surface)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontWeight = FontWeight.Medium) },
                            onClick = { category = cat; expandedCategory = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = location, 
                onValueChange = { location = it }, 
                label = { Text("Dirección exacta") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.AddLocationAlt, null, tint = MaterialTheme.colorScheme.primary) }
            )

            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { Text("Detalles adicionales...") }, 
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
            
            Column {
                Text("Prioridad del asunto", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Baja", "Media", "Alta").forEach { p ->
                        val isSel = priority == p
                        val pColor = when(p) {
                            "Alta" -> Color(0xFFD32F2F)
                            "Media" -> Color(0xFFF57C00)
                            else -> Color(0xFF388E3C)
                        }
                        SuggestionChip(
                            onClick = { priority = p },
                            label = { Text(p) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSel) pColor.copy(0.1f) else Color.Transparent,
                                labelColor = if (isSel) pColor else Color.Gray
                            )
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { launcher.launch("image/*") },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))))
                        Text("Toca para cambiar foto", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), style = MaterialTheme.typography.labelSmall)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("Evidencia Fotográfica", fontWeight = FontWeight.Bold)
                            Text("(Opcional)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            Button(
                onClick = { 
                    viewModel.submitReport(title, description, category, priority, location, imageUri) {
                        onSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving && title.isNotBlank() && description.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("ENVIAR REPORTE CIUDADANO", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
