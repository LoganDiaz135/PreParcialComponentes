package com.example.preparcialcomponentes.core.common

/**
 * Representa el estado de una pantalla o componente de UI en Compose.
 *
 * Estados disponibles:
 * - [Loading]: La operación asíncrona está en progreso.
 * - [Success]: La operación finalizó con datos válidos.
 * - [Empty]: La operación fue exitosa pero no hay elementos para mostrar (lista vacía).
 * - [Error]: Ocurrió un error con un mensaje legible para el usuario.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
