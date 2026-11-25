package com.example.preparcialcomponentes.core.network

/**
 * Configuración centralizada de red para la aplicación.
 *
 * NOTA PARA EL EXAMEN:
 * - Si usas el Emulador de Android y tu backend Python corre en el PC, usa "http://10.0.2.2:8000/".
 *   10.0.2.2 es la dirección IP alias que el emulador asigna al localhost de tu máquina anfitriona.
 * - Si usas un Dispositivo Físico conectado a la misma red Wi-Fi, cambia a tu IP local, por ejemplo:
 *   "http://192.168.1.50:8000/".
 * - Asegúrate siempre de incluir la barra inclinada al final ("/") en BASE_URL para Retrofit.
 */
object NetworkConfig {
    const val BASE_URL = "http://10.0.2.2:8000/"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L
}
