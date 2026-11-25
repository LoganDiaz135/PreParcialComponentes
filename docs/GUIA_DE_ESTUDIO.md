# GUÍA DE ESTUDIO — PROGRAMACIÓN POR COMPONENTES
## Fundamentos Técnicos y Decisiones Arquitectónicas del Proyecto

> [!NOTE]
> Esta guía está sincronizada con el estado actual del proyecto (**FASE 2 — Base Arquitectónica + HU-01 completada con FakeRepository**). Todos los nombres de archivos y clases citados existen en el código. Cuando se mencione una operación de fases posteriores (como un futuro `POST` conectado a UI), se indicará explícitamente.

---

## 1. ¿Qué es Jetpack Compose y cómo funciona aquí?

**Jetpack Compose** es el toolkit moderno de Android para construir interfaces de usuario de forma **declarativa** en Kotlin (sin archivos XML de layouts).

* En lugar de buscar vistas con `findViewById` y mutarlas manualmente, describes cómo debe verse la pantalla para un estado dado mediante funciones `@Composable`.
* Cuando el estado cambia, Compose ejecuta una **recomposición** (vuelve a dibujar únicamente los elementos afectados).
* **En este proyecto:**
  * `MainActivity.kt` llama a `setContent { AppNavHost() }`.
  * `HomeScreen.kt` observa el estado del `HomeViewModel` con `val state by viewModel.uiState.collectAsState()` y decide qué dibujar según sea `UiState.Loading`, `UiState.Success`, `UiState.Empty` o `UiState.Error`.
  * Los componentes visuales repetitivos (`AppButton`, `AppTextField`, `LoadingIndicator`, `ErrorMessage`, `EmptyState`, `ItemCard`) viven en `presentation/components/` para reutilizarlos en cualquier pantalla del examen.

---

## 2. ¿Qué es MVVM (Model - View - ViewModel)?

**MVVM** separa la interfaz gráfica de la lógica y los datos para que las pantallas sean simples y fáciles de mantener:

1. **View (Vista):** Funciones `@Composable` (ej. `HomeScreen.kt`). Su única tarea es mostrar el estado actual y enviar eventos del usuario (clics, texto) al ViewModel. **Nunca** hace peticiones HTTP ni procesa JSON.
2. **ViewModel:** Clase que hereda de `androidx.lifecycle.ViewModel` (ej. `HomeViewModel.kt`).
   * Sobrevive a rotaciones de pantalla.
   * Lanza corrutinas en `viewModelScope`.
   * Llama al Repository (cuando necesita datos remotos).
   * Expone el estado de la pantalla mediante `StateFlow`.
3. **Model (Modelo):** Reglas de datos, modelos de dominio (`domain/model/SavingPlan.kt`), repositorios (`SavingPlanRepository`) y fuente de datos remota (`SavingPlanApi`).

---

## 3. ¿Qué es Clean Architecture y cómo se aplica sin sobrearquitecturar?

**Clean Architecture** organiza el proyecto por capas de responsabilidad, haciendo que la lógica del negocio no dependa de librerías externas:

```
[ PRESENTATION ]  ──►  [ DOMAIN ]  ◄──  [ DATA ]
(Compose / ViewModel)  (Modelos / Repo) (Retrofit / DTO / Mapper)
         ▲                                      ▲
         └────────────── [ CORE ] ──────────────┘
                 (RetrofitClient, UiState, ApiResult)
```

* **`domain/` (El núcleo puro):** Modelos Kotlin limpios (`SavingPlan`, `Member`, `PaymentRecord`) y contratos (`SavingPlanRepository`). No importa Retrofit, Gson ni Compose.
  * *Nota sobre `usecase/`:* En FASE 0 se contempló `domain/usecase/` de forma opcional. No creamos UseCases obligatorios para operaciones directas; solo se añaden si una operación combina varios repositorios o se reutiliza en varias pantallas.
* **`data/` (Infraestructura de red):** Interfaces Retrofit (`SavingPlanApi`), DTOs (`SavingPlanDto`), mappers (`SavingPlanMapper`) e implementaciones de repositorio (`SavingPlanRepositoryImpl`).
* **`presentation/` (UI y estado):** Pantallas, ViewModels, componentes reutilizables, navegación y `AppViewModelFactory`.
* **`core/` (Utilidades transversales):** Configuración HTTP (`NetworkConfig`, `RetrofitClient`) y clases genéricas de estado/errores (`UiState`, `ApiResult`, `safeApiCall`).

---

## 4. ¿Qué es una API REST y qué es JSON?

* **API REST:** Conjunto de rutas HTTP (endpoints) expuestas por un servidor (en nuestro caso, un backend en Python con FastAPI y SQLite) para consultar (`GET`), crear (`POST`), actualizar (`PUT`/`PATCH`) o eliminar (`DELETE`) recursos.
* **JSON:** Formato de texto ligero clave-valor para intercambiar datos entre Android y el servidor.

Ejemplo de JSON que devolvería un backend Python:
```json
{
  "id": 1,
  "name": "Ahorro Vacaciones",
  "target_amount": 5000000.0,
  "current_amount": 1200000.0,
  "description": "Meta familiar"
}
```

---

## 5. ¿Qué son Retrofit, OkHttp y Gson?

Las tres librerías trabajan juntas en `core/network/RetrofitClient.kt`:

* **Retrofit:** Convierte una interfaz Kotlin con anotaciones (`@GET`, `@POST`, `@Path`, `@Body`) en llamadas HTTP reales. Ejemplo en `data/remote/api/SavingPlanApi.kt`.
* **OkHttp:** Es el motor HTTP debajo de Retrofit. En `RetrofitClient.kt` lo configuramos con:
  * Timeouts de conexión, lectura y escritura (`NetworkConfig.kt` = `30L` segundos).
  * `HttpLoggingInterceptor` con `level = HttpLoggingInterceptor.Level.BODY`, que imprime en **Logcat** la URL exacta, headers, código HTTP (200, 404, 422, 500) y el cuerpo JSON enviado y recibido. En un parcial, esto ahorra muchísimo tiempo al depurar errores de contrato con el backend.
* **Gson:** Traduce texto JSON ↔ objetos Kotlin (`GsonConverterFactory`).
  * `GsonBuilder().setLenient().create()` permite tolerar pequeñas variaciones de formato durante pruebas.

### Configuración indispensable para entorno local (`NetworkConfig.kt` y `AndroidManifest.xml`)
1. **`10.0.2.2` en emulador:** Para el emulador Android, `localhost` o `127.0.0.1` es el propio teléfono virtual. La IP especial `http://10.0.2.2:8000/` apunta al `localhost:8000` de tu computador donde corre Python. (En celular físico por Wi-Fi se usa la IP local del PC, ej. `http://192.168.1.50:8000/`).
2. **Barra final `/`:** En Retrofit, `BASE_URL` debe terminar en `/`.
3. **Permisos en `AndroidManifest.xml`:**
   * `<uses-permission android:name="android.permission.INTERNET" />`
   * `android:usesCleartextTraffic="true"` (necesario en Android 9+ porque en desarrollo local usamos `http://` y no `https://`).

---

## 6. ¿Qué es un DTO, qué es un Modelo de Dominio y para qué sirve el Mapper?

* **DTO (`SavingPlanDto.kt`):** Clase diseñada exactamente para el JSON del servidor. Usa `@SerializedName("target_amount")` para enlazar nombres en `snake_case` (típicos de Python/SQLite) con propiedades en `camelCase` de Kotlin (`targetAmount`), y usa tipos nulables (`Int?`, `String?`, `Double?`) para evitar fallos si el servidor omite un campo.
* **Modelo de Dominio (`SavingPlan.kt`):** Clase limpia (`val id: Int`, `val name: String`, `val targetAmount: Double`) sin anotaciones de Gson, usada por los ViewModels y las pantallas.
* **Mapper (`SavingPlanMapper.kt`):** Funciones de extensión `.toDomain()` que convierten DTO → Dominio aplicando valores seguros por defecto (`?: 0`, `.orEmpty()`, `?: 0.0`).
* **Ventaja en examen:** Si el profesor cambia un campo del JSON (por ejemplo de `target_amount` a `meta`), solo cambias el `@SerializedName` en el DTO; ninguna pantalla ni ViewModel se rompe.

---

## 7. ¿Qué es el Repository Pattern?

El **Repository** es el intermediario único entre los ViewModels y el origen de datos:
* Interfaz en `domain/repository/SavingPlanRepository.kt`: define *qué* operaciones existen (`getSavingPlans()`, `addMember()`, etc.) devolviendo `ApiResult<T>`.
* Implementación en `data/repository/SavingPlanRepositoryImpl.kt`: define *cómo* se obtienen (llamando a `SavingPlanApi` dentro de `safeApiCall`).

Gracias a esto, un ViewModel nunca conoce a Retrofit ni sabe si los datos vienen de red o de un repositorio falso (`FakeRepository`) en una prueba unitaria.

---

## 8. ¿Qué es una Factory manual (`AppViewModelFactory`)?

Android crea los `ViewModel` a través de `ViewModelProvider` para conservarlos al rotar la pantalla. Si un `ViewModel` recibe parámetros en su constructor (como un `Repository`), Android necesita una `ViewModelProvider.Factory` que sepa construirlo.

En `presentation/AppViewModelFactory.kt` usamos una **Factory manual** con propiedades `by lazy`:
* **Por qué no Hilt/Dagger:** Evita errores ocultos de generación de código (`kapt`/`ksp`) durante un examen contrarreloj y hace explícito todo el grafo de dependencias en un solo archivo de ~35 líneas.
* **Por qué no Room:** La persistencia oficial vive en el backend Python + SQLite; almacenar datos localmente en Android añadiría complejidad innecesaria y riesgo de desincronización.

---

## 9. ¿Qué son Coroutines y StateFlow?

* **Coroutines (`viewModelScope.launch`):** Permiten ejecutar operaciones `suspend` (como peticiones de red o retardos) en segundo plano sin congelar el hilo principal de la UI. Cuando el usuario sale de la pantalla y el ViewModel se destruye, `viewModelScope` cancela automáticamente la corrutina pendiente.
* **StateFlow:** Es un contenedor observable que siempre guarda el último estado emitido y notifica automáticamente a Compose cada vez que cambia:
  * En `HomeViewModel.kt`:
    ```kotlin
    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()
    ```
  * `_uiState` es privado y mutable (solo el ViewModel lo modifica).
  * `uiState` es público y de solo lectura (`asStateFlow()`) para que la vista observe sin poder alterarlo directamente.

---

## 10. ¿Cómo se gestionan los estados y errores (`UiState` y `ApiResult`)?

1. **En la capa de datos (`core/common/ApiResult.kt`):**
   `safeApiCall { ... }` envuelve cada llamada de red en un `try/catch` y captura:
   * `HttpException`: errores 400, 404, 500 del servidor.
   * `ConnectException`: backend apagado o IP/puerto incorrecto.
   * `SocketTimeoutException`: el servidor tardó más de 30s.
   * `JsonSyntaxException`: el JSON devuelto no coincide con el DTO.
   Devuelve siempre `ApiResult.Success(datos)` o `ApiResult.Error(mensaje, codigo)`.
2. **En la UI (`core/common/UiState.kt`):**
   Toda pantalla reacciona a 4 posibles estados:
   * `UiState.Loading` → muestra `LoadingIndicator()`
   * `UiState.Success(data)` → muestra el contenido o lista
   * `UiState.Empty` → muestra `EmptyState()`
   * `UiState.Error(message)` → muestra `ErrorMessage(message, onRetry = ...)`

---

## 11. ¿Cómo funciona un GET y un POST en esta arquitectura?

En `data/remote/api/SavingPlanApi.kt` ya tenemos preparados ambos patrones como referencia para las siguientes fases:

* **GET (Consultar datos sin cuerpo):**
  ```kotlin
  @GET("plans")
  suspend fun getSavingPlans(): List<SavingPlanDto>

  @GET("plans/{id}")
  suspend fun getSavingPlanById(@Path("id") id: Int): SavingPlanDto
  ```
  `@Path("id")` reemplaza `{id}` en la URL por el valor numérico pasado por parámetro.

* **POST (Enviar datos JSON en el cuerpo):**
  ```kotlin
  @POST("plans/{id}/members")
  suspend fun addMember(
      @Path("id") planId: Int,
      @Body memberRequest: MemberRequestDto
  ): MemberDto
  ```
  `@Body` indica a Retrofit + Gson que serialice el objeto `MemberRequestDto` a JSON y lo envíe en el cuerpo de la petición HTTP POST.

---

## 12. ¿Cómo se realizan las pruebas unitarias?

Las pruebas unitarias viven en `app/src/test/` y se ejecutan en la JVM de tu computador (sin emulador ni servidor Python) con `./gradlew testDebugUnitTest`.

Actualmente el proyecto cuenta con tres archivos de pruebas reales en `app/src/test/`:
1. **`SavingPlanMapperTest.kt`:** Prueba pura con JUnit que comprueba que `SavingPlanDto.toDomain()` y `List<SavingPlanDto>.toDomain()` transforman datos y protegen campos `null`.
2. **`HomeViewModelTest.kt`:** Usa `MainDispatcherRule.kt` y `StandardTestDispatcher` para comprobar que `HomeViewModel` pasa de `UiState.Loading` a `UiState.Success`.
3. **`PlansViewModelTest.kt`:** Inyecta `FakeSavingPlanRepository` a `PlansViewModel` para probar los 4 estados (`Loading`, `Success`, `Empty`, `Error`) y el reintento tras un fallo, completamente sin red ni servidor.
