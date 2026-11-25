package com.example.preparcialcomponentes.data.repository

import com.example.preparcialcomponentes.core.common.ApiResult
import com.example.preparcialcomponentes.core.common.safeApiCall
import com.example.preparcialcomponentes.data.mapper.toDomain
import com.example.preparcialcomponentes.data.remote.api.SavingPlanApi
import com.example.preparcialcomponentes.data.remote.dto.MemberRequestDto
import com.example.preparcialcomponentes.data.remote.dto.PaymentRequestDto
import com.example.preparcialcomponentes.domain.model.Member
import com.example.preparcialcomponentes.domain.model.PaymentRecord
import com.example.preparcialcomponentes.domain.model.SavingPlan
import com.example.preparcialcomponentes.domain.repository.SavingPlanRepository

/**
 * Implementación de producción del [SavingPlanRepository] utilizando Retrofit.
 *
 * Emplea [safeApiCall] para encapsular excepciones de red y mappers para retornar modelos de dominio.
 */
class SavingPlanRepositoryImpl(
    private val api: SavingPlanApi
) : SavingPlanRepository {

    override suspend fun getSavingPlans(): ApiResult<List<SavingPlan>> {
        return safeApiCall {
            api.getSavingPlans().toDomain()
        }
    }

    override suspend fun getSavingPlanById(id: Int): ApiResult<SavingPlan> {
        return safeApiCall {
            api.getSavingPlanById(id).toDomain()
        }
    }

    override suspend fun addMember(planId: Int, name: String, relationship: String): ApiResult<Member> {
        return safeApiCall {
            val request = MemberRequestDto(name = name, relationship = relationship)
            api.addMember(planId, request).toDomain()
        }
    }

    override suspend fun registerPayment(planId: Int, memberId: Int, amount: Double): ApiResult<PaymentRecord> {
        return safeApiCall {
            val request = PaymentRequestDto(memberId = memberId, amount = amount)
            api.registerPayment(planId, request).toDomain()
        }
    }

    override suspend fun getPayments(planId: Int): ApiResult<List<PaymentRecord>> {
        return safeApiCall {
            api.getPayments(planId).toDomain()
        }
    }
}
