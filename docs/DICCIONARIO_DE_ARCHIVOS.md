# DICCIONARIO ANALÍTICO Y PEDAGÓGICO DE ARCHIVOS — PreParcialComponentes

Este diccionario es la herramienta principal de estudio y referencia durante el examen. Clasifica los archivos en **Esenciales**, **Soporte** y **Configuración**, ofreciendo para los archivos esenciales una explicación profunda de su código real y cómo adaptarlos a cualquier entidad desconocida.

---

## 1. ARCHIVOS ESENCIALES (Análisis Profundo)

---

### 1.1 `NetworkConfig.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/core/network/NetworkConfig.kt`
* **Objetivo:** Centralizar la URL base y los timeouts de red.
* **Responsabilidad:** Proveer una única fuente de verdad para la dirección del servidor local.
* **Conceptos utilizados:** Constantes en Kotlin (`const val`), Timeouts en OkHttp.
* **Dependencias:** Ninguna (archivo puro del sistema de red).
* **Explicación del código real:**
  ```kotlin
  object NetworkConfig {
      const val BASE_URL = "http://10.0.2.2:8000/"
      const val CONNECT_TIMEOUT_SECONDS = 30L
      const val READ_TIMEOUT_SECONDS = 30L
      const val WRITE_TIMEOUT_SECONDS = 30L
  }
  ```
  `10.0.2.2` es la IP especial que usa el emulador Android para referirse a `localhost` de tu computador donde se ejecuta el backend Python (FastAPI + SQLite).
* **Qué se conserva:** Tiempos de espera (`30L`).
* **Qué suele cambiar en el parcial:** La constante `BASE_URL` (si el profesor especifica otro puerto como `5000`, `8080` o `5050`).
* **Ejemplo de adaptación:**
  ```kotlin
  // Si el backend corre en el puerto 5000:
  const val BASE_URL = "http://10.0.2.2:5000/"
  ```
* **Errores comunes:**
  * Usar `"http://localhost:8000/"` en lugar de `"http://10.0.2.2:8000/"` (provoca `ConnectException`).
  * Omitir la barra diagonal `/` al final (Retrofit lanza excepción al inicializarse).
* **Verificación:** Al realizar cualquier llamada HTTP, verificar en **Logcat (`OkHttp`)** la URL impresa.

---

### 1.2 `RetrofitClient.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/core/network/RetrofitClient.kt`
* **Objetivo:** Construir la instancia Singleton de Retrofit con OkHttp y Gson.
* **Responsabilidad:** Instanciar servicios de API de forma segura y eficiente sin recrear clientes HTTP.
* **Conceptos utilizados:** Patrón Singleton (`object`), Interceptores HTTP (`HttpLoggingInterceptor`), Factory Converters (`GsonConverterFactory`).
* **Dependencias:** `NetworkConfig.kt`, OkHttp3, Retrofit2, Gson.
* **Explicación del código real:**
  Configura `HttpLoggingInterceptor` con nivel `BODY` para ver los encabezados, peticiones y respuestas JSON en Logcat. Usa `GsonBuilder().setLenient().create()` para tolerar variaciones en el formato JSON.
* **Qué se conserva:** **100% del archivo.** No requiere modificaciones durante el examen.
* **Qué suele cambiar:** Nada.
* **Ejemplo de uso en `AppViewModelFactory`:**
  ```kotlin
  val productApi = RetrofitClient.createService(ProductApi::class.java)
  ```
* **Errores comunes:** Modificar el timeout o quitar el logger durante el examen (pierdes visibilidad de errores en Logcat).
* **Verificación:** Revisar en Logcat líneas con `--> GET ...` y `<-- 200 OK`.

---

### 1.3 `UiState.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/core/common/UiState.kt`
* **Objetivo:** Modelar de forma exhaustiva los 4 estados de cualquier pantalla en Compose.
* **Responsabilidad:** Evitar inconsistencias de estado (ej. mostrar datos mientras se carga o indicar error con pantalla en blanco).
* **Conceptos utilizados:** Sealed Class con Genéricos (`sealed class UiState<out T>`).
* **Dependencias:** Ninguna.
* **Explicación del código real:**
  ```kotlin
  sealed class UiState<out T> {
      object Loading : UiState<Nothing>()
      object Empty : UiState<Nothing>()
      data class Success<T>(val data: T) : UiState<T>()
      data class Error(val message: String) : UiState<Nothing>()
  }
  ```
* **Qué se conserva:** **100% del archivo.**
* **Qué suele cambiar:** Nada.
* **Ejemplo de uso en ViewModel:** `private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)`
* **Errores comunes:** Olvidar manejar `UiState.Empty` en pantallas donde una lista pueda volver vacía.
* **Verificación:** Probar visualmente los 4 estados mediante componentes de `presentation/components/`.

---

### 1.4 `ApiResult.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/core/common/ApiResult.kt`
* **Objetivo:** Encapsular resultados de la capa de datos y prevenir caídas (`crashes`) por excepciones de red.
* **Responsabilidad:** Convertir excepciones de red (código HTTP, falta de conexión, timeout, error JSON) en objetos controlados.
* **Conceptos utilizados:** Sealed class, Funciones de orden superior (`suspend fun <T> safeApiCall`).
* **Dependencias:** `NetworkConfig.kt`, `retrofit2.HttpException`, Gson, Java Net.
* **Explicación del código real:**
  ```kotlin
  sealed class ApiResult<out T> {
      data class Success<out T>(val data: T) : ApiResult<T>()
      data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
  }
  ```
  `safeApiCall` ejecuta una lambda dentro de un `try-catch`, capturando `HttpException` (400, 404, 500), `ConnectException` (servidor apagado) y `JsonSyntaxException`.
* **Qué se conserva:** **100% del archivo.**
* **Qué suele cambiar:** Nada en un parcial estándar.
* **Ejemplo de uso:** `return safeApiCall { api.getProducts().toDomain() }`
* **Errores comunes:** Llamar directamente al servicio Retrofit sin envolverlo en `safeApiCall` (la app falla al perder conexión).
* **Verificación:** Simular un servidor apagado para comprobar que emite `ApiResult.Error` en lugar de cerrar la aplicación.

---

### 1.5 `SavingPlan.kt` (Modelo de Dominio)
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/domain/model/SavingPlan.kt`
* **Objetivo:** Representar la entidad de negocio pura dentro de Android.
* **Responsabilidad:** Ser la fuente de verdad consumida por ViewModels y Screens Compose, aislada de librerías de serialización como Gson.
* **Conceptos utilizados:** Data Class en Kotlin.
* **Explicación del código real:**
  ```kotlin
  data class SavingPlan(
      val id: Int,
      val name: String,
      val targetAmount: Double,
      val currentAmount: Double = 0.0,
      val description: String = ""
  )
  ```
* **Qué se conserva:** La estructura de Data Class limpia.
* **Qué se modifica en el parcial:** **Se reemplaza o crea un nuevo archivo por cada entidad del examen.**
* **Ejemplo de adaptación (Escenario Productos):**
  ```kotlin
  data class Product(
      val id: Int,
      val name: String,
      val price: Double,
      val stock: Int = 0
  )
  ```
* **Errores comunes:** Agregar anotaciones de Gson como `@SerializedName` en el modelo de dominio (debe ir únicamente en los DTOs).

---

### 1.6 `SavingPlanDto.kt` (Data Transfer Objects)
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/data/remote/dto/SavingPlanDto.kt`
* **Objetivo:** Mapear la estructura exacta del JSON devuelto o recibido por la API REST.
* **Responsabilidad:** Mapear los nombres de claves `snake_case` del backend Python a `camelCase` de Kotlin y tolerar valores nulos (`?`).
* **Conceptos utilizados:** `@SerializedName`, Nulabilidad explícita (`Int?`, `String?`).
* **Explicación del código real:**
  ```kotlin
  data class SavingPlanDto(
      @SerializedName("id") val id: Int? = null,
      @SerializedName("name") val name: String? = null,
      @SerializedName("target_amount") val targetAmount: Double? = null,
      @SerializedName("current_amount") val currentAmount: Double? = null,
      @SerializedName("description") val description: String? = null
  )
  ```
  También separa DTOs de solicitud (`PaymentRequestDto`) que no contienen `id` cuando la base de datos lo genera en el servidor.
* **Qué se modifica en el parcial:** Reemplazar o crear los DTOs según las claves JSON del servidor del examen.
* **Ejemplo de adaptación:**
  ```kotlin
  data class ProductDto(
      @SerializedName("id") val id: Int? = null,
      @SerializedName("nombre_producto") val name: String? = null,
      @SerializedName("precio_unitario") val price: Double? = null
  )
  ```
* **Errores comunes:** Escribir mal el nombre en `@SerializedName("...")` (el campo resulta en `null` sin marcar error de sintaxis).

---

### 1.7 `SavingPlanMapper.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/data/mapper/SavingPlanMapper.kt`
* **Objetivo:** Transformar DTOs nulables a modelos de dominio no nulables.
* **Responsabilidad:** Asignar valores por defecto seguros (`?: 0`, `.orEmpty()`, `?: 0.0`).
* **Conceptos utilizados:** Extension Functions (`SavingPlanDto.toDomain()`), `@JvmName`.
* **Explicación del código real:**
  ```kotlin
  fun SavingPlanDto.toDomain(): SavingPlan {
      return SavingPlan(
          id = this.id ?: 0,
          name = this.name.orEmpty(),
          targetAmount = this.targetAmount ?: 0.0,
          currentAmount = this.currentAmount ?: 0.0,
          description = this.description.orEmpty()
      )
  }
  fun List<SavingPlanDto>.toDomain(): List<SavingPlan> = this.map { it.toDomain() }
  ```
* **Qué se modifica en el parcial:** Crear las extensiones `.toDomain()` correspondientes a los nuevos DTOs.
* **Errores comunes:** Olvidar `@JvmName` cuando existen múltiples extensiones `List<Dto>.toDomain()`, lo cual causa conflicto de firmas en tiempo de compilación.

---

### 1.8 `SavingPlanApi.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/data/remote/api/SavingPlanApi.kt`
* **Objetivo:** Definir la interfaz de comunicación Retrofit con los endpoints HTTP.
* **Responsabilidad:** Declarar rutas, verbos HTTP (`@GET`, `@POST`), parámetros de ruta (`@Path`) y cuerpos JSON (`@Body`).
* **Conceptos utilizados:** Anotaciones Retrofit, Funciones `suspend`.
* **Explicación del código real:**
  ```kotlin
  interface SavingPlanApi {
      @GET("plans")
      suspend fun getSavingPlans(): List<SavingPlanDto>

      @POST("plans/{id}/payments")
      suspend fun registerPayment(
          @Path("id") planId: Int,
          @Body paymentRequest: PaymentRequestDto
      ): PaymentRecordDto
  }
  ```
* **Qué se modifica en el parcial:** Definir los métodos para los endpoints del examen (`products`, `students`, `projects`).
* **Errores comunes:** Olvidar la palabra clave `suspend` o incluir una barra inicial `/` en `@GET("/plans")`.

---

### 1.9 Repositorios (`SavingPlanRepository.kt`, `RepositoryImpl` y `FakeRepository`)
* **Ubicaciones:** `domain/repository/` y `data/repository/`
* **Objetivo:** Implementar la abstracción de datos.
* **Responsabilidad:** `SavingPlanRepository` define la interfaz. `SavingPlanRepositoryImpl` invoca la API real con `safeApiCall`. `FakeSavingPlanRepository` simula la respuesta en memoria para desarrollo y pruebas offline.
* **Qué se modifica en el parcial:** Crear la interfaz y la implementación correspondiente al recurso del examen.

---

### 1.10 `AppViewModelFactory.kt`
* **Ubicación:** `app/src/main/java/com/example/preparcialcomponentes/presentation/AppViewModelFactory.kt`
* **Objetivo:** Construir e inyectar dependencias en los ViewModels.
* **Responsabilidad:** Inyectar los repositorios a los ViewModels correspondientes y permitir alternar con `useFakeData = true/false`.
* **Explicación del código real:**
  ```kotlin
  class AppViewModelFactory(private val useFakeData: Boolean = true) : ViewModelProvider.Factory {
      private val savingPlanRepository: SavingPlanRepository by lazy {
          if (useFakeData) FakeSavingPlanRepository()
          else SavingPlanRepositoryImpl(RetrofitClient.createService(SavingPlanApi::class.java))
      }
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return when {
              modelClass.isAssignableFrom(PlansViewModel::class.java) -> PlansViewModel(savingPlanRepository) as T
              else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
          }
      }
  }
  ```
* **Qué se modifica en el parcial:** Registrar nuevos repositorios y agregar ramas en el bloque `when` para nuevos ViewModels.

---

### 1.11 `Screen.kt` y `AppNavHost.kt`
* **Ubicaciones:** `presentation/navigation/`
* **Objetivo:** Gestionar las rutas y el grafo de pantallas Compose.
* **Responsabilidad:** Asignar cadenas de ruta y vincular cada destino con su pantalla Compose y ViewModel.

---

### 1.12 `PlansViewModel.kt` y `PlansScreen.kt` (HU-01)
* **Ubicaciones:** `presentation/screens/plans/`
* **Objetivo:** Referencia maestra de consulta y listado (`GET`).
* **Responsabilidad:** `PlansViewModel` ejecuta la carga en corrutina y expone el `StateFlow<UiState>`. `PlansScreen` observa el estado y dibuja los componentes según el caso (`Loading`, `Empty`, `Error` o `Success`).

---

## 2. ARCHIVOS DE SOPORTE (Resumen Funcional)

| Archivo | Responsabilidad | Uso en Parcial |
|---|---|---|
| `MainActivity.kt` | Punto de entrada único (`setContent { AppNavHost() }`). | No modificar. |
| `AppButton.kt` | Botón estilizado con soporte de carga y deshabilitación. | Reutilizar en formularios. |
| `AppTextField.kt` | Campo de texto configurable con mensaje de error explícito. | Reutilizar en formularios. |
| `LoadingIndicator.kt` | Spinner centrado con texto de carga. | Reutilizar en estado `Loading`. |
| `ErrorMessage.kt` | Tarjeta de error con botón de reintento. | Reutilizar en estado `Error`. |
| `EmptyState.kt` | Componente amigable para listas sin registros. | Reutilizar en estado `Empty`. |
| `ItemCard.kt` | Tarjeta contenedor Material 3 para listas. | Reutilizar dentro de `LazyColumn`. |
| `MainDispatcherRule.kt` | Regla JUnit para redirigir `Dispatchers.Main` a la JVM. | Reutilizar en tests de ViewModel. |
| `PlansViewModelTest.kt` | Test unitario de 4 estados en la JVM. | Usar como plantilla para nuevos tests. |

---

## 3. ARCHIVOS DE CONFIGURACIÓN

| Archivo | Ajustes clave | Errores comunes en examen |
|---|---|---|
| `AndroidManifest.xml` | Permiso `INTERNET` y `usesCleartextTraffic="true"`. | Borrar `usesCleartextTraffic` (bloquea HTTP local). |
| `app/build.gradle.kts` | Versiones de SDK, Compose BOM, Retrofit, Gson, Coroutines. | Modificar versiones que desalineen Kotlin/Compose. |
| `build.gradle.kts` (raíz) | Plugins AGP (`8.5.2`) y Kotlin (`1.9.24`). | No modificar. |
| `settings.gradle.kts` | Repositorios (`google()`, `mavenCentral()`). | No modificar. |
| `gradle.properties` | Memoria JVM (`-Xmx2048m`), flags de AndroidX. | Reducir memoria (causa fallos de compilación). |
