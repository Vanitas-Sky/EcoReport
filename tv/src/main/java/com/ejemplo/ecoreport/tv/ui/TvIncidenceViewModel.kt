package com.ejemplo.ecoreport.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.ecoreport.core.data.IncidenceRepository
import com.ejemplo.ecoreport.core.model.Incidence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TvIncidenceViewModel(
    private val repository: IncidenceRepository = IncidenceRepository()
) : ViewModel() {

    private val _incidences = MutableStateFlow<List<Incidence>>(emptyList())
    val incidences: StateFlow<List<Incidence>> = _incidences.asStateFlow()

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
}