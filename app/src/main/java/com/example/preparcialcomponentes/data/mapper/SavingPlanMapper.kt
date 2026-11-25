package com.example.preparcialcomponentes.data.mapper

import com.example.preparcialcomponentes.data.remote.dto.MemberDto
import com.example.preparcialcomponentes.data.remote.dto.PaymentRecordDto
import com.example.preparcialcomponentes.data.remote.dto.SavingPlanDto
import com.example.preparcialcomponentes.domain.model.Member
import com.example.preparcialcomponentes.domain.model.PaymentRecord
import com.example.preparcialcomponentes.domain.model.SavingPlan

/**
 * Funciones de extensión de mapeo de DTO a Modelos de Dominio.
 *
 * Aplica valores por defecto si algún campo llega nulo del backend.
 */
fun SavingPlanDto.toDomain(): SavingPlan {
    return SavingPlan(
        id = this.id ?: 0,
        name = this.name.orEmpty(),
        targetAmount = this.targetAmount ?: 0.0,
        currentAmount = this.currentAmount ?: 0.0,
        description = this.description.orEmpty()
    )
}

fun List<SavingPlanDto>.toDomain(): List<SavingPlan> {
    return this.map { it.toDomain() }
}

fun MemberDto.toDomain(): Member {
    return Member(
        id = this.id ?: 0,
        planId = this.planId ?: 0,
        name = this.name.orEmpty(),
        relationship = this.relationship.orEmpty()
    )
}

fun PaymentRecordDto.toDomain(): PaymentRecord {
    return PaymentRecord(
        id = this.id ?: 0,
        planId = this.planId ?: 0,
        memberId = this.memberId ?: 0,
        amount = this.amount ?: 0.0,
        date = this.date.orEmpty()
    )
}

@JvmName("paymentRecordListToDomain")
fun List<PaymentRecordDto>.toDomain(): List<PaymentRecord> {
    return this.map { it.toDomain() }
}
