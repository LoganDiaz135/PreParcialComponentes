package com.example.preparcialcomponentes.data.repository

import com.example.preparcialcomponentes.core.common.ApiResult
import com.example.preparcialcomponentes.domain.model.Member
import com.example.preparcialcomponentes.domain.model.PaymentRecord
import com.example.preparcialcomponentes.domain.model.SavingPlan
import com.example.preparcialcomponentes.domain.repository.SavingPlanRepository
import kotlinx.coroutines.delay

/**
 * Implementación SIMULADA EN MEMORIA de [SavingPlanRepository] para desarrollo y pruebas sin servidor.
 *
 * IMPORTANTE:
 * - Esta clase NO realiza llamadas HTTP ni se conecta a ningún backend real.
 * - Existe para desarrollar, ejecutar en el emulador y probar la UI/ViewModel mientras no
 *   se disponga del servidor Python (FastAPI + SQLite).
 * - Cuando el backend esté disponible, basta con reemplazar esta instancia por
 *   [SavingPlanRepositoryImpl] dentro de `AppViewModelFactory.kt`.
 */
class FakeSavingPlanRepository(
    private val plansToReturn: List<SavingPlan> = defaultSamplePlans(),
    var shouldReturnError: Boolean = false,
    var errorMessage: String = "Error simulado: no se pudo obtener la lista de planes.",
    private val simulatedDelayMs: Long = 500L
) : SavingPlanRepository {

    override suspend fun getSavingPlans(): ApiResult<List<SavingPlan>> {
        if (simulatedDelayMs > 0L) {
            delay(simulatedDelayMs)
        }
        return if (shouldReturnError) {
            ApiResult.Error(errorMessage)
        } else {
            ApiResult.Success(plansToReturn)
        }
    }

    override suspend fun getSavingPlanById(id: Int): ApiResult<SavingPlan> {
        val plan = plansToReturn.find { it.id == id }
        return if (plan != null) {
            ApiResult.Success(plan)
        } else {
            ApiResult.Error("Plan con id=$id no encontrado (simulación)")
        }
    }

    override suspend fun addMember(
        planId: Int,
        name: String,
        relationship: String
    ): ApiResult<Member> {
        return ApiResult.Error("Operación aún no implementada en esta fase (HU-03)")
    }

    override suspend fun registerPayment(
        planId: Int,
        memberId: Int,
        amount: Double
    ): ApiResult<PaymentRecord> {
        return ApiResult.Error("Operación aún no implementada en esta fase (HU-04)")
    }

    override suspend fun getPayments(planId: Int): ApiResult<List<PaymentRecord>> {
        return ApiResult.Error("Operación aún no implementada en esta fase (HU-05)")
    }

    companion object {
        fun defaultSamplePlans(): List<SavingPlan> = listOf(
            SavingPlan(
                id = 1,
                name = "Vacaciones Familiares",
                targetAmount = 5000000.0,
                currentAmount = 1850000.0,
                description = "Ahorro grupal para viaje de fin de año (dato de prueba)"
            ),
            SavingPlan(
                id = 2,
                name = "Fondo de Emergencia",
                targetAmount = 3000000.0,
                currentAmount = 2400000.0,
                description = "Reserva médica y del hogar (dato de prueba)"
            ),
            SavingPlan(
                id = 3,
                name = "Remodelación Cocina",
                targetAmount = 8000000.0,
                currentAmount = 950000.0,
                description = "Compra de electrodomésticos y pintura (dato de prueba)"
            )
        )
    }
}
