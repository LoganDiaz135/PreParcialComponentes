package com.example.preparcialcomponentes.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preparcialcomponentes.core.common.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de ejemplo para la pantalla de inicio (HomeScreen).
 *
 * Demuestra:
 * - El uso de StateFlow para exponer el estado a Compose.
 * - [UiState] con Loading y Success.
 * - Operaciones asíncronas seguras con [viewModelScope].
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Simulación inicial para verificar reactividad de la UI
            delay(500)
            _uiState.value = UiState.Success("Plantilla de PreParcial lista para conectar historias de usuario.")
        }
    }
}
