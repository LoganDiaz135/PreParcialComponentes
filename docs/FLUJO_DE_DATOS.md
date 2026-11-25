# FLUJO DE DATOS EXHAUSTIVO — PreParcialComponentes

Este documento explica paso a paso cómo viajan los datos en la aplicación, desglosando las dos operaciones fundamentales que pueden solicitarse en el examen: **Lectura de datos (`GET`)** y **Envío de datos con validación local (`POST`)**.

---

## 1. Flujo A: Operación de Consulta y Listado (`GET`)

Este flujo ya se encuentra funcional en el proyecto para **HU-01** (`HomeScreen` $\rightarrow$ `PlansScreen`).

### Diagrama Secuencial
```text
[PlansScreen.kt]
       │  collectAsState() observa ViewModel.uiState
       │  Acción: viewModel.loadPlans()
       ▼
[PlansViewModel.kt]
       │  1. _uiState.value = UiState.Loading
       │  2. viewModelScope.launch { repository.getSavingPlans() }
       ▼
[SavingPlanRepository (Interfaz)]
       │  Desacopla el ViewModel del origen real de datos
       ▼
[FakeSavingPlanRepository]          o          [SavingPlanRepositoryImpl]
(Modo Simulado: useFakeData = true)             (Modo Real: useFakeData = false)
       │                                               │
       ├─ delay(500ms)                                 ├─ safeApiCall { api.getSavingPlans().toDomain() }
       └─ ApiResult.Success(samplePlans)               ├─ Retrofit GET http://10.0.2.2:8000/plans
                                                       ├─ Gson deserializa a List<SavingPlanDto>
                                                       └─ Mapper convierte a List<SavingPlan>
       │                                               │
       └───────────────────────┬───────────────────────┘
                               ▼
[PlansViewModel.kt]
       │  Evalúa el ApiResult devuelto:
       │  - Si Success con elementos  -> _uiState.value = UiState.Success(list)
       │  - Si Success con lista vacía -> _uiState.value = UiState.Empty
       │  - Si Error                  -> _uiState.value = UiState.Error(message)
       ▼
[PlansScreen.kt]
       │  Recomposición de Compose según el estado:
       │  - UiState.Loading -> LoadingIndicator()
       │  - UiState.Empty   -> EmptyState()
       │  - UiState.Error   -> ErrorMessage(onRetry)
       │  - UiState.Success -> LazyColumn(ItemCard)
```

---

## 2. Flujo B: Operación de Envío de Datos (`POST` con Validación Local)

Este flujo describe el recorrido paso a paso cuando el usuario llena un formulario en pantalla para crear un nuevo registro.

### Diagrama Secuencial

```text
[FormScreen.kt]
       │  Usuario escribe en AppTextField y toca AppButton("Guardar")
       │  Invoca viewModel.submitForm()
       ▼
[FormViewModel.kt]
       │  PASO 1: Validación Local de Campos en Kotlin (0 ms, sin consumir red)
       │  - ¿Nombre no vacío?
       │  - ¿Monto/Precio válido y > 0?
       │
       ├────────► SI HAY ERROR DE VALIDACIÓN:
       │          Asigna mensaje de error a state local (ej. priceError = "Precio debe ser mayor a 0").
       │          Detiene el flujo. La UI resalta el campo en rojo. NO toca el Repository ni la red.
       │
       └────────► SI LA VALIDACIÓN ES EXITOSA:
                  1. _uiState.value = UiState.Loading
                  2. Construye DTO Request (sin 'id')
                  3. Lanza corrutina: repository.createEntity(request)
       ▼
[RepositoryImpl / FakeRepository]
       │  PASO 2: Procesamiento del Envío
       │  - En Modo Real: safeApiCall { api.createEntity(requestDto).toDomain() }
       │  - Retrofit + Gson envían HTTP POST con cuerpo JSON {"name": "...", "price": 100.0}
       │  - Servidor responde HTTP 201/200 OK con JSON de la entidad creada incluyendo 'id'
       │  - Mapper `.toDomain()` convierte DTO Response -> Modelo de Dominio
       ▼
[FormViewModel.kt]
       │  PASO 3: Emisión del Resultado
       │  - Si ApiResult.Success -> _uiState.value = UiState.Success(entidadCreada)
       │  - Si ApiResult.Error   -> _uiState.value = UiState.Error(mensajeDeRed)
       ▼
[FormScreen.kt]
       │  PASO 4: Respuesta Visual en Compose
       │  - Si UiState.Loading -> Muestra AppButton con spinner
       │  - Si UiState.Error   -> Muestra ErrorMessage con el fallo devuelto
       │  - Si UiState.Success -> Ejecuta LaunchedEffect { onNavigateBack() }
```

---

## 3. Matriz de Transformación de Objetos por Capa

| Capa | Tipo de Objeto en `GET` | Tipo de Objeto en `POST` | Propósito |
|---|---|---|---|
| **UI (Compose)** | Formato legible en pantalla (`String`, `NumberFormat`) | Entradas de texto puras (`var input by mutableStateOf("")`) | Captura de eventos e interacción visual con el usuario. |
| **ViewModel** | `StateFlow<UiState<List<T>>>` | `StateFlow<UiState<T>?>` + variables de error de campo | Mantiene el estado sobreviviente a rotación y ejecuta validaciones. |
| **Repository** | `suspend fun get(): ApiResult<List<Domain>>` | `suspend fun create(dto): ApiResult<Domain>` | Contrato desacoplado que retorna `ApiResult`. |
| **Data (DTO Request)** | No aplica | `EntityRequestDto` (sin `id`) | Objeto serializable enviado en el cuerpo HTTP `@Body`. |
| **Data (DTO Response)** | `List<EntityDto>` (campos nulables) | `EntityDto` (con `id` asignado por servidor) | Respuesta deserializada por Gson con `@SerializedName`. |
| **Data (Mapper)** | `dtoList.toDomain()` | `dto.toDomain()` | Convierte DTOs en entidades puras asegurando valores no nulos. |
| **Domain (Model)** | `List<SavingPlan>` / `List<Product>` | `SavingPlan` / `Product` | Estructura limpia consumida por la aplicación. |

---

## 4. Gestión Transversal de Errores en el Flujo

Cualquier fallo durante el flujo de datos es interceptado y convertido a un mensaje comprensible sin cerrar la aplicación:

1. **Error de Validación Local (UI/ViewModel):** Se detecta en el ViewModel antes de llamar a la red (ej. campo numérico con letras). No genera llamada HTTP.
2. **Error de Conexión de Red (`ConnectException`):** Capturado por `safeApiCall`. Ocurre si el backend Python está apagado o la IP `10.0.2.2` es incorrecta.
3. **Error del Servidor HTTP (`HttpException` 404/500):** Capturado por `safeApiCall`. Ocurre si la ruta REST no existe o la base de datos SQLite lanzó una excepción en Python.
4. **Error de Formato JSON (`JsonSyntaxException`):** Capturado por `safeApiCall`. Ocurre si el servidor devuelven claves JSON distintas a las anotadas en `@SerializedName`.
