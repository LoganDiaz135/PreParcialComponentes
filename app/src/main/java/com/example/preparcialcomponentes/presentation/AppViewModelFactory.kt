package com.example.preparcialcomponentes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.preparcialcomponentes.core.network.RetrofitClient
import com.example.preparcialcomponentes.data.remote.api.SavingPlanApi
import com.example.preparcialcomponentes.data.repository.FakeSavingPlanRepository
import com.example.preparcialcomponentes.data.repository.SavingPlanRepositoryImpl
import com.example.preparcialcomponentes.domain.repository.SavingPlanRepository
import com.example.preparcialcomponentes.presentation.screens.home.HomeViewModel
import com.example.preparcialcomponentes.presentation.screens.plans.PlansViewModel

/**
 * Fábrica manual para construir ViewModels con sus dependencias inyectadas.
 *
 * CAMBIO ENTRE SIMULACIÓN Y BACKEND REAL:
 * - Actualmente usa [FakeSavingPlanRepository] porque no hay un backend activo en esta fase.
 * - Cuando exista el backend Python con contrato confirmado, cambia `useFakeData = false`
 *   (o reemplaza directamente por [SavingPlanRepositoryImpl]).
 */
class AppViewModelFactory(
    private val useFakeData: Boolean = true
) : ViewModelProvider.Factory {

    private val savingPlanRepository: SavingPlanRepository by lazy {
        if (useFakeData) {
            FakeSavingPlanRepository()
        } else {
            SavingPlanRepositoryImpl(
                api = RetrofitClient.createService(SavingPlanApi::class.java)
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel() as T
            }
            modelClass.isAssignableFrom(PlansViewModel::class.java) -> {
                PlansViewModel(savingPlanRepository) as T
            }
            else -> throw IllegalArgumentException("ViewModel no registrado en AppViewModelFactory: ${modelClass.name}")
        }
    }
}
