package com.example.preparcialcomponentes.presentation.navigation

/**
 * Rutas de navegación de la aplicación declaradas como sealed class.
 *
 * REGLA DE ADAPTACIÓN:
 * Mantén aquí únicamente las pantallas reales implementadas en AppNavHost.
 * Cuando agregues una pantalla con argumentos en el parcial (ej. detalle o formulario por ID),
 * usa el patrón:
 *
 * ```kotlin
 * object Detail : Screen("detail/{itemId}") {
 *     fun createRoute(itemId: Int): String = "detail/$itemId"
 * }
 * ```
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Plans : Screen("plans")
}
