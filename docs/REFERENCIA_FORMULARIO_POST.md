# REFERENCIA MAESTRA: FORMULARIOS Y ENVÍO DE DATOS (`POST` CON VALIDACIÓN LOCAL)

Este documento sirve como **guía de referencia directa** para resolver cualquier historia de usuario de **creación, registro o envío de datos (`POST`)** durante el examen.

---

## 1. Enunciado Típico de Examen
> **HU-POST:** Como usuario de la aplicación, quiero registrar un nuevo elemento (ej. Producto, Estudiante, Pago o Integrante) ingresando sus datos en un formulario, de modo que la aplicación valide los campos antes de enviarlos, me muestre un indicador mientras procesa la petición y notifique el resultado exitoso o el mensaje de error.

---

## 2. Objetivo Técnico y Arquitectura de un `POST`

### Flujo Completo de Datos en un Envío:
```text
[Pantalla Compose] (Escribe en AppTextField)
       │
       ▼ (Evento de clic en AppButton)
[ViewModel] -> Ejecuta validación local en Kotlin (Sin llamadas HTTP)
       │  ├─ Si los datos SON INVÁLIDOS:
       │  │    Emite error en campo / UiState.Error local (0 ms, sin consumir red).
       │  └─ Si los datos SON VÁLIDOS:
       │       1. Emite UiState.Loading
       │       2. Instancia DTO Request (sin 'id')
       │       3. Invoca repository.createEntity(request)
       ▼
[RepositoryImpl / FakeRepository]
       │  Ejecuta safeApiCall { api.createEntity(requestDto).toDomain() }
       ▼
[Retrofit + Gson]
       │  Serializa DTO Request -> JSON {"name": "...", "amount": 100.0}
       │  Envía HTTP POST -> http://10.0.2.2:8000/recurso
       ▼
[ViewModel]
       │  Si ApiResult.Success -> Emite UiState.Success(entidadCreada)
       │  Si ApiResult.Error   -> Emite UiState.Error(mensaje)
       ▼
[Pantalla Compose] -> Muestra confirmación o mensaje de error.
```

---

## 3. Plantilla de Código de Referencia

### 3.1 DTO de Solicitud (`RequestDto`)
A diferencia del DTO de respuesta, el DTO de envío **no contiene `id`** si este lo asigna la base de datos en el servidor:

```kotlin
// data/remote/dto/ProductRequestDto.kt
data class ProductRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("stock") val stock: Int
)
```

### 3.2 Endpoint Retrofit (`Api`)
```kotlin
// data/remote/api/ProductApi.kt
interface ProductApi {
    @POST("products")
    suspend fun createProduct(
        @Body request: ProductRequestDto
    ): ProductDto
}
```

### 3.3 Método en el Repository
```kotlin
// domain/repository/ProductRepository.kt
interface ProductRepository {
    suspend fun createProduct(name: String, price: Double, stock: Int): ApiResult<Product>
}

// data/repository/ProductRepositoryImpl.kt
override suspend fun createProduct(name: String, price: Double, stock: Int): ApiResult<Product> {
    return safeApiCall {
        val request = ProductRequestDto(name = name, price = price, stock = stock)
        api.createProduct(request).toDomain()
    }
}
```

### 3.4 ViewModel con Validación Local de Campos
El ViewModel valida **antes** de lanzar la corrutina de red para evitar llamadas inútiles:

```kotlin
// presentation/screens/create_product/CreateProductViewModel.kt
class CreateProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Product>?>(null) // null = estado inicial reposo
    val uiState: StateFlow<UiState<Product>?> = _uiState.asStateFlow()

    // Estado local de los campos del formulario
    var nameInput by mutableStateOf("")
        private set
    var priceInput by mutableStateOf("")
        private set
    var stockInput by mutableStateOf("")
        private set

    // Mensajes de error por campo
    var nameError by mutableStateOf<String?>(null)
        private set
    var priceError by mutableStateOf<String?>(null)
        private set

    fun onNameChange(value: String) {
        nameInput = value
        nameError = null
    }

    fun onPriceChange(value: String) {
        priceInput = value
        priceError = null
    }

    fun onStockChange(value: String) {
        stockInput = value
    }

    fun submitForm() {
        val name = nameInput.trim()
        val price = priceInput.toDoubleOrNull()
        val stock = stockInput.toIntOrNull() ?: 0

        var isValid = true

        if (name.isBlank()) {
            nameError = "El nombre del producto es obligatorio."
            isValid = false
        }

        if (price == null || price <= 0.0) {
            priceError = "El precio debe ser un número mayor a 0."
            isValid = false
        }

        if (!isValid) return // Detiene el flujo sin consumir llamadas de red

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.createProduct(name, price!!, stock)) {
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

### 3.5 Screen en Jetpack Compose
Reutiliza `AppTextField` y `AppButton` de `presentation/components/`:

```kotlin
@Composable
fun CreateProductScreen(
    viewModel: CreateProductViewModel,
    onSuccessBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registrar Producto", style = MaterialTheme.typography.titleLarge)

        AppTextField(
            value = viewModel.nameInput,
            onValueChange = viewModel::onNameChange,
            label = "Nombre del Producto",
            errorMessage = viewModel.nameError
        )

        AppTextField(
            value = viewModel.priceInput,
            onValueChange = viewModel::onPriceChange,
            label = "Precio Unitario",
            errorMessage = viewModel.priceError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        AppTextField(
            value = viewModel.stockInput,
            onValueChange = viewModel::onStockChange,
            label = "Stock Inicial",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        AppButton(
            text = "Guardar Producto",
            onClick = { viewModel.submitForm() },
            isLoading = state is UiState.Loading
        )

        when (val currentState = state) {
            is UiState.Error -> ErrorMessage(message = currentState.message)
            is UiState.Success -> {
                LaunchedEffect(Unit) {
                    onSuccessBack()
                }
            }
            else -> {}
        }
    }
}
```

---

## 4. Pruebas Unitarias para Formulario `POST`

En `CreateProductViewModelTest.kt`:

1. **Prueba de Validación Local (sin red):**
   ```kotlin
   @Test
   fun `submitForm con precio vacio no llama al repositorio y asigna error`() = runTest {
       val viewModel = CreateProductViewModel(fakeRepository)
       viewModel.onNameChange("Laptop")
       viewModel.onPriceChange("") // Inválido
       viewModel.submitForm()

       assertEquals("El precio debe ser un número mayor a 0.", viewModel.priceError)
       assertNull(viewModel.uiState.value) // No emitió Loading ni tocó la red
   }
   ```

2. **Prueba de Envío Exitoso:**
   ```kotlin
   @Test
   fun `submitForm con datos validos emite Loading y luego Success`() = runTest {
       val viewModel = CreateProductViewModel(fakeRepository)
       viewModel.onNameChange("Teclado")
       viewModel.onPriceChange("150000.0")
       viewModel.submitForm()

       advanceUntilIdle()
       assertTrue(viewModel.uiState.value is UiState.Success)
   }
   ```

---

## 5. Adaptación a Cualquier Entidad en el Examen

* **Si piden `Registrar Estudiante`:** Cambiar campos a `fullName`, `code` y `gpa`. Validar que `code` no esté vacío.
* **Si piden `Asignar Investigador a Proyecto`:** Pasar `projectId` por parámetro y enviar `MemberRequestDto(name, role)`.
* **Si piden `Registrar Venta`:** Validar `quantity > 0` y `totalAmount > 0`.
