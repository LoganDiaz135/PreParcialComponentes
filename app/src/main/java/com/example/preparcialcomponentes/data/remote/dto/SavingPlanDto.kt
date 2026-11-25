package com.example.preparcialcomponentes.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects (DTO) para la serialización y deserialización de JSON.
 *
 * Se utiliza @SerializedName para mapear nombres de atributos en snake_case del backend Python
 * (FastAPI / SQLite) a convenciones camelCase de Kotlin.
 */
data class SavingPlanDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("target_amount")
    val targetAmount: Double? = null,

    @SerializedName("current_amount")
    val currentAmount: Double? = null,

    @SerializedName("description")
    val description: String? = null
)

data class MemberDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("plan_id")
    val planId: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("relationship")
    val relationship: String? = null
)

data class MemberRequestDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("relationship")
    val relationship: String
)

data class PaymentRecordDto(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("plan_id")
    val planId: Int? = null,

    @SerializedName("member_id")
    val memberId: Int? = null,

    @SerializedName("amount")
    val amount: Double? = null,

    @SerializedName("date")
    val date: String? = null
)

data class PaymentRequestDto(
    @SerializedName("member_id")
    val memberId: Int,

    @SerializedName("amount")
    val amount: Double
)
