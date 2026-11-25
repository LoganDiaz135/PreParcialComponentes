package com.example.preparcialcomponentes.core.common

import com.example.preparcialcomponentes.core.network.NetworkConfig

/**
 * Wrapper de resultado para operaciones de datos del Repository.
 *
 * Permite encapsular el éxito o fracaso de llamadas de red sin propagar
 * excepciones no controladas hacia los ViewModels.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

/**
 * Ejecuta una llamada de red de forma segura dentro de un bloque try-catch,
 * mapeando cualquier excepción de red o parseo a un [ApiResult.Error].
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        val result = apiCall()
        ApiResult.Success(result)
    } catch (e: retrofit2.HttpException) {
        val errorMsg = when (e.code()) {
            400 -> "Solicitud incorrecta (400)"
            404 -> "Recurso no encontrado en el servidor (404)"
            500 -> "Error interno del servidor (500)"
            else -> "Error del servidor: código HTTP ${e.code()}"
        }
        ApiResult.Error(errorMsg, code = e.code())
    } catch (e: java.net.ConnectException) {
        ApiResult.Error("No se pudo conectar al servidor. Verifica que el backend esté corriendo en ${NetworkConfig.BASE_URL}")
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("Tiempo de espera agotado al conectar con el servidor.")
    } catch (e: com.google.gson.JsonSyntaxException) {
        ApiResult.Error("Error al procesar el formato JSON de la respuesta: ${e.message}")
    } catch (e: Exception) {
        ApiResult.Error(e.localizedMessage ?: "Ocurrió un error inesperado.")
    }
}
