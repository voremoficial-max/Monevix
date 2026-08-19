package com.monevix.app.domain

/**
 * Representa una línea añadida en la Calculadora: un trabajo, la cantidad
 * producida y el precio unitario vigente en el momento de agregarlo.
 *
 * Es un modelo de dominio puro (sin Room ni Compose) usado mientras el
 * usuario arma una liquidación. En la Fase 5 se persistirá como
 * `SettlementItem`, guardando siempre [unitPrice] tal como quedó aquí para
 * que cambios futuros en el trabajo no alteren liquidaciones ya guardadas.
 */
data class CalculationItem(
    val workTypeId: Long,
    val code: String,
    val name: String,
    val unitPrice: Long,
    val quantity: Int
) {
    /** Subtotal de esta línea: cantidad × precio unitario. */
    val subtotal: Long
        get() = unitPrice * quantity
}
