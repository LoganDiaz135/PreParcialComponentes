# GUÍA PRÁCTICA DE MODIFICACIÓN DE CÓDIGO — PreParcialComponentes

Esta guía práctica enseña exactamente **dónde ingresar, qué cambiar, qué conservar y cómo verificar** los archivos del proyecto al adaptar la aplicación a un enunciado desconocido durante el examen.

---

## 1. Mapa de Navegación y Referencias del Código

Esta tabla es la guía rápida para estudiar y saber a dónde ir según la tarea que te soliciten:

| Si necesito... | Debo buscar en la carpeta... | Archivo real de referencia |
|---|---|---|
| **Crear o adaptar una entidad** | `app/src/main/java/.../domain/model/` | [SavingPlan.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/domain/model/SavingPlan.kt) |
| **Modelar el JSON del servidor** | `app/src/main/java/.../data/remote/dto/` | [SavingPlanDto.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/remote/dto/SavingPlanDto.kt) |
| **Convertir DTOs a modelos limpios** | `app/src/main/java/.../data/mapper/` | [SavingPlanMapper.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/mapper/SavingPlanMapper.kt) |
| **Definir endpoints Retrofit** | `app/src/main/java/.../data/remote/api/` | [SavingPlanApi.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/remote/api/SavingPlanApi.kt) |
| **Definir contrato de datos** | `app/src/main/java/.../domain/repository/` | [SavingPlanRepository.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/domain/repository/SavingPlanRepository.kt) |
| **Implementar llamada con red** | `app/src/main/java/.../data/repository/` | [SavingPlanRepositoryImpl.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/repository/SavingPlanRepositoryImpl.kt) |
| **Simular datos en memoria** | `app/src/main/java/.../data/repository/` | [FakeSavingPlanRepository.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/repository/FakeSavingPlanRepository.kt) |
| **Registrar e inyectar ViewModels** | `app/src/main/java/.../presentation/` | [AppViewModelFactory.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/AppViewModelFactory.kt) |
| **Manejar el estado de la UI** | `app/src/main/java/.../presentation/screens/` | [PlansViewModel.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/screens/plans/PlansViewModel.kt) |
| **Construir pantallas Compose** | `app/src/main/java/.../presentation/screens/` | [PlansScreen.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/screens/plans/PlansScreen.kt) |
| **Reutilizar botones, inputs, loaders** | `app/src/main/java/.../presentation/components/` | `AppButton.kt`, `AppTextField.kt`, `ItemCard.kt` |
| **Configurar rutas de navegación** | `app/src/main/java/.../presentation/navigation/` | [Screen.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/navigation/Screen.kt) y [AppNavHost.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/navigation/AppNavHost.kt) |
| **Ejecutar pruebas unitarias** | `app/src/test/java/com/example/preparcialcomponentes/` | `PlansViewModelTest.kt` y `SavingPlanMapperTest.kt` |

---

## 2. Respuestas Prácticas para el Examen

### ❓ Si me piden crear una nueva entidad, ¿a qué carpeta voy?
**Respuesta:** Vas a `domain/model/`. Creas una data class limpia con los atributos del enunciado (ej. `Product.kt`).

### ❓ Si me piden conectar con un endpoint GET, ¿dónde trabajo?
**Respuesta:** Trabajas en 4 carpetas:
1. `data/remote/dto/` $\rightarrow$ creas el DTO de respuesta.
2. `data/mapper/` $\rightarrow$ creas la función `.toDomain()`.
3. `data/remote/api/` $\rightarrow$ agregas `@GET("endpoint") suspend fun get...()`.
4. `domain/repository/` y `data/repository/` $\rightarrow$ agregas el método y lo envuelves en `safeApiCall`.

### ❓ Si me piden enviar un formulario POST, ¿qué carpetas y archivos modifico?
**Respuesta:**
1. `data/remote/dto/` $\rightarrow$ creas el `RequestDto` (sin `id`).
2. `data/remote/api/` $\rightarrow$ agregas `@POST("endpoint") suspend fun create...(@Body request)`.
3. `presentation/screens/<pantalla>/` $\rightarrow$ agregas la función en el ViewModel con validación local de campos y construyes la pantalla Compose usando `AppTextField` y `AppButton`.

### ❓ Si me piden una pantalla de detalle, ¿dónde trabajo?
**Respuesta:** Creas la carpeta `presentation/screens/detail/` con `DetailViewModel.kt` y `DetailScreen.kt`, usando el método `getRepositoryById(id)`.

### ❓ Si me piden pasar un ID entre pantallas, ¿qué archivos modifico?
**Respuesta:** Modificas:
1. `presentation/navigation/Screen.kt` $\rightarrow$ agregas `object Detail : Screen("detail/{id}")`.
2. `presentation/navigation/AppNavHost.kt` $\rightarrow$ lees el argumento con `backStackEntry.arguments?.getInt("id")`.

### ❓ Si me piden mostrar un mensaje de error, ¿qué componentes puedo reutilizar?
**Respuesta:** Reutilizas `ErrorMessage(message, onRetry = ...)` ubicado en `presentation/components/ErrorMessage.kt`.

### ❓ Si me piden validar un campo, ¿dónde debería estar esa lógica?
**Respuesta:** En el **ViewModel** de la pantalla (`presentation/screens/<pantalla>/<Nombre>ViewModel.kt`), **antes** de lanzar la corrutina de red.

### ❓ Si me piden realizar una prueba unitaria, ¿dónde la creo?
**Respuesta:** En la carpeta `app/src/test/java/com/example/preparcialcomponentes/`, creando una clase terminada en `Test` (ej. `ProductMapperTest.kt` o `ProductsViewModelTest.kt`).

---

## 3. Ejemplo Obligatorio de Adaptación Paso a Paso (`SavingPlan` $\rightarrow$ `Product`)

A continuación se detalla la transformación completa paso a paso usando como ejemplo cambiar el sistema de **Planes de Ahorro** a un sistema de **Productos (Inventario)**.

---

### Paso 3.1 — Modelo de Dominio
* **Ruta original:** `app/src/main/java/.../domain/model/SavingPlan.kt`
* **Ruta nueva:** `app/src/main/java/.../domain/model/Product.kt`

#### A. Qué se cambia:
Se renombran los atributos del plan de ahorro (`targetAmount`, `currentAmount`) por los atributos del producto (`price`, `stock`).
#### B. Por qué se cambia:
Porque la UI y el ViewModel necesitan trabajar con la estructura conceptual del producto.
#### C. Qué se conserva:
El campo `id: Int` y la palabra clave `data class`.
#### D. Clases que dependen de este cambio:
`ProductDto.kt`, `ProductMapper.kt`, `ProductRepository.kt`, `ProductsViewModel.kt`, `ProductsScreen.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (SavingPlan.kt)
data class SavingPlan(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val description: String = ""
)

// DESPUÉS (Product.kt)
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int = 0
)
```

---

### Paso 3.2 — DTO (Data Transfer Object)
* **Ruta original:** `app/src/main/java/.../data/remote/dto/SavingPlanDto.kt`
* **Ruta nueva:** `app/src/main/java/.../data/remote/dto/ProductDto.kt`

#### A. Qué se cambia:
Las anotaciones `@SerializedName` para coincidir con el JSON del backend (`"nombre"`, `"precio_unitario"`, `"stock"`).
#### B. Por qué se cambia:
Para que Gson pueda deserializar el JSON que envía el servidor Python sin importar el nombre de las llaves.
#### C. Qué se conserva:
La nulabilidad de los tipos (`Int?`, `String?`, `Double?`) para prevenir cierres de app si falta un dato.
#### D. Clases que dependen de este cambio:
`ProductMapper.kt` y `ProductApi.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (SavingPlanDto.kt)
data class SavingPlanDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("target_amount") val targetAmount: Double? = null
)

// DESPUÉS (ProductDto.kt)
data class ProductDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nombre") val name: String? = null,
    @SerializedName("precio_unitario") val price: Double? = null,
    @SerializedName("stock") val stock: Int? = null
)
```

---

### Paso 3.3 — Mapper
* **Ruta original:** `app/src/main/java/.../data/mapper/SavingPlanMapper.kt`
* **Ruta nueva:** `app/src/main/java/.../data/mapper/ProductMapper.kt`

#### A. Qué se cambia:
El cuerpo de la función de extensión `ProductDto.toDomain()`.
#### B. Por qué se cambia:
Para transformar los campos del DTO en una instancia válida de `Product`, asignando valores por defecto ante nulos.
#### C. Qué se conserva:
Los operadores elvis (`?: 0`, `.orEmpty()`, `?: 0.0`) y el patrón `.map { it.toDomain() }` para listas.
#### D. Clases que dependen de este cambio:
`ProductRepositoryImpl.kt` y `ProductMapperTest.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (SavingPlanMapper.kt)
fun SavingPlanDto.toDomain(): SavingPlan = SavingPlan(
    id = this.id ?: 0,
    name = this.name.orEmpty(),
    targetAmount = this.targetAmount ?: 0.0
)

// DESPUÉS (ProductMapper.kt)
fun ProductDto.toDomain(): Product = Product(
    id = this.id ?: 0,
    name = this.name.orEmpty(),
    price = this.price ?: 0.0,
    stock = this.stock ?: 0
)
fun List<ProductDto>.toDomain(): List<Product> = this.map { it.toDomain() }
```

---

### Paso 3.4 — Interfaz de API Retrofit
* **Ruta original:** `app/src/main/java/.../data/remote/api/SavingPlanApi.kt`
* **Ruta nueva:** `app/src/main/java/.../data/remote/api/ProductApi.kt`

#### A. Qué se cambia:
La ruta en `@GET("products")` y el tipo de retorno `List<ProductDto>`.
#### B. Por qué se cambia:
Para consultar la colección correcta en el servidor REST.
#### C. Qué se conserva:
La anotación `@GET` y la palabra clave `suspend`.
#### D. Clases que dependen de este cambio:
`ProductRepositoryImpl.kt` y `AppViewModelFactory.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (SavingPlanApi.kt)
interface SavingPlanApi {
    @GET("plans")
    suspend fun getSavingPlans(): List<SavingPlanDto>
}

// DESPUÉS (ProductApi.kt)
interface ProductApi {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}
```

---

### Paso 3.5 — Contrato de Repositorio
* **Ruta original:** `app/src/main/java/.../domain/repository/SavingPlanRepository.kt`
* **Ruta nueva:** `app/src/main/java/.../domain/repository/ProductRepository.kt`

#### A. Qué se cambia:
Nombre de la interfaz y nombre del método `getProducts()`.
#### B. Por qué se cambia:
Para expresar la intención de negocio limpia.
#### C. Qué se conserva:
El retorno envuelto en `ApiResult<List<Product>>`.
#### D. Clases que dependen de este cambio:
`ProductRepositoryImpl.kt`, `FakeProductRepository.kt`, `ProductsViewModel.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (SavingPlanRepository.kt)
interface SavingPlanRepository {
    suspend fun getSavingPlans(): ApiResult<List<SavingPlan>>
}

// DESPUÉS (ProductRepository.kt)
interface ProductRepository {
    suspend fun getProducts(): ApiResult<List<Product>>
}
```

---

### Paso 3.6 — Implementación de Repositorio (Real y Fake)
* **Ruta original:** `app/src/main/java/.../data/repository/SavingPlanRepositoryImpl.kt`
* **Ruta nueva:** `app/src/main/java/.../data/repository/ProductRepositoryImpl.kt`

#### A. Qué se cambia:
Se llama a `api.getProducts().toDomain()` dentro de `safeApiCall`.
#### B. Por qué se cambia:
Para ejecutar la petición Retrofit adecuada y mapear a productos.
#### C. Qué se conserva:
El envoltorio `safeApiCall { ... }`.
#### D. Clases que dependen de este cambio:
`AppViewModelFactory.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (SavingPlanRepositoryImpl.kt)
class SavingPlanRepositoryImpl(private val api: SavingPlanApi) : SavingPlanRepository {
    override suspend fun getSavingPlans(): ApiResult<List<SavingPlan>> {
        return safeApiCall { api.getSavingPlans().toDomain() }
    }
}

// DESPUÉS (ProductRepositoryImpl.kt)
class ProductRepositoryImpl(private val api: ProductApi) : ProductRepository {
    override suspend fun getProducts(): ApiResult<List<Product>> {
        return safeApiCall { api.getProducts().toDomain() }
    }
}
```

---

### Paso 3.7 — ViewModel
* **Ruta original:** `app/src/main/java/.../presentation/screens/plans/PlansViewModel.kt`
* **Ruta nueva:** `app/src/main/java/.../presentation/screens/products/ProductsViewModel.kt`

#### A. Qué se cambia:
El tipo genérico del StateFlow `UiState<List<Product>>` y la función `loadProducts()`.
#### B. Por me se cambia:
Para gestionar los 4 estados de la lista de productos.
#### C. Qué se conserva:
`viewModelScope.launch`, `UiState.Loading`, `UiState.Empty`, `UiState.Success`, `UiState.Error`.
#### D. Clases que dependen de este cambio:
`ProductsScreen.kt`, `AppViewModelFactory.kt`, `ProductsViewModelTest.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (PlansViewModel.kt)
class PlansViewModel(private val repository: SavingPlanRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<SavingPlan>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SavingPlan>>> = _uiState.asStateFlow()

    init { loadPlans() }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getSavingPlans()) {
                is ApiResult.Success -> _uiState.value = if (result.data.isEmpty()) UiState.Empty else UiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = UiState.Error(result.message)
            }
        }
    }
}

// DESPUÉS (ProductsViewModel.kt)
class ProductsViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Product>>> = _uiState.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getProducts()) {
                is ApiResult.Success -> _uiState.value = if (result.data.isEmpty()) UiState.Empty else UiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = UiState.Error(result.message)
            }
        }
    }
}
```

---

### Paso 3.8 — Pantalla Compose
* **Ruta original:** `app/src/main/java/.../presentation/screens/plans/PlansScreen.kt`
* **Ruta nueva:** `app/src/main/java/.../presentation/screens/products/ProductsScreen.kt`

#### A. Qué se cambia:
Los textos dentro de `ItemCard` para mostrar el precio y stock del producto.
#### B. Por qué se cambia:
Para reflejar visualmente los atributos de la nueva entidad.
#### C. Qué se conserva:
El `Scaffold`, `TopAppBar`, el bloque `when (state)` con `LoadingIndicator`, `EmptyState`, `ErrorMessage` y la `LazyColumn`.
#### D. Clases que dependen de este cambio:
`AppNavHost.kt`.
#### E. Código Antes y Después:
```kotlin
// ANTES (PlansScreen.kt - fragmento dentro de LazyColumn)
ItemCard(onClick = { onPlanClick(plan) }) {
    Text(text = plan.name, style = MaterialTheme.typography.titleMedium)
    Text(text = "Meta: ${plan.targetAmount}")
}

// DESPUÉS (ProductsScreen.kt - fragmento dentro de LazyColumn)
ItemCard(onClick = { onProductClick(product) }) {
    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
    Text(text = "Precio: $ ${product.price}", style = MaterialTheme.typography.bodyMedium)
    Text(text = "Stock: ${product.stock} unidades", style = MaterialTheme.typography.bodySmall)
}
```

---

## 4. Método de Verificación Definitivo

Una vez realizada cualquier modificación, ejecuta los siguientes 2 pasos en la terminal de Android Studio para asegurar que todo quedó correcto:

1. **Ejecutar Pruebas Unitarias:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
   Si el comando imprime `BUILD SUCCESSFUL`, tus mappers y ViewModels funcionan sin errores de compilación ni fallos lógicos en la JVM.

2. **Compilar el APK de la App:**
   ```bash
   ./gradlew assembleDebug
   ```
   Si imprime `BUILD SUCCESSFUL`, el proyecto no tiene errores de sintaxis, importaciones faltantes ni tipos incompatibles.
