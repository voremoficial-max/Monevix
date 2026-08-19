package com.monevix.app.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Formatea mientras el usuario escribe un valor entero en pesos colombianos.
 * Ej.: 4 -> 4, 40 -> 40, 400 -> 400, 4000 -> 4.000.
 *
 * El punto es únicamente separador de miles; no representa decimales.
 */
object CurrencyInputFormatter {
    fun format(raw: String): String {
        val digits = raw.filter(Char::isDigit).trimStart('0')
        if (digits.isEmpty()) return ""
        return digits.reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    }

    fun toLongOrNull(formatted: String): Long? =
        formatted.filter(Char::isDigit).toLongOrNull()

    /**
     * Reformatea un [TextFieldValue] manteniendo el cursor en el lugar correcto.
     *
     * Con un simple `String` como valor del campo, Compose no sabe dónde va el
     * cursor cuando el texto cambia de largo (por ejemplo al insertarse un
     * nuevo punto de miles), así que lo calcula "adivinando" y a veces el
     * dígito que acabas de escribir termina insertado en una posición
     * equivocada (ej.: escribir 20.009 mostraba 20.090).
     *
     * Aquí calculamos el cursor nosotros mismos: contamos cuántos dígitos
     * había antes del cursor, formateamos el número completo, y ubicamos el
     * cursor justo después de esa misma cantidad de dígitos en el resultado
     * ya formateado. Así el cursor siempre queda pegado al dígito que se
     * acaba de escribir o borrar, sin importar en qué parte del número esté.
     */
    fun reformat(newValue: TextFieldValue): TextFieldValue {
        val cursorPos = newValue.selection.end.coerceIn(0, newValue.text.length)
        val digitsBeforeCursorRaw = newValue.text.take(cursorPos).count { it.isDigit() }

        val allDigitsRaw = newValue.text.filter { it.isDigit() }
        val allDigitsTrimmed = allDigitsRaw.trimStart('0')
        val leadingZerosRemoved = allDigitsRaw.length - allDigitsTrimmed.length

        val digitsBeforeCursor =
            (digitsBeforeCursorRaw - leadingZerosRemoved).coerceIn(0, allDigitsTrimmed.length)

        val formatted = format(newValue.text)

        var newCursorPos = 0
        if (digitsBeforeCursor > 0) {
            var digitCount = 0
            for (i in formatted.indices) {
                if (formatted[i].isDigit()) {
                    digitCount++
                    if (digitCount == digitsBeforeCursor) {
                        newCursorPos = i + 1
                        break
                    }
                }
            }
        }

        return TextFieldValue(text = formatted, selection = TextRange(newCursorPos))
    }
}
