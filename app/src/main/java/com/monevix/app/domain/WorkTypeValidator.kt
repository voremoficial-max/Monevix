package com.monevix.app.domain

/**
 * Reglas de validación para el formulario de trabajos (tarifas).
 * Lógica pura (sin Android ni Compose) para poder probarla con pruebas
 * unitarias JVM, tal como pide el proyecto.
 */
object WorkTypeValidator {

    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 60

    const val MIN_CODE_LENGTH = 1
    const val MAX_CODE_LENGTH = 10
    const val MAX_UNIT_PRICE = 100_000_000L

    private val CODE_REGEX = Regex("^[A-Za-z0-9-]+$")

    /**
     * Valida el código de un trabajo (ej. "C01").
     * @return un mensaje de error legible, o null si el código es válido.
     */
    fun validateCode(code: String): String? {
        val trimmed = code.trim()
        return when {
            trimmed.isEmpty() -> "El código es obligatorio."
            trimmed.length < MIN_CODE_LENGTH -> "El código es demasiado corto."
            trimmed.length > MAX_CODE_LENGTH -> "El código debe tener máximo $MAX_CODE_LENGTH caracteres."
            !CODE_REGEX.matches(trimmed) -> "El código solo puede tener letras, números y guiones."
            else -> null
        }
    }

    /**
     * Valida el nombre de un trabajo (ej. "Cocido de telas").
     * @return un mensaje de error legible, o null si el nombre es válido.
     */
    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "El nombre es obligatorio."
            trimmed.length < MIN_NAME_LENGTH -> "El nombre debe tener al menos $MIN_NAME_LENGTH caracteres."
            trimmed.length > MAX_NAME_LENGTH -> "El nombre es demasiado largo."
            else -> null
        }
    }

    /**
     * Valida el precio unitario ingresado como texto (para poder validar
     * directamente lo que el usuario escribe en el formulario, antes de
     * convertirlo a Long).
     * @return un mensaje de error legible, o null si el precio es válido.
     */
    fun validateUnitPrice(rawPrice: String): String? {
        val trimmed = rawPrice.trim()
        if (trimmed.isEmpty()) return "El precio es obligatorio."

        val price = trimmed.toLongOrNull()
            ?: return "Ingresa un precio válido (solo números)."

        return when {
            price <= 0L -> "El precio debe ser mayor que cero."
            price > MAX_UNIT_PRICE -> "El precio ingresado es demasiado alto."
            else -> null
        }
    }
}
