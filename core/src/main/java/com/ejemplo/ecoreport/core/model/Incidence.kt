package com.ejemplo.ecoreport.core.model

data class Incidence(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Infraestructura", // Limpieza, Fugas, Podas, etc.
    val priority: String = "Media",            // Alta, Media, Baja
    val imageUrl: String = "",
    val status: String = "Pendiente",          // Pendiente, En Proceso, Resuelto
    val timestamp: Long = System.currentTimeMillis()
)