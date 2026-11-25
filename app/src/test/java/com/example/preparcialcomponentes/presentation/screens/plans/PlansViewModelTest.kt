package com.example.preparcialcomponentes.presentation.screens.plans

import com.example.preparcialcomponentes.core.common.UiState
import com.example.preparcialcomponentes.data.repository.FakeSavingPlanRepository
import com.example.preparcialcomponentes.domain.model.SavingPlan
import com.example.preparcialcomponentes.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas unitarias de [PlansViewModel] utilizando [FakeSavingPlanRepository].
 *
 * Verifica los cuatro estados (Loading, Success, Empty, Error) y la recuperación ante reintento,
 * sin depender de red ni de un backend en ejecución.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlansViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `loadPlans emite Loading y luego Success cuando existen planes`() = runTest {
        val samplePlans = listOf(
            SavingPlan(id = 1, name = "Viaje", targetAmount = 1000.0, currentAmount = 200.0),
            SavingPlan(id = 2, name = "Estudios", targetAmount = 3000.0, currentAmount = 1500.0)
        )
        val fakeRepository = FakeSavingPlanRepository(
            plansToReturn = samplePlans,
            simulatedDelayMs = 0L
        )

        val viewModel = PlansViewModel(fakeRepository)
        assertTrue(viewModel.uiState.value is UiState.Loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(2, data.size)
        assertEquals("Viaje", data[0].name)
    }

    @Test
    fun `loadPlans emite Empty cuando el repositorio devuelve una lista vacia`() = runTest {
        val fakeRepository = FakeSavingPlanRepository(
            plansToReturn = emptyList(),
            simulatedDelayMs = 0L
        )

        val viewModel = PlansViewModel(fakeRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Empty)
    }

    @Test
    fun `loadPlans emite Error cuando el repositorio falla`() = runTest {
        val fakeRepository = FakeSavingPlanRepository(
            shouldReturnError = true,
            errorMessage = "Error de conexión simulado",
            simulatedDelayMs = 0L
        )

        val viewModel = PlansViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Error de conexión simulado", (state as UiState.Error).message)
    }

    @Test
    fun `reintentar loadPlans despues de un Error pasa a Success cuando el repositorio responde bien`() = runTest {
        val fakeRepository = FakeSavingPlanRepository(
            plansToReturn = listOf(SavingPlan(id = 1, name = "Casa", targetAmount = 5000.0)),
            shouldReturnError = true,
            simulatedDelayMs = 100L
        )

        val viewModel = PlansViewModel(fakeRepository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UiState.Error)

        // Simulamos que el usuario pulsa "Reintentar" tras restablecerse el servicio
        fakeRepository.shouldReturnError = false
        viewModel.loadPlans()
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value is UiState.Loading)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UiState.Success)
    }
}
