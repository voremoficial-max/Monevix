package com.monevix.app.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorValidatorTest {

    @Test
    fun cantidadVacia_devuelveError() {
        assertNotNull(CalculatorValidator.validateQuantity(""))
    }

    @Test
    fun cantidadNoNumerica_devuelveError() {
        assertNotNull(CalculatorValidator.validateQuantity("abc"))
    }

    @Test
    fun cantidadCero_devuelveError() {
        assertNotNull(CalculatorValidator.validateQuantity("0"))
    }

    @Test
    fun cantidadNegativa_devuelveError() {
        assertNotNull(CalculatorValidator.validateQuantity("-5"))
    }

    @Test
    fun cantidadDemasiadoAlta_devuelveError() {
        val cantidadAlta = (CalculatorValidator.MAX_QUANTITY + 1).toString()
        assertNotNull(CalculatorValidator.validateQuantity(cantidadAlta))
    }

    @Test
    fun cantidadValida_noDevuelveError() {
        assertNull(CalculatorValidator.validateQuantity("35"))
    }
}
