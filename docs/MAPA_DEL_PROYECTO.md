# MAPA DEL PROYECTO — PreParcialComponentes

Este documento muestra la ubicación real de cada carpeta y archivo del proyecto en su estado actual (**FASE 2 — HU-01 completada**) y qué archivos intervienen en cada flujo.

---

## 1. Árbol Real del Proyecto

```text
pre_parcial/
├── app/
│   ├── build.gradle.kts                          # Dependencias (Compose, Retrofit, Gson, OkHttp, Coroutines, JUnit)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml               # Permiso INTERNET y usesCleartextTraffic="true"
│       │   ├── res/                              # strings.xml, themes.xml, ic_launcher.xml
│       │   └── java/com/example/preparcialcomponentes/
│       │       ├── MainActivity.kt               # Entry point único; lanza MaterialTheme + AppNavHost()
│       │       │
│       │       ├── core/                         # [REUTILIZABLE 100%] Configuración transversal
│       │       │   ├── common/
│       │       │   │   ├── ApiResult.kt          # Wrapper Success/Error + safeApiCall()
│       │       │   │   └── UiState.kt            # Loading / Empty / Success / Error
│       │       │   └── network/
│       │       │       ├── NetworkConfig.kt      # BASE_URL (10.0.2.2:8000) y timeouts
│       │       │       └── RetrofitClient.kt     # Singleton Retrofit + OkHttp + Logging + Gson
│       │       │
│       │       ├── domain/                       # [ADAPTABLE POR ENUNCIADO] Núcleo de negocio
│       │       │   ├── model/
│       │       │   │   └── SavingPlan.kt         # Modelos de ejemplo: SavingPlan, Member, PaymentRecord
│       │       │   └── repository/
│       │       │       └── SavingPlanRepository.kt # Interfaz contrato del repositorio
│       │       │
│       │       ├── data/                         # [ADAPTABLE POR ENUNCIADO] Conexión REST y mapeo
│       │       │   ├── mapper/
│       │       │   │   └── SavingPlanMapper.kt   # Extensiones .toDomain() (DTO -> Dominio)
│       │       │   ├── remote/
│       │       │   │   ├── api/
│       │       │   │   │   └── SavingPlanApi.kt  # Endpoints Retrofit (@GET, @POST)
│       │       │   │   └── dto/
│       │       │   │       └── SavingPlanDto.kt  # DTOs con @SerializedName
│       │       │   └── repository/
│       │       │       ├── FakeSavingPlanRepository.kt # Simulación en memoria (sin HTTP)
│       │       │       └── SavingPlanRepositoryImpl.kt # Implementación real Retrofit + safeApiCall
│       │       │
│       │       └── presentation/                 # UI Compose, ViewModels, Navegación y Factory
│       │           ├── AppViewModelFactory.kt    # Fábrica manual de dependencias y ViewModels
│       │           ├── components/               # [REUTILIZABLE 100%] Componentes UI genéricos
│       │           │   ├── AppButton.kt
│       │           │   ├── AppTextField.kt
│       │           │   ├── EmptyState.kt
│       │           │   ├── ErrorMessage.kt
│       │           │   ├── ItemCard.kt
│       │           │   └── LoadingIndicator.kt
│       │           ├── navigation/
│       │           │   ├── AppNavHost.kt         # Grafo Compose (Home -> Plans)
│       │           │   └── Screen.kt             # Rutas activas (Home, Plans)
│       │           └── screens/
│       │               ├── home/
│       │               │   ├── HomeScreen.kt     # Pantalla principal de la plantilla
│       │               │   └── HomeViewModel.kt  # ViewModel inicial con StateFlow
│       │               └── plans/
│       │                   ├── PlansScreen.kt    # HU-01: Listado con Loading/Empty/Error/Success
│       │                   └── PlansViewModel.kt # HU-01: ViewModel conectado a SavingPlanRepository
│       │
│       └── test/java/com/example/preparcialcomponentes/ # Pruebas unitarias en JVM
│           ├── util/
│           │   └── MainDispatcherRule.kt         # Regla JUnit para Dispatchers.Main en ViewModels
│           ├── data/mapper/
│           │   └── SavingPlanMapperTest.kt       # Test puro de mapeo DTO -> Dominio
│           └── presentation/screens/
│               ├── home/
│               │   └── HomeViewModelTest.kt      # Test de estados de HomeViewModel
│               └── plans/
│                   └── PlansViewModelTest.kt     # HU-01: Test de 4 estados + reintento con FakeRepository
│
├── docs/                                         # Documentación de estudio y adaptación
│   ├── GUIA_DE_ESTUDIO.md
│   ├── MAPA_DEL_PROYECTO.md
│   ├── DICCIONARIO_DE_ARCHIVOS.md
│   ├── FLUJO_DE_DATOS.md
│   ├── ADAPTACION_AL_EXAMEN.md
│   ├── EJERCICIOS_DE_ADAPTACION.md
│   ├── SUSTENTACION.md
│   └── HU_01_LISTAR_PLANES.md                    # Referencia maestra de HU-01 (10 puntos + adaptación)
│
├── build.gradle.kts                              # Plugins raíz (AGP 8.5.2, Kotlin 1.9.24)
├── settings.gradle.kts                           # Nombre del proyecto y módulo :app
├── gradle.properties                             # Memoria JVM y flags de AndroidX/SDK
└── local.properties                              # Ruta local al Android SDK (no versionar)
```

---

## 2. Mapa de Archivos por Flujo

### Flujo A: Arranque de la app y HU-01 (Activo actualmente en modo simulado)
1. `MainActivity.kt` → inicia Compose y llama a `AppNavHost()`.
2. `AppNavHost.kt` → carga `Screen.Home` y crea `HomeViewModel` vía `AppViewModelFactory`.
3. `HomeScreen.kt` → muestra `AppButton("HU-01: Consultar Planes de Ahorro")`.
4. Al pulsar el botón → `AppNavHost.kt` navega a `Screen.Plans`, instancia `PlansViewModel(savingPlanRepository)` y muestra `PlansScreen.kt`.
5. `PlansViewModel.kt` → consulta `FakeSavingPlanRepository.getSavingPlans()` (simulado en memoria mientras `useFakeData = true`) y emite `Loading` → `Success` / `Empty` / `Error`.

### Flujo B: Conexión REST real preparada (cuando exista backend Python)
1. `NetworkConfig.kt` + `RetrofitClient.kt` → configuran la URL base, OkHttp Logging y Gson.
2. `AppViewModelFactory(useFakeData = false)` → cambia `FakeSavingPlanRepository` por `SavingPlanRepositoryImpl(RetrofitClient.createService(SavingPlanApi::class.java))`.
3. `SavingPlanApi.kt` + `SavingPlanDto.kt` + `SavingPlanMapper.kt` + `ApiResult.kt` (`safeApiCall`) → ejecutan la petición HTTP real sin modificar `PlansViewModel` ni `PlansScreen`.

### Flujo C: Ejecución de Pruebas Unitarias Locales (Activo actualmente)
1. `SavingPlanMapperTest.kt` → prueba directamente `SavingPlanDto.toDomain()` y `List<SavingPlanDto>.toDomain()`.
2. `HomeViewModelTest.kt` + `MainDispatcherRule.kt` → prueban `HomeViewModel`.
3. `PlansViewModelTest.kt` + `MainDispatcherRule.kt` + `FakeSavingPlanRepository` → prueban los 4 estados y el reintento de `PlansViewModel`.

---

## 3. Guía Rápida: "¿Qué archivo abro si necesito...?"

| Necesidad durante el parcial | Archivo(s) donde actuar |
|---|---|
| Cambiar la IP o puerto del servidor Python | `core/network/NetworkConfig.kt` |
| Ver los JSON reales en Logcat o cambiar timeouts | `core/network/RetrofitClient.kt` (ya tiene `Level.BODY`) |
| Ajustar un nombre de campo que viene distinto en el JSON | `data/remote/dto/SavingPlanDto.kt` (`@SerializedName`) |
| Cambiar un endpoint (`@GET`, `@POST`, ruta, `@Path`) | `data/remote/api/SavingPlanApi.kt` |
| Agregar/quitar atributos en la app o valores por defecto | `domain/model/SavingPlan.kt` y `data/mapper/SavingPlanMapper.kt` |
| Registrar un nuevo `Repository` o `ViewModel` | `presentation/AppViewModelFactory.kt` |
| Agregar una nueva pantalla al flujo de navegación | `presentation/navigation/Screen.kt` y `AppNavHost.kt` |
| Reutilizar botón, input con error, carga, error o tarjeta | `presentation/components/` |
| Agregar una librería o revisar versiones | `app/build.gradle.kts` |
