package com.monevix.app.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WorkTypeValidatorTest {

    @Test
    fun codigoVacio_devuelveError() {
        assertNotNull(WorkTypeValidator.validateCode(""))
    }

    @Test
    fun codigoConEspacios_devuelveError() {
        assertNotNull(WorkTypeValidator.validateCode("   "))
    }

    @Test
    fun codigoConCaracteresInvalidos_devuelveError() {
        assertNotNull(WorkTypeValidator.validateCode("C01#"))
    }

    @Test
    fun codigoMuyLargo_devuelveError() {
        val codigoLargo = "A".repeat(WorkTypeValidator.MAX_CODE_LENGTH + 1)
        assertNotNull(WorkTypeValidator.validateCode(codigoLargo))
    }

    @Test
    fun codigoValido_noDevuelveError() {
        assertNull(WorkTypeValidator.validateCode("C01"))
    }

    @Test
    fun nombreVacio_devuelveError() {
        assertNotNull(WorkTypeValidator.validateName(""))
    }

    @Test
    fun nombreMuyCorto_devuelveError() {
        assertNotNull(WorkTypeValidator.validateName("Co"))
    }

    @Test
    fun nombreValido_noDevuelveError() {
        assertNull(WorkTypeValidator.validateName("Cocido de telas"))
    }

    @Test
    fun precioVacio_devuelveError() {
        assertNotNull(WorkTypeValidator.validateUnitPrice(""))
    }

    @Test
    fun precioNoNumerico_devuelveError() {
        assertNotNull(WorkTypeValidator.validateUnitPrice("abc"))
    }

    @Test
    fun precioCero_devuelveError() {
        assertNotNull(WorkTypeValidator.validateUnitPrice("0"))
    }

    @Test
    fun precioNegativo_devuelveError() {
        assertNotNull(WorkTypeValidator.validateUnitPrice("-100"))
    }

    @Test
    fun precioDemasiadoAlto_devuelveError() {
        val precioAlto = (WorkTypeValidator.MAX_UNIT_PRICE + 1).toString()
        assertNotNull(WorkTypeValidator.validateUnitPrice(precioAlto))
    }

    @Test
    fun precioValido_noDevuelveError() {
        assertNull(WorkTypeValidator.validateUnitPrice("4000"))
    }
}
