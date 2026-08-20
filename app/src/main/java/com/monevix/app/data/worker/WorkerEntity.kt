package com.monevix.app.data.worker

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa a un trabajador dentro del sistema.
 *
 * Un trabajador nunca se elimina físicamente de la base de datos: cuando ya
 * no está activo, se marca con [isActive] = false para conservar la
 * integridad de las liquidaciones históricas asociadas a él (regla de datos
 * del proyecto: nunca borrar registros históricos).
 */
@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val documentId: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
