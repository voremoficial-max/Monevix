package com.monevix.app.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formateador de moneda único para toda la app.
 *
 * Muestra el peso colombiano sin decimales y con punto SOLO como
 * separador de miles/millones (ej. $4.000, $140.000, $1.000.000),
 * nunca como separador decimal. Se usa en Trabajos, Calculadora,
 * Liquidaciones y Resumen mensual para que el formato sea idéntico
 * en toda la aplicación.
 */
object CurrencyFormatter {

    private val numberFormat: NumberFormat =
        NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }

    fun format(amount: Long): String = "$" + numberFormat.format(amount)
}
