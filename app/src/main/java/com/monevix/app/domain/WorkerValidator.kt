package com.monevix.app.domain

/**
 * Reglas de validación para el formulario de trabajadores.
 * Es lógica pura (sin Android ni Compose) para poder probarla fácilmente
 * con pruebas unitarias JVM, tal como pide el proyecto.
 */
object WorkerValidator {

    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 80

    /**
     * Valida el nombre de un trabajador.
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

    /** Valida el teléfono opcional: si se ingresa, solo dígitos, espacios, + y -. */
    fun validatePhone(phone: String?): String? {
        if (phone.isNullOrBlank()) return null
        val regex = Regex("^[0-9+\\-\\s]{6,20}$")
        return if (!regex.matches(phone.trim())) {
            "Ingresa un teléfono válido (solo números)."
        } else {
            null
        }
    }
}
