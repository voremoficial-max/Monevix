package com.monevix.app.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyInputFormatterTest {

    @Test
    fun format_agregaPuntosDeMiles() {
        assertEquals("4", CurrencyInputFormatter.format("4"))
        assertEquals("40", CurrencyInputFormatter.format("40"))
        assertEquals("400", CurrencyInputFormatter.format("400"))
        assertEquals("4.000", CurrencyInputFormatter.format("4000"))
        assertEquals("40.000", CurrencyInputFormatter.format("40000"))
        assertEquals("400.000", CurrencyInputFormatter.format("400000"))
        assertEquals("4.500.000", CurrencyInputFormatter.format("4500000"))
    }

    @Test
    fun toLongOrNull_ignoraPuntos() {
        assertEquals(4500000L, CurrencyInputFormatter.toLongOrNull("4.500.000"))
        assertEquals(null, CurrencyInputFormatter.toLongOrNull(""))
    }

    /**
     * Este es el caso exacto que reportó el usuario: al escribir "20009"
     * dígito por dígito, el cursor debía quedar siempre justo después del
     * último dígito escrito, sin importar los puntos de miles insertados,
     * para que el resultado final fuera "20.009" y no "20.090".
     */
    @Test
    fun reformat_mantieneElCursorDespuesDelUltimoDigitoEscrito_20009() {
        var value = TextFieldValue("")
        val secuencia = listOf("2", "0", "0", "0", "9")

        // Simula escribir dígito por dígito al final del campo, como lo haría el teclado.
        for (digito in secuencia) {
            val textoConNuevoDigito = value.text + digito
            val entrada = TextFieldValue(
                text = textoConNuevoDigito,
                selection = TextRange(textoConNuevoDigito.length)
            )
            value = CurrencyInputFormatter.reformat(entrada)
        }

        assertEquals("20.009", value.text)
        assertEquals(value.text.length, value.selection.end)
    }

    @Test
    fun reformat_insertarDigitoEnElMedio_conservaElValorYElCursorCorrecto() {
        // Texto actual formateado: "4.000" con el cursor entre el "4" y el primer "0" (posición 1).
        val actual = TextFieldValue(text = "4.000", selection = TextRange(1))

        // El usuario escribe "5" ahí: el campo pasa a "45.000" con el cursor tras el "5".
        val conNuevoDigito = TextFieldValue(text = "45.000", selection = TextRange(2))
        val resultado = CurrencyInputFormatter.reformat(conNuevoDigito)

        assertEquals("45.000", resultado.text)
        assertEquals(2, resultado.selection.end)
    }

    @Test
    fun reformat_campoVacio_devuelveCursorEnCero() {
        val resultado = CurrencyInputFormatter.reformat(TextFieldValue(text = "", selection = TextRange(0)))
        assertEquals("", resultado.text)
        assertEquals(0, resultado.selection.end)
    }
}
