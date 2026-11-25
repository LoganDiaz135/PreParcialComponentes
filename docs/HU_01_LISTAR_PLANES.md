# REFERENCIA MAESTRA: HU-01 — CONSULTAR Y LISTAR PLANES DE AHORRO

Este documento convierte la implementación de **HU-01** en la guía práctica de referencia para resolver cualquier historia de usuario de **consulta y listado (`GET` de colección)** durante el examen.

---

## 1. Enunciado de la Historia
> **HU-01:** Como usuario de *PreParcialComponentes*, quiero consultar y visualizar el listado de planes de ahorro familiar disponibles, viendo claramente si la información está cargando, si no existen registros o si ocurrió un error de comunicación, con opción de reintentar.

---

## 2. Objetivo Técnico
Implementar de principio a fin el flujo **UI Compose $\rightarrow$ ViewModel (`StateFlow`) $\rightarrow$ Repository (Interfaz) $\rightarrow$ Implementación (`FakeSavingPlanRepository`)**, demostrando:
1. Separación limpia entre UI, estado, contrato de dominio y origen de datos.
2. Gestión explícita de los 4 estados de `UiState`: `Loading`, `Success`, `Empty` y `Error`.
3. Ejecución y pruebas unitarias sin depender de un servidor Python activo.
4. Sustitución directa por Retrofit (`SavingPlanRepositoryImpl`) cuando el backend exista.

---

## 3. Archivos Involucrados

### Nuevos archivos creados en FASE 2
* `app/src/main/java/com/example/preparcialcomponentes/data/repository/FakeSavingPlanRepository.kt`
* `app/src/main/java/com/example/preparcialcomponentes/presentation/screens/plans/PlansViewModel.kt`
* `app/src/main/java/com/example/preparcialcomponentes/presentation/screens/plans/PlansScreen.kt`
* `app/src/test/java/com/example/preparcialcomponentes/presentation/screens/plans/PlansViewModelTest.kt`

### Archivos existentes conectados o actualizados
* `domain/model/SavingPlan.kt` (modelo `SavingPlan`)
* `domain/repository/SavingPlanRepository.kt` (método `getSavingPlans()`)
* `data/remote/dto/SavingPlanDto.kt` y `data/mapper/SavingPlanMapper.kt` (listos para el modo real)
* `data/remote/api/SavingPlanApi.kt` y `data/repository/SavingPlanRepositoryImpl.kt` (listos para el modo real)
* `presentation/AppViewModelFactory.kt` (registra `PlansViewModel` y selecciona `FakeSavingPlanRepository` por defecto)
* `presentation/navigation/Screen.kt` y `AppNavHost.kt` (ruta `Screen.Plans` conectada a `PlansScreen`)
* `presentation/screens/home/HomeScreen.kt` (botón `"HU-01: Consultar Planes de Ahorro"`)
* `app/src/test/java/com/example/preparcialcomponentes/data/mapper/SavingPlanMapperTest.kt` (agregado test de listado `List<SavingPlanDto>.toDomain()`)

---

## 4. Responsabilidad de Cada Archivo en HU-01

| Archivo | Qué hace en HU-01 |
|---|---|
| `SavingPlan.kt` | Define la estructura limpia `SavingPlan(id, name, targetAmount, currentAmount, description)`. |
| `SavingPlanRepository.kt` | Contrato `suspend fun getSavingPlans(): ApiResult<List<SavingPlan>>`. |
| `FakeSavingPlanRepository.kt` | **Simulación en memoria** (sin HTTP): espera `simulatedDelayMs` y retorna `ApiResult.Success` o `ApiResult.Error`. |
| `SavingPlanRepositoryImpl.kt` | **Implementación real** (inactiva mientras `useFakeData = true`): llama `api.getSavingPlans().toDomain()` dentro de `safeApiCall`. |
| `PlansViewModel.kt` | Ejecuta `loadPlans()` en `viewModelScope` y transforma `ApiResult` en `UiState`. |
| `PlansScreen.kt` | Observa `viewModel.uiState` con `collectAsState()` y dibuja `LoadingIndicator`, `EmptyState`, `ErrorMessage` o `LazyColumn` con `ItemCard`. |
| `AppViewModelFactory.kt` | Entrega la instancia de `SavingPlanRepository` al constructor de `PlansViewModel`. |
| `AppNavHost.kt` | Conecta la ruta `Screen.Plans` con `PlansScreen` y maneja `onBack`. |

---

## 5. Flujo Completo de Datos (Simulación vs Comunicación Real)

> [!IMPORTANT]
> **Distinción clave para la sustentación:** En el estado actual (`useFakeData = true`), **NO ocurre ninguna petición HTTP ni consulta a SQLite**. Los datos provienen de la lista en memoria `FakeSavingPlanRepository.defaultSamplePlans()`.

### 5.1 Flujo que se ejecuta actualmente (Modo Simulado)
```text
HomeScreen ("HU-01: Consultar Planes de Ahorro")
   │  navController.navigate(Screen.Plans.route)
   ▼
AppNavHost -> AppViewModelFactory(useFakeData = true)
   │  Crea PlansViewModel(FakeSavingPlanRepository())
   ▼
PlansViewModel.init -> loadPlans()
   │  1. _uiState.value = UiState.Loading
   │  2. Llama repository.getSavingPlans()
   ▼
FakeSavingPlanRepository.getSavingPlans()
   │  delay(500L) -> retorna ApiResult.Success(3 planes de prueba)
   ▼
PlansViewModel evalúa ApiResult:
   │  Lista con 3 items -> _uiState.value = UiState.Success(data)
   ▼
PlansScreen (collectAsState) -> dibuja LazyColumn con ItemCard
```

### 5.2 Cómo se sustituye por Retrofit Real (Modo Backend Python)
En [AppViewModelFactory.kt](file:///c:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/AppViewModelFactory.kt), cuando exista el backend Python con su contrato confirmado, **solo cambias `useFakeData = false`** (o reemplazas `FakeSavingPlanRepository()` por `SavingPlanRepositoryImpl(RetrofitClient.createService(SavingPlanApi::class.java))`).
**Ni `PlansViewModel` ni `PlansScreen` cambian una sola línea.**

### 5.3 Ejemplo Didáctico de un `GET` Real (Sin inventar un backend en ejecución)
Si un backend FastAPI expusiera el endpoint `GET /plans` devolviendo este JSON hipotético:
```json
[
  {
    "id": 1,
    "name": "Fondo Escolar",
    "target_amount": 2500000.0,
    "current_amount": 750000.0,
    "description": "Útiles y matrícula"
  }
]
```
Así actuarían nuestras piezas reales ya preparadas:
1. **Retrofit (`SavingPlanApi.kt`):**
   ```kotlin
   @GET("plans")
   suspend fun getSavingPlans(): List<SavingPlanDto>
   ```
   OkHttp envía `GET http://10.0.2.2:8000/plans`.
2. **Gson + DTO (`SavingPlanDto.kt`):**
   Convierte `"target_amount"` $\rightarrow$ `targetAmount` usando `@SerializedName("target_amount")`.
3. **Mapper (`SavingPlanMapper.kt`):**
   `api.getSavingPlans().toDomain()` convierte `SavingPlanDto` $\rightarrow$ `SavingPlan` protegiendo posibles `null`.
4. **Manejo de errores (`safeApiCall` en `ApiResult.kt`):**
   * Si el servidor Python está apagado $\rightarrow$ `ConnectException` $\rightarrow$ `ApiResult.Error("No se pudo conectar...")`.
   * Si la ruta no existe $\rightarrow$ `HttpException(404)` $\rightarrow$ `ApiResult.Error("Recurso no encontrado (404)")`.
   * Si el JSON cambió de formato $\rightarrow$ `JsonSyntaxException` $\rightarrow$ `ApiResult.Error("Error al procesar el formato JSON...")`.

---

## 6. Explicación de la Implementación

* **`PlansViewModel.kt`:**
  * Llama `loadPlans()` en `init` para cargar automáticamente al abrir la pantalla.
  * Emite `UiState.Loading` antes de invocar al repositorio para mostrar el spinner inmediato.
  * Si `result` es `ApiResult.Success` y `result.data.isEmpty()`, emite `UiState.Empty` (distinto de error).
* **`PlansScreen.kt`:**
  * Usa `Scaffold` + `TopAppBar` con flecha atrás (`onBack`) y botón de recarga (`Icons.Default.Refresh`).
  * Usa `LazyColumn` con `key = { plan -> plan.id }` para rendimiento óptimo en listas.
  * Reutiliza `ItemCard` de `presentation/components/` y formatea valores monetarios con `NumberFormat`.

---

## 7. Estados de la Interfaz

| Estado (`UiState`) | Cuándo ocurre | Componente Compose mostrado |
|---|---|---|
| `UiState.Loading` | Al entrar a la pantalla o pulsar recargar/reintentar | `LoadingIndicator("Cargando planes de ahorro...")` |
| `UiState.Success(plans)` | `ApiResult.Success` con al menos 1 plan | `LazyColumn` con tarjetas `ItemCard` |
| `UiState.Empty` | `ApiResult.Success(emptyList())` | `EmptyState("No hay planes de ahorro registrados.")` |
| `UiState.Error(msg)` | `ApiResult.Error(msg)` (red, HTTP, JSON o error simulado) | `ErrorMessage(msg, onRetry = { viewModel.loadPlans() })` |

---

## 8. Pruebas Unitarias

En `PlansViewModelTest.kt` se verifican los 4 estados y el reintento usando `MainDispatcherRule(StandardTestDispatcher())` y `FakeSavingPlanRepository(..., simulatedDelayMs = 0L)`:

1. `loadPlans emite Loading y luego Success cuando existen planes`: Comprueba `UiState.Loading` antes de `advanceUntilIdle()` y `UiState.Success` con 2 elementos después.
2. `loadPlans emite Empty cuando el repositorio devuelve una lista vacia`: Comprueba `UiState.Empty`.
3. `loadPlans emite Error cuando el repositorio falla`: Configura `shouldReturnError = true` y verifica el mensaje en `UiState.Error`.
4. `reintentar loadPlans despues de un Error pasa a Success...`: Simula falla inicial $\rightarrow$ cambia `shouldReturnError = false` $\rightarrow$ llama `viewModel.loadPlans()` $\rightarrow$ verifica `Loading` $\rightarrow$ `Success`.

Adicionalmente, `SavingPlanMapperTest.kt` incluye `toDomain mapea una lista de SavingPlanDto conservando el orden`.

---

## 9. Cómo Verificar la Funcionalidad

1. **Pruebas unitarias (sin emulador ni servidor):**
   ```bash
   ./gradlew testDebugUnitTest
   ```
2. **Compilación e instalación en emulador:**
   * Ejecuta la app en Android Studio.
   * En `HomeScreen`, pulsa **"HU-01: Consultar Planes de Ahorro"**.
   * Verás `LoadingIndicator` durante ~500 ms y luego los 3 planes de prueba de `FakeSavingPlanRepository`.
   * Pulsa el ícono de refrescar arriba a la derecha para observar de nuevo el ciclo `Loading` $\rightarrow$ `Success`.
3. **Cómo probar `Empty` o `Error` manualmente en el emulador (opcional):**
   * En `FakeSavingPlanRepository`, pasa temporalmente `plansToReturn = emptyList()` o `shouldReturnError = true` en `AppViewModelFactory` para ver `EmptyState` o `ErrorMessage` con el botón `Reintentar`.

---

## 10. Cómo Adaptar HU-01 a un Listado de Otra Entidad en el Parcial

Supón que el profesor pide listar **Productos**, **Estudiantes** o **Proyectos**. Observa lo poco que cambia:

### Qué se CONSERVA intacto (no se toca)
* `core/common/UiState.kt`, `core/common/ApiResult.kt`, `core/network/RetrofitClient.kt`
* `presentation/components/` (`LoadingIndicator`, `EmptyState`, `ErrorMessage`, `ItemCard`, `AppButton`)
* `MainActivity.kt` y `MainDispatcherRule.kt`

### Qué CAMBIA según la entidad del examen

| Pieza | En HU-01 (Ahorro) | Si piden **Productos** (Inventario) | Si piden **Estudiantes** | Si piden **Proyectos** |
|---|---|---|---|---|
| **Modelo (`domain/model/`)** | `SavingPlan` (`name`, `targetAmount`, `currentAmount`) | `Product` (`name`, `price`, `stock`) | `Student` (`fullName`, `code`, `average`) | `Project` (`title`, `budget`, `status`) |
| **DTO (`data/remote/dto/`)** | `SavingPlanDto` | `ProductDto` (`@SerializedName("unit_price")`, `"stock"`) | `StudentDto` (`@SerializedName("full_name")`, `"student_code"`) | `ProjectDto` (`@SerializedName("project_title")`, `"budget"`) |
| **Endpoint (`data/remote/api/`)** | `@GET("plans")` | `@GET("products")` | `@GET("students")` | `@GET("projects")` |
| **Repository (`domain/` + `data/`)** | `SavingPlanRepository` / `Impl` / `Fake` | `ProductRepository` / `Impl` / `Fake` | `StudentRepository` / `Impl` / `Fake` | `ProjectRepository` / `Impl` / `Fake` |
| **ViewModel (`presentation/screens/`)** | `PlansViewModel` (`loadPlans()`) | `ProductsViewModel` (`loadProducts()`) | `StudentsViewModel` (`loadStudents()`) | `ProjectsViewModel` (`loadProjects()`) |
| **Screen (`presentation/screens/`)** | `PlansScreen` (muestra ahorrado/meta) | `ProductsScreen` (muestra precio/stock en `ItemCard`) | `StudentsScreen` (muestra código/promedio en `ItemCard`) | `ProjectsScreen` (muestra presupuesto/estado en `ItemCard`) |
| **Factory (`AppViewModelFactory`)** | Registra `PlansViewModel` | Registra `ProductsViewModel` | Registra `StudentsViewModel` | Registra `ProjectsViewModel` |
| **Navegación (`Screen` + `AppNavHost`)** | `Screen.Plans` | `Screen.Products` | `Screen.Students` | `Screen.Projects` |
| **Test (`test/`)** | `PlansViewModelTest` + `SavingPlanMapperTest` | `ProductsViewModelTest` + `ProductMapperTest` | `StudentsViewModelTest` + `StudentMapperTest` | `ProjectsViewModelTest` + `ProjectMapperTest` |

**Regla de oro para el examen:** La estructura de `PlansViewModel` (~35 líneas) y `PlansScreen` (~130 líneas) es exactamente la misma para cualquier listado; solo cambian el tipo genérico `UiState<List<NuevaEntidad>>`, el método del repositorio y los textos dentro de `ItemCard`.
