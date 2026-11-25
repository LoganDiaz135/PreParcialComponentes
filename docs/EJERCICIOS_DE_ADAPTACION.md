# EJERCICIOS DE ADAPTACIÓN — PRÁCTICA PROGRESIVA

Estos ejercicios están diseñados para entrenar tu velocidad de adaptación **sin resolverlos automáticamente**. Úsalos después de estudiar `DICCIONARIO_DE_ARCHIVOS.md` y `ADAPTACION_AL_EXAMEN.md`.

---

## Nivel 1: Configuración y Contratos JSON (10–15 min)

### Ejercicio 1.1 — Cambio de puerto y endpoint
* **Escenario:** El profesor levanta el servidor FastAPI en el puerto `5050` y la ruta de listado ya no es `/plans`, sino `/api/v1/planes`.
* **Tu tarea:**
  1. Identifica qué dos archivos debes modificar (sin tocar `RetrofitClient.kt` ni `MainActivity.kt`).
  2. Realiza el cambio mental o en una rama de prueba y verifica que el proyecto compila.

### Ejercicio 1.2 — Nuevo atributo JSON en `snake_case`
* **Escenario:** El backend agrega el campo `"monthly_quota": 150000.0` al JSON de cada plan de ahorro.
* **Tu tarea:**
  1. Agrega `monthlyQuota` a `SavingPlanDto` con `@SerializedName("monthly_quota")`.
  2. Agrégalo a `SavingPlan` (modelo de dominio) con valor por defecto `0.0`.
  3. Actualiza `SavingPlanMapper.kt`.
  4. Agrega una aserción en `SavingPlanMapperTest.kt` (caso completo y caso `null`) y verifica con `./gradlew testDebugUnitTest`.

---

## Nivel 2: Cambio de Contexto de Entidad Simple (20–30 min)

### Ejercicio 2.1 — Pasar de `SavingPlan` a `Expense` (Gastos) o `Sale` (Ventas)
* **Escenario:** El enunciado pide una app para consultar y registrar gastos diarios (`id`, `category`, `amount`, `description`, `date`).
* **Tu tarea:**
  1. Dibuja en papel los nombres de los nuevos archivos en `domain/model/`, `data/remote/dto/`, `data/remote/api/`, `data/mapper/`, `domain/repository/` y `data/repository/`.
  2. Lista qué archivos de `core/` y `presentation/components/` **permanecen 100% intactos**.
  3. Escribe el DTO `ExpenseRequestDto` para el `POST /expenses` (pensando qué campos NO debes enviar porque los crea SQLite).
  4. Define una función de validación en Kotlin que rechace `category` vacía y `amount <= 0` o no numérico.

---

## Nivel 3: Entidades con Relación 1:N y Navegación con Parámetros (30–40 min)

### Ejercicio 3.1 — De Planes e Integrantes a **Proyectos e Investigadores**
* **Escenario:** Un proyecto (`Project`: `id`, `title`, `budget`) tiene muchos investigadores (`Researcher`: `id`, `projectId`, `name`, `specialty`).
  * HU-A: Listar proyectos (`GET /projects`).
  * HU-B: Ver detalle de un proyecto (`GET /projects/{id}`).
  * HU-C: Asignar investigador a un proyecto (`POST /projects/{id}/researchers`).
* **Tu tarea:**
  1. Diseña las rutas en `Screen.kt` (¿cuáles llevan `{projectId}` y cuáles no?).
  2. Explica cómo viaja `projectId` desde que el usuario toca una `ItemCard` en la lista hasta que llega a `@Path("id")` en Retrofit.
  3. Indica qué líneas cambiarías en `AppViewModelFactory.kt` si ahora tienes `ProjectRepository` separado o unificado.

---

## Nivel 4: Simulación de Parcial Completo (45–60 min)

### Ejercicio 4.1 — **Productos, Proveedores y Compras** (Inventario)
* **Enunciado simulado:**
  > Un almacén gestiona **Productos** (`id`, `name`, `price`, `stock`, `supplier_id`) asociados a **Proveedores** (`id`, `company_name`, `phone`). El sistema debe permitir:
  > 1. Listar productos.
  > 2. Ver detalle de un producto con datos de su proveedor.
  > 3. Crear un nuevo proveedor.
  > 4. Registrar una compra de inventario (`product_id`, `quantity`, `unit_cost`) que aumenta el stock en el servidor.
  > 5. Consultar el historial de compras de un producto.
* **Tu reto (resuelve paso a paso siguiendo `ADAPTACION_AL_EXAMEN.md`):**
  1. Define los DTOs de entrada y salida con `@SerializedName`.
  2. Define la interfaz `InventoryApi` (anotaciones `@GET`, `@POST`, `@Path`, `@Body`).
  3. Decide si usarás 1 o 2 repositorios y justifica tu decisión como en una sustentación.
  4. Define qué validaciones locales deben ejecutarse antes de enviar la compra (`quantity` entero positivo, `unit_cost` positivo).
  5. Diseña un `FakeInventoryRepository` para probar sin backend que:
     * Al registrar una compra válida, el estado pasa a `Success`.
     * Si el listado de compras viene vacío, el ViewModel emite `UiState.Empty`.
     * Si el repositorio retorna `ApiResult.Error`, el ViewModel emite `UiState.Error`.

---

## Nivel 5: Autoevaluación Oral (10 min)

Antes de cerrar tu sesión de estudio, responde en voz alta las preguntas de `SUSTENTACION.md` señalando en Android Studio el archivo exacto donde está implementado cada concepto.
