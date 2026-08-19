package com.monevix.app

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Prueba unitaria básica de la Fase 1. Confirma que el entorno de pruebas
 * (JUnit + Gradle) funciona correctamente antes de agregar lógica de negocio.
 * En la Fase 4 se agregarán pruebas reales para la lógica de cálculo de pagos.
 */
class ExampleUnitTest {

    @Test
    fun suma_esCorrecta() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun calculoBasico_cantidadPorPrecio_esCorrecto() {
        // Simula el cálculo que usará la calculadora de pagos: cantidad x precio unitario.
        val cantidad = 35
        val precioUnitario = 4000.0
        val esperado = 140000.0
        assertEquals(esperado, cantidad * precioUnitario, 0.0)
    }
}
