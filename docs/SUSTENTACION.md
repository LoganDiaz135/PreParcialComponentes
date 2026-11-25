# GUÍA DE SUSTENTACIÓN TÉCNICA — PreParcialComponentes

Preguntas frecuentes en una sustentación universitaria de Android / Programación por Componentes, respondidas con referencia directa a los archivos reales del proyecto.

---

## 1. ¿Por qué se utiliza MVVM en este proyecto?
Para separar la interfaz gráfica (Jetpack Compose) de la lógica de estado y del acceso a datos.
* **En el proyecto:** `HomeScreen.kt` solo dibuja los estados (`Loading`, `Success`, `Empty`, `Error`) y llama acciones de `HomeViewModel.kt`. `HomeViewModel` mantiene el estado en un `StateFlow` que sobrevive a rotaciones de pantalla y nunca contiene referencias a vistas ni llamadas directas a Retrofit.

## 2. ¿Qué responsabilidad tiene el Repository?
Centralizar el acceso a los datos y desacoplar al ViewModel de la fuente concreta (API REST, caché o Fake).
* **En el proyecto:** `domain/repository/SavingPlanRepository.kt` define el contrato (`suspend fun getSavingPlans(): ApiResult<List<SavingPlan>>`) y `data/repository/SavingPlanRepositoryImpl.kt` lo implementa llamando a `SavingPlanApi` dentro de `safeApiCall`. El ViewModel solo depende de la interfaz.

## 3. ¿Qué es Retrofit?
Es un cliente HTTP declarativo para Android/JVM que convierte interfaces Kotlin con anotaciones (`@GET`, `@POST`, `@Path`, `@Body`) en peticiones HTTP reales.
* **En el proyecto:** Se configura como singleton en `core/network/RetrofitClient.kt` usando `NetworkConfig.BASE_URL` y `GsonConverterFactory`, y se declara en `data/remote/api/SavingPlanApi.kt`.

## 4. ¿Qué es OkHttp y para qué lo configuramos?
Es el motor HTTP de bajo nivel sobre el que trabaja Retrofit.
* **En el proyecto:** En `core/network/RetrofitClient.kt` configuramos `OkHttpClient` con timeouts de 30s (`NetworkConfig.kt`) y un `HttpLoggingInterceptor` en nivel `BODY` para ver en Logcat las URLs, códigos de respuesta y cuerpos JSON completos durante el desarrollo.

## 5. ¿Qué es un DTO y qué diferencia existe con un Modelo de Dominio?
* **DTO (Data Transfer Object):** Representa exactamente el JSON del servidor. En `data/remote/dto/SavingPlanDto.kt` tiene anotaciones `@SerializedName("target_amount")` y propiedades nulables (`Double?`) para tolerar respuestas incompletas.
* **Modelo de Dominio:** Es la clase limpia que usa la aplicación (`domain/model/SavingPlan.kt`), sin anotaciones de Gson ni campos nulos innecesarios.
* **Por qué separarlos:** Si el backend cambia el nombre de una clave JSON, solo actualizamos el DTO y `SavingPlanMapper.kt`; las pantallas y ViewModels no se tocan.

## 6. ¿Qué es StateFlow y por qué exponemos `StateFlow` público y `MutableStateFlow` privado?
`StateFlow` es un flujo observable de Kotlin Coroutines que siempre conserva el último estado emitido y notifica a Compose cuando cambia.
* **En el proyecto:** En `HomeViewModel.kt`, `_uiState` es `MutableStateFlow` privado para que **solo el ViewModel** pueda cambiar el estado, y `uiState = _uiState.asStateFlow()` es de solo lectura para que `HomeScreen.kt` lo observe con `collectAsState()` sin poder alterarlo.

## 7. ¿Qué hace exactamente el ViewModel?
1. Mantiene los datos y el `UiState` de la pantalla mientras la pantalla esté viva (incluso si rota).
2. Lanza corrutinas en `viewModelScope` para operaciones asíncronas.
3. Ejecuta validaciones de presentación antes de enviar datos.
4. Invoca al Repository y transforma `ApiResult` en `UiState` (`Loading`, `Success`, `Empty`, `Error`).

## 8. ¿Cómo se realiza una solicitud GET?
1. En `SavingPlanApi.kt` se declara un método `suspend` anotado con `@GET("plans")` (o `@GET("plans/{id}")` con `@Path("id")`).
2. `SavingPlanRepositoryImpl` lo invoca dentro de `safeApiCall { api.getSavingPlans().toDomain() }`.
3. Retrofit construye la petición `GET http://10.0.2.2:8000/plans`, Gson convierte el JSON recibido a `List<SavingPlanDto>` y el mapper lo convierte a `List<SavingPlan>`.

## 9. ¿Cómo se envía un POST?
1. Creamos un DTO de solicitud sin `id` (ej. `MemberRequestDto` en `SavingPlanDto.kt`).
2. En `SavingPlanApi.kt` anotamos el método con `@POST("plans/{id}/members")`, usando `@Path("id")` para la URL y `@Body memberRequest: MemberRequestDto` para el cuerpo JSON.
3. Gson serializa el DTO a texto JSON, OkHttp lo envía al servidor FastAPI y la respuesta se mapea a `Member` dentro de `safeApiCall`.

## 10. ¿Cómo se gestiona un error del servidor o de red?
Todas las llamadas de datos pasan por `safeApiCall` en `core/common/ApiResult.kt`. Allí un bloque `try/catch` captura `HttpException` (400, 404, 500), `ConnectException` (servidor caído), `SocketTimeoutException` y `JsonSyntaxException`, devolviendo `ApiResult.Error(mensaje)`. El ViewModel lo convierte en `UiState.Error(mensaje)` y la UI muestra `ErrorMessage.kt` con botón de reintento.

## 11. ¿Cómo se prueba un ViewModel sin servidor?
Usando **JUnit**, `kotlinx-coroutines-test`, un `FakeRepository` en memoria (que implementa la interfaz del repositorio sin Retrofit) y `MainDispatcherRule.kt` (en `app/src/test/.../util/`), que reemplaza `Dispatchers.Main` de Android por un dispatcher de prueba en la JVM.
* **Ejemplo real actual:** `PlansViewModelTest.kt` inyecta `FakeSavingPlanRepository` a `PlansViewModel` y verifica `Loading`, `Success`, `Empty`, `Error` y la recuperación al reintentar, sin depender de backend ni emulador.

## 12. ¿Por qué no se utiliza Room ni Hilt?
* **Sin Room:** La persistencia oficial está en el backend Python con SQLite. Agregar una base de datos local en Android duplicaría la fuente de verdad y añadiría complejidad innecesaria en un examen.
* **Sin Hilt/Dagger:** La inyección manual mediante `AppViewModelFactory.kt` (usando `by lazy`) es transparente, fácil de depurar en segundos durante un parcial y no depende de procesadores de anotaciones (`kapt`/`ksp`).

## 13. ¿Cómo se cambia un endpoint o la URL base?
* **URL base / puerto:** Se modifica únicamente `BASE_URL` en `core/network/NetworkConfig.kt`.
* **Endpoint específico:** Se modifica la anotación (`@GET("...")`, `@POST("...")`) en la interfaz de `data/remote/api/` (ej. `SavingPlanApi.kt`).

## 14. ¿Cómo se agrega una nueva entidad?
1. Crear el modelo limpio en `domain/model/`.
2. Crear los DTOs (respuesta y/o request) en `data/remote/dto/` y el mapper `.toDomain()` en `data/mapper/`.
3. Agregar los endpoints en `data/remote/api/` (reutilizando la API existente o creando una nueva).
4. Definir/ampliar el contrato en `domain/repository/` y su implementación en `data/repository/`.
5. Registrar el repositorio en `presentation/AppViewModelFactory.kt` si creaste uno nuevo.

## 15. ¿Cómo se agrega una nueva pantalla?
1. Crear la carpeta en `presentation/screens/<nombre>/` con su `<Nombre>ViewModel.kt` y `<Nombre>Screen.kt` (reutilizando `UiState` y los componentes de `presentation/components/`).
2. Registrar el ViewModel en `presentation/AppViewModelFactory.kt`.
3. Agregar la ruta en `presentation/navigation/Screen.kt` y el `composable(...)` correspondiente en `AppNavHost.kt`.

## 16. ¿Por qué en `NetworkConfig.kt` usamos `10.0.2.2` y en `AndroidManifest.xml` activamos `usesCleartextTraffic="true"`?
* `10.0.2.2` es la IP especial con la que el emulador Android apunta al `localhost` de la máquina anfitriona donde corre el backend Python.
* `usesCleartextTraffic="true"` permite tráfico `http://` sin cifrado TLS, que Android 9+ bloquea por defecto pero que es necesario en desarrollo local contra FastAPI.
