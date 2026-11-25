# GLOSARIO PRÁCTICO DE CARPETAS Y ARCHIVOS — PreParcialComponentes

Este glosario explica en lenguaje sencillo y amigable la estructura **REAL** del proyecto en Android Studio. Está diseñado para ayudarte a ubicarte sin perderte entre carpetas durante la preparación y el día del examen.

---

## 1. Estructura de Carpetas del Proyecto (De la Raíz a los Detalles)

### 📁 `app/`
* **Significado en español:** Aplicación.
* **Qué contiene:** Todo el código fuente, recursos visuales, imágenes, diseños Compose, pruebas unitarias y archivos de configuración del módulo principal de Android.
* **Para qué existe:** Android Studio permite tener múltiples módulos, pero `app` es el contenedor principal de nuestra aplicación.
* **Archivos que guarda:** `build.gradle.kts` (del módulo), carpeta `src/`.
* **Cuándo entrar en el parcial:** Raras veces directamente a la carpeta raíz; casi todo el trabajo se hace dentro de `app/src/`.

---

### 📁 `app/src/`
* **Significado en español:** Código fuente (abreviatura de *Source*).
* **Qué contiene:** Las dos divisiones principales del proyecto: `main` (código de la app) y `test` (pruebas unitarias).
* **Para qué existe:** Separa el código de producción que se ejecuta en el teléfono o emulador de las pruebas que corren en el computador.
* **Cuándo entrar en el parcial:** Es la puerta de entrada a todo el código.

---

### 📁 `app/src/main/`
* **Significado en español:** Principal.
* **Qué contiene:** El código que hace funcionar la aplicación: la carpeta `java/` (o kotlin), la carpeta de recursos `res/` (imágenes, textos `strings.xml`) y el manifiesto `AndroidManifest.xml`.
* **Para qué existe:** Es el núcleo del proyecto Android.
* **Cuándo entrar en el parcial:** Siempre que necesites programar una pantalla, una API o cambiar recursos visuales.

---

### 📁 `app/src/main/java/com/example/preparcialcomponentes/`
* **Significado en español:** Paquete principal del código Kotlin.
* **Qué contiene:** Todas las capas del proyecto organizadas en: `core`, `domain`, `data` y `presentation`, más la actividad principal `MainActivity.kt`.
* **Para qué existe:** Mantiene el código ordenado siguiendo la arquitectura del proyecto (Clean Architecture + MVVM).
* **Cuándo entrar en el parcial:** Aquí pasarás el 90% del tiempo de desarrollo durante el examen.

---

### 📁 `core/`
* **Significado en español:** Núcleo o centro.
* **Qué contiene:** Código transversal y utilidades reutilizables por toda la aplicación, sin importar de qué trate la entidad (Planes, Productos o Estudiantes).
* **Subcarpetas que contiene:** `common/` y `network/`.
* **Para qué existe:** Evita duplicar código de red, llamadas de seguridad y manejo de estados.
* **Ejemplos reales:** `UiState.kt`, `ApiResult.kt`, `NetworkConfig.kt`, `RetrofitClient.kt`.
* **Cuándo entrar en el parcial:** Únicamente si el profesor te indica cambiar la IP o puerto del servidor (`NetworkConfig.kt`).

---

### 📁 `core/common/`
* **Significado en español:** Núcleo común.
* **Qué contiene:** Clases genéricas para gestionar estados de interfaz y envoltorios de respuesta.
* **Archivos reales:** `UiState.kt` (Loading, Empty, Success, Error) y `ApiResult.kt` (`safeApiCall`).
* **Cuándo entrar en el parcial:** No requiere modificaciones; se usa para consultar cómo funcionan los estados `UiState`.

---

### 📁 `core/network/`
* **Significado en español:** Núcleo de red.
* **Qué contiene:** La configuración HTTP con Retrofit, OkHttp y Gson.
* **Archivos reales:** `NetworkConfig.kt` (IP y puerto `http://10.0.2.2:8000/`) y `RetrofitClient.kt` (Singleton HTTP).
* **Cuándo entrar en el parcial:** Si el servidor Python se ejecuta en un puerto diferente (ej. 5000 o 5050), abres `NetworkConfig.kt` para ajustar la URL.

---

### 📁 `domain/`
* **Significado en español:** Dominio o Núcleo del Negocio.
* **Qué contiene:** Reglas e información pura de la aplicación en lenguaje de negocio. No sabe nada sobre JSON, Gson, Retrofit ni pantallas visuales.
* **Subcarpetas que contiene:** `model/` y `repository/`.
* **Para qué existe:** Protege el núcleo del proyecto. Si el backend cambia de Python a Java o de SQLite a Postgres, las reglas de dominio no sufren cambios.
* **Cuándo entrar en el parcial:** Al inicio del examen para definir la entidad principal del enunciado.

---

### 📁 `domain/model/`
* **Significado en español:** Modelos de Dominio.
* **Qué contiene:** Clases Kotlin puras (`data class`) que representan los conceptos principales del examen.
* **Archivos reales:** `SavingPlan.kt` (contiene `SavingPlan`, `Member`, `PaymentRecord`).
* **Cuándo entrar en el parcial:** Cuando crees o modifiques la entidad del examen (ej. `Product.kt`, `Student.kt`, `Project.kt`).

---

### 📁 `domain/repository/`
* **Significado en español:** Contratos de Repositorio.
* **Qué contiene:** Interfaces Kotlin que definen **QUÉ** operaciones de datos existen (ej. `getSavingPlans()`), retornando siempre `ApiResult<T>`.
* **Archivos reales:** `SavingPlanRepository.kt`.
* **Cuándo entrar en el parcial:** Para declarar los métodos que la aplicación necesitará invocar.

---

### 📁 `data/`
* **Significado en español:** Capa de Datos e Infraestructura.
* **Qué contiene:** El código encargado de comunicarse con el exterior: peticiones HTTP REST, DTOs con anotaciones JSON, mapeadores e implementaciones de repositorio.
* **Subcarpetas que contiene:** `remote/`, `mapper/` y `repository/`.
* **Para qué existe:** Resuelve la parte técnica de cómo traer o enviar información por red.
* **Cuándo entrar en el parcial:** Cuando crees el DTO (`ProductDto.kt`), el mapper (`ProductMapper.kt`), la API (`ProductApi.kt`) o el repositorio real/fake.

---

### 📁 `data/remote/`
* **Significado en español:** Datos Remotos.
* **Qué contiene:** Las comunicaciones con el servidor web.
* **Subcarpetas que contiene:** `dto/` y `api/`.

---

### 📁 `data/remote/dto/`
* **Significado en español:** Objetos de Transferencia de Datos (Data Transfer Objects).
* **Qué contiene:** Clases Kotlin que representan exactamente el formato del JSON que envía o recibe el servidor.
* **Archivos reales:** `SavingPlanDto.kt` (usa `@SerializedName("target_amount")` y tipos nulables `Double?`).
* **Cuándo entrar en el parcial:** Cuando leas la estructura del JSON del profesor y crees los DTOs de respuesta o solicitud (`ProductDto`, `ProductRequestDto`).

---

### 📁 `data/remote/api/`
* **Significado en español:** Interfaces de API REST.
* **Qué contiene:** Interfaces Retrofit anotadas con `@GET`, `@POST`, `@Path`, `@Body`.
* **Archivos reales:** `SavingPlanApi.kt`.
* **Cuándo entrar en el parcial:** Para agregar las rutas HTTP del examen (ej. `@GET("products")`, `@POST("products")`).

---

### 📁 `data/mapper/`
* **Significado en español:** Mapeadores / Transformadores.
* **Qué contiene:** Extensiones Kotlin `.toDomain()` que convierten objetos `Dto` (con posibles campos nulos) en objetos `Model` de dominio seguros.
* **Archivos reales:** `SavingPlanMapper.kt`.
* **Cuándo entrar en el parcial:** Cuando crees las funciones de transformación para los nuevos DTOs del examen.

---

### 📁 `data/repository/`
* **Significado en español:** Implementaciones de Repositorio.
* **Qué contiene:** La lógica concreta que ejecuta las llamadas HTTP o simula datos en memoria.
* **Archivos reales:** `SavingPlanRepositoryImpl.kt` (usa Retrofit + `safeApiCall`) y `FakeSavingPlanRepository.kt` (simulación en memoria).
* **Cuándo entrar en el parcial:** Para implementar la obtención real de datos o crear datos simulados en memoria sin servidor.

---

### 📁 `presentation/`
* **Significado en español:** Capa de Presentación o Interfaz de Usuario (UI).
* **Qué contiene:** Todo lo que el usuario ve y toca en la pantalla, junto con el control del estado y la navegación.
* **Subcarpetas que contiene:** `components/`, `navigation/`, `screens/` y la fábrica `AppViewModelFactory.kt`.
* **Para qué existe:** Conecta las pantallas Compose con los ViewModels y maneja la navegación.
* **Cuándo entrar en el parcial:** Para construir pantallas, registrar ViewModels en la Factory y configurar rutas de navegación.

---

### 📁 `presentation/components/`
* **Significado en español:** Componentes Reutilizables de UI.
* **Qué contiene:** Pequeños bloques visuales genéricos que no dependen de ninguna entidad específica.
* **Archivos reales:** `AppButton.kt`, `AppTextField.kt`, `LoadingIndicator.kt`, `ErrorMessage.kt`, `EmptyState.kt`, `ItemCard.kt`.
* **Cuándo entrar en el parcial:** Para reutilizarlos dentro de las pantallas sin volver a diseñarlos desde cero.

---

### 📁 `presentation/navigation/`
* **Significado en español:** Navegación entre Pantallas.
* **Qué contiene:** La definición de las rutas y el grafo de navegación de la aplicación.
* **Archivos reales:** `Screen.kt` (nombres de rutas) y `AppNavHost.kt` (vínculo entre rutas y pantallas Compose).
* **Cuándo entrar en el parcial:** Al agregar una pantalla nueva o configurar navegación con parámetros (`detail/{id}`).

---

### 📁 `presentation/screens/`
* **Significado en español:** Pantallas de la Aplicación.
* **Qué contiene:** Carpetas por cada pantalla principal del proyecto.
* **Subcarpetas reales:** `home/` (`HomeScreen.kt`, `HomeViewModel.kt`) y `plans/` (`PlansScreen.kt`, `PlansViewModel.kt`).
* **Cuándo entrar en el parcial:** Para crear la lógica de presentación y el diseño de la pantalla del examen (ej. `products/ProductsScreen.kt`, `products/ProductsViewModel.kt`).

---

### 📁 `app/src/test/java/com/example/preparcialcomponentes/`
* **Significado en español:** Pruebas Unitarias Locales.
* **Qué contiene:** Pruebas automáticas que se ejecutan en la máquina (JVM) sin necesidad de un emulador ni celular.
* **Archivos reales:** `SavingPlanMapperTest.kt`, `PlansViewModelTest.kt`, `HomeViewModelTest.kt`, `util/MainDispatcherRule.kt`.
* **Cuándo entrar en el parcial:** Para comprobar que los mappers o los ViewModels funcionan correctamente ejecutando `./gradlew testDebugUnitTest`.

---

## 2. Diferencias entre Términos que Suelen Confundirse

Para estudiar y responder con claridad durante la sustentación:

| Términos a comparar | Diferencia Principal | ¿En qué se parecen? | Ejemplo en el proyecto |
|---|---|---|---|
| **`data` vs `domain`** | `domain` contiene las reglas puras del negocio (limpias). `data` contiene la parte técnica (HTTP, JSON, Retrofit). | Ambas son capas del backend interno de la app. | `domain`: `SavingPlan.kt`<br>`data`: `SavingPlanDto.kt` |
| **`model` vs `dto`** | `model` es la clase limpia usada por las pantallas (`val targetAmount: Double`). `dto` es la clase que refleja el JSON del servidor con `@SerializedName("target_amount")`. | Ambos son contenedores de datos (`data class`). | `model`: `domain/model/SavingPlan.kt`<br>`dto`: `data/remote/dto/SavingPlanDto.kt` |
| **`repository` vs `api`** | `api` es la interfaz de Retrofit que habla HTTP. `repository` es la interfaz que el ViewModel usa, envolviendo las llamadas en `ApiResult`. | Ambos definen métodos de consulta o envío. | `api`: `SavingPlanApi.kt`<br>`repository`: `SavingPlanRepository.kt` |
| **`screen` vs `viewmodel`** | `screen` es la función `@Composable` que dibuja la UI. `viewmodel` es la clase que mantiene el estado `StateFlow` y llama al repositorio. | Ambos pertenecen a la capa de presentación (`presentation`). | `screen`: `PlansScreen.kt`<br>`viewmodel`: `PlansViewModel.kt` |
| **`common` vs `components`** | `common` guarda clases de lógica transversal (`UiState`, `ApiResult`). `components` guarda widgets visuales Compose (`AppButton`, `ItemCard`). | Ambos son componentes 100% reutilizables en cualquier proyecto. | `common`: `core/common/UiState.kt`<br>`components`: `presentation/components/AppButton.kt` |
