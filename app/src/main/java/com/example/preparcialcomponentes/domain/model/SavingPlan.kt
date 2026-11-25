package com.example.preparcialcomponentes.domain.model

/**
 * Modelo de dominio que representa un Plan de Ahorro Familiar.
 *
 * Los modelos de dominio son independientes del formato JSON del backend (no usan @SerializedName).
 */
data class SavingPlan(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val description: String = ""
)

/**
 * Modelo de dominio para un Integrante del plan.
 */
data class Member(
    val id: Int,
    val planId: Int,
    val name: String,
    val relationship: String = ""
)

/**
 * Modelo de dominio para un Pago / Aporte de un integrante.
 */
data class PaymentRecord(
    val id: Int,
    val planId: Int,
    val memberId: Int,
    val amount: Double,
    val date: String = ""
)
