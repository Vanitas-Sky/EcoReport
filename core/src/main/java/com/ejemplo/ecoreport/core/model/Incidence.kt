package com.ejemplo.ecoreport.core.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Incidence(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "Infraestructura",
    var priority: String = "Media",
    var imageUrl: String = "",
    var status: String = "Pendiente",
    var timestamp: Long = 0,
    var location: String = ""
) {
    // Constructor sin argumentos necesario para Firebase
    constructor() : this("", "", "", "Infraestructura", "Media", "", "Pendiente", 0, "")
}
