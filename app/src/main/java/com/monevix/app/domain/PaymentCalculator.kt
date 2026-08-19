package com.monevix.app.domain

/**
 * Lógica de cálculo de pagos, separada de la interfaz (regla del proyecto:
 * "la lógica de cálculo debe estar separada de la interfaz"). Es lógica pura
 * y por eso se puede probar con pruebas unitarias JVM sin depender de
 * Android.
 */
object PaymentCalculator {

    /** Subtotal de una línea: cantidad × precio unitario. */
    fun subtotal(quantity: Int, unitPrice: Long): Long = quantity * unitPrice

    /** Total de una liquidación: suma de los subtotales de todas sus líneas. */
    fun total(items: List<CalculationItem>): Long = items.sumOf { it.subtotal }
}
