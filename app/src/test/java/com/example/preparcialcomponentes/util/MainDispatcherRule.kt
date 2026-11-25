package com.example.preparcialcomponentes.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla de JUnit para reemplazar Dispatchers.Main durante pruebas unitarias en la JVM.
 *
 * POR QUÉ EXISTE:
 * Los ViewModels usan `viewModelScope.launch`, que por defecto corre en `Dispatchers.Main` (hilo UI de Android).
 * En pruebas unitarias locales (`test/`) no existe Android ni hilo principal, así que sin esta regla
 * los tests de ViewModel fallarían con `Module with the Main dispatcher had failed to initialize`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
