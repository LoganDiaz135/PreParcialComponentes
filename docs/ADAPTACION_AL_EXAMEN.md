# PLAYBOOK OPERATIVO: ADAPTACIÓN EN 15 MINUTOS AL PARCIAL

Esta guía es el **manual de supervivencia cronometrado** para adaptar la arquitectura del proyecto a cualquier enunciado desconocido (*Productos*, *Estudiantes*, *Proyectos*, *Ventas*, *Inventario*) durante el examen.

---

## 1. Cronograma de Transformación (15 Minutos)

```text
[00:00 - 03:00]  Paso 1: Lectura del enunciado y mapeo de entidades en papel.
[03:00 - 05:00]  Paso 2: Ajuste de URL base y puerto (NetworkConfig.kt).
[05:00 - 07:00]  Paso 3: Creación de Modelos de Dominio (domain/model/).
[07:00 - 09:00]  Paso 4: Creación de DTOs y Mappers (data/remote/dto/ y data/mapper/).
[09:00 - 11:00]  Paso 5: Definición de endpoints Retrofit y Repositorio (data/remote/api/ y repository/).
[11:00 - 13:00]  Paso 6: Registro en AppViewModelFactory y ViewModels (presentation/).
[13:00 - 15:00]  Paso 7: Rutas en Screen.kt + AppNavHost.kt y comprobación con Gradle.
```

---

## 2. Checklist Paso a Paso por Archivos

### Paso 1: Análisis del Enunciado (3 min)
Identifica en la hoja del examen:
1. Nombre del recurso (ej. `products`, `students`, `projects`).
2. Operación requerida: ¿Listar (`GET`), Registrar (`POST`), Ver detalle (`GET/{id}`)?
3. Nombres de claves JSON del backend (ej. `unit_price`, `stock`, `student_code`).

---

### Paso 2: Red ([`NetworkConfig.kt`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/core/network/NetworkConfig.kt)) (2 min)
* Abre `core/network/NetworkConfig.kt`.
* Ajusta `BASE_URL` si el servidor Python corre en un puerto diferente a `:8000`:
  ```kotlin
  const val BASE_URL = "http://10.0.2.2:5000/" // Cambiar puerto si aplica
  ```

---

### Paso 3: Modelo de Dominio ([`domain/model/`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/domain/model/)) (2 min)
* Crea la Data Class limpia sin anotaciones de Gson:
  ```kotlin
  // domain/model/Product.kt
  data class Product(
      val id: Int,
      val name: String,
      val price: Double,
      val stock: Int = 0
  )
  ```

---

### Paso 4: DTOs y Mappers ([`data/remote/dto/`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/remote/dto/) y [`data/mapper/`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/mapper/)) (2 min)
1. **DTO de respuesta:**
   ```kotlin
   // data/remote/dto/ProductDto.kt
   data class ProductDto(
       @SerializedName("id") val id: Int? = null,
       @SerializedName("nombre") val name: String? = null,
       @SerializedName("precio") val price: Double? = null,
       @SerializedName("stock") val stock: Int? = null
   )
   ```
2. **Mapper:**
   ```kotlin
   // data/mapper/ProductMapper.kt
   fun ProductDto.toDomain(): Product = Product(
       id = this.id ?: 0,
       name = this.name.orEmpty(),
       price = this.price ?: 0.0,
       stock = this.stock ?: 0
   )

   fun List<ProductDto>.toDomain(): List<Product> = this.map { it.toDomain() }
   ```

---

### Paso 5: API y Repositorio ([`data/remote/api/`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/remote/api/) y [`repository/`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/data/repository/)) (2 min)
1. **API Retrofit:**
   ```kotlin
   interface ProductApi {
       @GET("products")
       suspend fun getProducts(): List<ProductDto>
   }
   ```
2. **Repositorio (Interfaz + Fake):**
   ```kotlin
   interface ProductRepository {
       suspend fun getProducts(): ApiResult<List<Product>>
   }

   class FakeProductRepository(
       private val products: List<Product> = listOf(
           Product(1, "Laptop", 2500000.0, 10),
           Product(2, "Mouse", 45000.0, 25)
       )
   ) : ProductRepository {
       override suspend fun getProducts(): ApiResult<List<Product>> = ApiResult.Success(products)
   }
   ```

---

### Paso 6: Registrar en Factory y ViewModel (2 min)
1. **[`AppViewModelFactory.kt`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/AppViewModelFactory.kt):**
   ```kotlin
   private val productRepository: ProductRepository by lazy {
       if (useFakeData) FakeProductRepository()
       else ProductRepositoryImpl(RetrofitClient.createService(ProductApi::class.java))
   }
   // En create():
   modelClass.isAssignableFrom(ProductsViewModel::class.java) -> ProductsViewModel(productRepository) as T
   ```
2. **`ProductsViewModel.kt`:** Copia el patrón de [PlansViewModel.kt](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/screens/plans/PlansViewModel.kt) cambiando `SavingPlan` por `Product`.

---

### Paso 7: Rutas y Verificación (2 min)
1. **[`Screen.kt`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/navigation/Screen.kt):** Añade `object Products : Screen("products")`.
2. **[`AppNavHost.kt`](file:///C:/Users/logan/AndroidStudioProjects/pre_parcial/app/src/main/java/com/example/preparcialcomponentes/presentation/navigation/AppNavHost.kt):** Añade `composable(Screen.Products.route) { ProductsScreen(...) }`.
3. **Verificación rápida con Gradle:**
   Lanza `./gradlew testDebugUnitTest` en la terminal de Android Studio.

---

## 3. Matriz Rápidas de Adaptación por Escenarios

| Elemento | Escenario A: **Productos / Inventario** | Escenario B: **Estudiantes / Materias** | Escenario C: **Proyectos / Investigadores** |
|---|---|---|---|
| **Dominio** | `Product(id, name, price, stock)` | `Student(id, name, code, gpa)` | `Project(id, title, budget, status)` |
| **DTO** | `ProductDto` (`@SerializedName("precio")`) | `StudentDto` (`@SerializedName("codigo")`) | `ProjectDto` (`@SerializedName("presupuesto")`) |
| **Endpoint** | `@GET("products")` | `@GET("students")` | `@GET("projects")` |
| **Request DTO** | `ProductRequestDto(name, price, stock)` | `StudentRequestDto(name, code, gpa)` | `ProjectRequestDto(title, budget)` |
| **Validación** | `price > 0` y `stock >= 0` | `code.isNotBlank()` | `budget > 0` |
| **ItemCard UI** | Muestra Nombre y Precio formateado | Muestra Nombre y Código | Muestra Título y Presupuesto |

---

## 4. Qué NUNCA debes Modificar en un Parcial

* `core/common/UiState.kt`
* `core/common/ApiResult.kt`
* `core/network/RetrofitClient.kt`
* `presentation/components/` (`AppButton`, `AppTextField`, `LoadingIndicator`, `ErrorMessage`, `EmptyState`, `ItemCard`)
* `MainActivity.kt` y `MainDispatcherRule.kt`

Estas piezas son **100% universales** y te ahorran más de 45 minutos de trabajo repetitivo en el examen.
