package com.example.preparcialcomponentes.presentation.screens.home

import com.example.preparcialcomponentes.core.common.UiState
import com.example.preparcialcomponentes.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Prueba unitaria mínima de verificación para [HomeViewModel].
 *
 * Demuestra cómo verificar los estados de un ViewModel (`Loading` -> `Success`)
 * en la JVM local sin emulador ni servidor real.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `al inicializar HomeViewModel pasa de Loading a Success`() = runTest {
        val viewModel = HomeViewModel()

        // Con StandardTestDispatcher la corrutina aún no avanza hasta ejecutar advanceUntilIdle()
        assertTrue(viewModel.uiState.value is UiState.Loading)

        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState is UiState.Success)
    }
}
