package com.monevix.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class WorkerValidatorTest {

    @Test
    fun nombreVacio_devuelveError() {
        val error = WorkerValidator.validateName("")
        assertNotNull(error)
    }

    @Test
    fun nombreConSoloEspacios_devuelveError() {
        val error = WorkerValidator.validateName("    ")
        assertNotNull(error)
    }

    @Test
    fun nombreMuyCorto_devuelveError() {
        val error = WorkerValidator.validateName("Al")
        assertNotNull(error)
    }

    @Test
    fun nombreValido_noDevuelveError() {
        val error = WorkerValidator.validateName("Carlos Pérez")
        assertNull(error)
    }

    @Test
    fun nombreMuyLargo_devuelveError() {
        val nombreLargo = "A".repeat(WorkerValidator.MAX_NAME_LENGTH + 1)
        val error = WorkerValidator.validateName(nombreLargo)
        assertNotNull(error)
    }

    @Test
    fun telefonoVacio_esValido_porqueEsOpcional() {
        assertNull(WorkerValidator.validatePhone(""))
        assertNull(WorkerValidator.validatePhone(null))
    }

    @Test
    fun telefonoConLetras_devuelveError() {
        val error = WorkerValidator.validatePhone("abc123")
        assertNotNull(error)
    }

    @Test
    fun telefonoValido_noDevuelveError() {
        val error = WorkerValidator.validatePhone("+57 300 123 4567")
        assertNull(error)
    }
}
