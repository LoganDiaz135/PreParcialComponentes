package com.example.preparcialcomponentes.data.mapper

import com.example.preparcialcomponentes.data.remote.dto.SavingPlanDto
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Prueba unitaria pura (JUnit) para verificar el mapeo de DTOs JSON a modelos de dominio,
 * incluyendo protección ante campos nulos enviados por el servidor.
 */
class SavingPlanMapperTest {

    @Test
    fun `toDomain mapea correctamente un SavingPlanDto completo`() {
        val dto = SavingPlanDto(
            id = 10,
            name = "Fondo Emergencia",
            targetAmount = 2000000.0,
            currentAmount = 500000.0,
            description = "Meta anual"
        )

        val domain = dto.toDomain()

        assertEquals(10, domain.id)
        assertEquals("Fondo Emergencia", domain.name)
        assertEquals(2000000.0, domain.targetAmount, 0.001)
        assertEquals(500000.0, domain.currentAmount, 0.001)
        assertEquals("Meta anual", domain.description)
    }

    @Test
    fun `toDomain aplica valores por defecto cuando llegan campos nulos`() {
        val dto = SavingPlanDto(
            id = null,
            name = null,
            targetAmount = null,
            currentAmount = null,
            description = null
        )

        val domain = dto.toDomain()

        assertEquals(0, domain.id)
        assertEquals("", domain.name)
        assertEquals(0.0, domain.targetAmount, 0.001)
        assertEquals(0.0, domain.currentAmount, 0.001)
        assertEquals("", domain.description)
    }

    @Test
    fun `toDomain mapea una lista de SavingPlanDto conservando el orden`() {
        val dtoList = listOf(
            SavingPlanDto(id = 1, name = "Plan A", targetAmount = 100.0),
            SavingPlanDto(id = 2, name = "Plan B", targetAmount = 200.0)
        )

        val domainList = dtoList.toDomain()

        assertEquals(2, domainList.size)
        assertEquals("Plan A", domainList[0].name)
        assertEquals("Plan B", domainList[1].name)
    }
}
