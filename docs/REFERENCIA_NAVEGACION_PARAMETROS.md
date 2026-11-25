# REFERENCIA MAESTRA: NAVEGACIÓN CON PARÁMETROS (`detail/{id}`)

Este documento explica de forma clara y aplicable cómo definir y consumir **rutas dinámicas con parámetros en Jetpack Compose** durante el examen (por ejemplo, para navegar a la vista de detalle de un elemento seleccionándolo de una lista).

---

## 1. Enunciado Típico de Examen
> **HU-DETALLE:** Como usuario, quiero seleccionar un elemento de la lista para navegar a su pantalla de detalle pasando su identificador (`id`), permitiendo consultar la información completa del recurso desde la API o un repositorio simulado.

---

## 2. Declaración de Rutas en `Screen.kt`

Para evitar escribir cadenas hardcodeadas, define las rutas en `presentation/navigation/Screen.kt`:

```kotlin
// presentation/navigation/Screen.kt
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Plans : Screen("plans")

    // Ruta estática con parámetro entre llaves {planId}
    object Detail : Screen("detail/{planId}") {
        // Función helper para construir la ruta real con el ID concreto
        fun createRoute(planId: Int): String = "detail/$planId"
    }
}
```

---

## 3. Configuración del Grafo en `AppNavHost.kt`

En `presentation/navigation/AppNavHost.kt`, declara el `composable` especificando la clave y el tipo de dato del argumento (`NavType.IntType`):

```kotlin
// presentation/navigation/AppNavHost.kt
composable(
    route = Screen.Detail.route, // "detail/{planId}"
    arguments = listOf(
        navArgument("planId") {
            type = NavType.IntType
            defaultValue = -1
        }
    )
) { backStackEntry ->
    // Extraer el argumento de forma segura
    val planId = backStackEntry.arguments?.getInt("planId") ?: -1

    // Instanciar el ViewModel
    val detailViewModel: DetailViewModel = viewModel(factory = factory)

    // Lanzar la carga inicial pasando el ID
    LaunchedEffect(planId) {
        if (planId != -1) {
            detailViewModel.loadDetail(planId)
        }
    }

    // Renderizar la pantalla Compose
    DetailScreen(
        viewModel = detailViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

---

## 4. Disparo de la Navegación desde la UI Lista

En la pantalla de listado (`PlansScreen.kt`), al hacer clic en un ítem de la `LazyColumn`, invocas la función `createRoute`:

```kotlin
// Dentro de PlansScreen.kt
items(items = plans, key = { plan -> plan.id }) { plan ->
    ItemCard(
        onClick = { onPlanClick(plan.id) } // Pasa el ID al callback de navegación
    ) {
        Text(text = plan.name, style = MaterialTheme.typography.titleMedium)
        // ...
    }
}
```

Y en `AppNavHost.kt`:
```kotlin
PlansScreen(
    viewModel = plansViewModel,
    onBack = { navController.popBackStack() },
    onPlanClick = { planId ->
        navController.navigate(Screen.Detail.createRoute(planId))
    }
)
```

---

## 5. Captura del ID en el ViewModel

El `DetailViewModel` recibe el ID y consulta al repositorio:

```kotlin
// presentation/screens/detail/DetailViewModel.kt
class DetailViewModel(
    private val repository: SavingPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SavingPlan>>(UiState.Loading)
    val uiState: StateFlow<UiState<SavingPlan>> = _uiState.asStateFlow()

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getSavingPlanById(id)) {
                is ApiResult.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
```

---

## 6. Errores Comunes en Navegación con Parámetros

1. **Nombre de clave desalineado:** Si en `Screen.kt` pones `"detail/{id}"` pero en `AppNavHost.kt` lees `getInt("planId")`, `planId` valdrá siempre `-1` o `0`. El nombre dentro de `{...}` y en `getInt("...")` debe ser **idéntico**.
2. **Omitir `NavType`:** No especificar `type = NavType.IntType` hace que Compose lo trate como `String`, fallando al convertir a `Int`.
3. **Múltiples llamadas en `LaunchedEffect`:** Usar `LaunchedEffect(planId)` garantiza que solo se solicite la carga cuando `planId` cambie o la pantalla se inicie por primera vez.
