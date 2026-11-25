package com.example.preparcialcomponentes.domain.repository

import com.example.preparcialcomponentes.core.common.ApiResult
import com.example.preparcialcomponentes.domain.model.Member
import com.example.preparcialcomponentes.domain.model.PaymentRecord
import com.example.preparcialcomponentes.domain.model.SavingPlan

/**
 * Contrato de repositorio para operaciones sobre Planes de Ahorro.
 *
 * El Repository desacopla a los ViewModels del origen concreto de datos
 * (Retrofit, memoria o fakes para pruebas).
 */
interface SavingPlanRepository {
    suspend fun getSavingPlans(): ApiResult<List<SavingPlan>>
    suspend fun getSavingPlanById(id: Int): ApiResult<SavingPlan>
    suspend fun addMember(planId: Int, name: String, relationship: String): ApiResult<Member>
    suspend fun registerPayment(planId: Int, memberId: Int, amount: Double): ApiResult<PaymentRecord>
    suspend fun getPayments(planId: Int): ApiResult<List<PaymentRecord>>
}
