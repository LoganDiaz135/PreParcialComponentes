package com.example.preparcialcomponentes.presentation.screens.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.preparcialcomponentes.core.common.ApiResult
import com.example.preparcialcomponentes.core.common.UiState
import com.example.preparcialcomponentes.domain.model.SavingPlan
import com.example.preparcialcomponentes.domain.repository.SavingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para HU-01: Consultar y listar planes de ahorro.
 *
 * Responsabilidades:
 * - Depende únicamente de la interfaz [SavingPlanRepository] (no conoce Retrofit ni Fake).
 * - Expone un [StateFlow] de [UiState] para que la vista Compose observe los cambios.
 * - Gestiona explícitamente los 4 estados: Loading, Success, Empty y Error.
 */
class PlansViewModel(
    private val repository: SavingPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<SavingPlan>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SavingPlan>>> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getSavingPlans()) {
                is ApiResult.Success -> {
                    _uiState.value = if (result.data.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(result.data)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
