package com.ejemplo.ecoreport.core.model

data class Incidence(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Infraestructura",
    val priority: String = "Media",
    val imageUrl: String = "",
    val status: String = "Pendiente",
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = ""
)
