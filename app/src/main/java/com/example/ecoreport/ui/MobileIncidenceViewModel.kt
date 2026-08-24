package com.example.ecoreport.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.ecoreport.core.data.IncidenceRepository
import com.ejemplo.ecoreport.core.model.Incidence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MobileIncidenceViewModel(
    private val repository: IncidenceRepository = IncidenceRepository()
) : ViewModel() {

    private val _incidences = MutableStateFlow<List<Incidence>>(emptyList())
    val incidences: StateFlow<List<Incidence>> = _incidences.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadIncidences()
    }

    private fun loadIncidences() {
        viewModelScope.launch {
            repository.getIncidencesFlow().collect { list ->
                _incidences.value = list
            }
        }
    }

    fun submitReport(
        title: String,
        description: String,
        category: String,
        priority: String,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || description.isBlank()) return

        _isSaving.value = true
        val newIncidence = Incidence(
            title = title.trim(),
            description = description.trim(),
            category = category,
            priority = priority,
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1530587191325-3db32d826c18?auto=format&fit=crop&w=600&q=80" },
            status = "Pendiente",
            timestamp = System.currentTimeMillis()
        )

        repository.saveIncidence(newIncidence) { success ->
            _isSaving.value = false
            if (success) onSuccess()
        }
    }
}