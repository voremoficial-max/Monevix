package com.monevix.app.data.worktype

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de trabajos (tarifas). Única puerta de entrada de la UI/ViewModel
 * hacia la base de datos (Room) para todo lo relacionado con tipos de trabajo.
 */
class WorkTypeRepository(private val workTypeDao: WorkTypeDao) {

    fun observeAll(): Flow<List<WorkTypeEntity>> = workTypeDao.observeAll()

    fun observeActive(): Flow<List<WorkTypeEntity>> = workTypeDao.observeActive()

    fun search(query: String): Flow<List<WorkTypeEntity>> = workTypeDao.search(query.trim())

    suspend fun getById(workTypeId: Long): WorkTypeEntity? = workTypeDao.getById(workTypeId)

    suspend fun isCodeTaken(code: String, excludingId: Long = -1L): Boolean =
        workTypeDao.countByCode(code.trim(), excludingId) > 0

    suspend fun create(code: String, name: String, unitPrice: Long): Long {
        val workType = WorkTypeEntity(
            code = code.trim().uppercase(),
            name = name.trim(),
            unitPrice = unitPrice,
            isActive = true
        )
        return workTypeDao.insert(workType)
    }

    suspend fun update(workType: WorkTypeEntity) = workTypeDao.update(workType)

    /** Marca el trabajo como inactivo sin borrarlo ni afectar liquidaciones pasadas. */
    suspend fun deactivate(workType: WorkTypeEntity) =
        workTypeDao.update(workType.copy(isActive = false))

    /** Reactiva un trabajo previamente desactivado. */
    suspend fun activate(workType: WorkTypeEntity) =
        workTypeDao.update(workType.copy(isActive = true))
}
