package com.example.preparcialcomponentes.data.remote.api

import com.example.preparcialcomponentes.data.remote.dto.MemberDto
import com.example.preparcialcomponentes.data.remote.dto.MemberRequestDto
import com.example.preparcialcomponentes.data.remote.dto.PaymentRecordDto
import com.example.preparcialcomponentes.data.remote.dto.PaymentRequestDto
import com.example.preparcialcomponentes.data.remote.dto.SavingPlanDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interfaz Retrofit para interactuar con la API REST de planes de ahorro.
 *
 * Adaptable durante el examen cambiando las rutas relativas.
 */
interface SavingPlanApi {

    @GET("plans")
    suspend fun getSavingPlans(): List<SavingPlanDto>

    @GET("plans/{id}")
    suspend fun getSavingPlanById(@Path("id") id: Int): SavingPlanDto

    @POST("plans/{id}/members")
    suspend fun addMember(
        @Path("id") planId: Int,
        @Body memberRequest: MemberRequestDto
    ): MemberDto

    @POST("plans/{id}/payments")
    suspend fun registerPayment(
        @Path("id") planId: Int,
        @Body paymentRequest: PaymentRequestDto
    ): PaymentRecordDto

    @GET("plans/{id}/payments")
    suspend fun getPayments(@Path("id") planId: Int): List<PaymentRecordDto>
}
