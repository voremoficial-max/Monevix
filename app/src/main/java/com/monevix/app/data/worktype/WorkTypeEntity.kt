package com.monevix.app.data.worktype

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa un tipo de trabajo (tarea) que puede realizar un trabajador,
 * junto con su tarifa vigente por unidad producida.
 *
 * IMPORTANTE (regla del proyecto): un [WorkTypeEntity] nunca se elimina
 * físicamente. Cuando ya no se usa, se marca [isActive] = false. Esto es
 * indispensable porque en fases posteriores las liquidaciones (Settlement)
 * guardarán una copia del precio unitario vigente al momento de liquidar
 * (campo separado en SettlementItem), por lo que cambiar o desactivar un
 * trabajo aquí jamás debe alterar liquidaciones ya guardadas.
 *
 * El [code] es único (ver índice único) y se guarda siempre en mayúsculas
 * para evitar duplicados como "c01" y "C01".
 */
@Entity(
    tableName = "work_types",
    indices = [Index(value = ["code"], unique = true)]
)
data class WorkTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val code: String,
    val name: String,
    /** Precio vigente por unidad, en pesos (sin decimales). */
    val unitPrice: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
