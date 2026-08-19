package com.monevix.app.domain

/**
 * Reglas de validación para la Calculadora de pagos (Fase 4).
 * Lógica pura (sin Android ni Compose) para poder probarla con pruebas
 * unitarias JVM.
 */
object CalculatorValidator {

    const val MAX_QUANTITY = 100_000

    /**
     * Valida la cantidad ingresada como texto en el diálogo "Añadir trabajo".
     * @return un mensaje de error legible, o null si la cantidad es válida.
     */
    fun validateQuantity(rawQuantity: String): String? {
        val trimmed = rawQuantity.trim()
        if (trimmed.isEmpty()) return "La cantidad es obligatoria."

        val quantity = trimmed.toIntOrNull()
            ?: return "Ingresa una cantidad válida (solo números)."

        return when {
            quantity <= 0 -> "La cantidad debe ser mayor que cero."
            quantity > MAX_QUANTITY -> "La cantidad ingresada es demasiado alta."
            else -> null
        }
    }
}
