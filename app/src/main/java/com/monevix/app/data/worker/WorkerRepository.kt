package com.monevix.app.data.worker

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de trabajadores. Es la única puerta de entrada de la UI/ViewModel
 * hacia la base de datos (Room) para todo lo relacionado con trabajadores.
 */
class WorkerRepository(private val workerDao: WorkerDao) {

    fun observeAll(): Flow<List<WorkerEntity>> = workerDao.observeAll()

    fun observeActive(): Flow<List<WorkerEntity>> = workerDao.observeActive()

    suspend fun getById(workerId: Long): WorkerEntity? = workerDao.getById(workerId)

    suspend fun isNameTaken(name: String, excludingId: Long = -1L): Boolean =
        workerDao.countByName(name.trim(), excludingId) > 0

    suspend fun create(name: String, documentId: String?, phone: String?): Long {
        val worker = WorkerEntity(
            name = name.trim(),
            documentId = documentId?.trim()?.ifBlank { null },
            phone = phone?.trim()?.ifBlank { null },
            isActive = true
        )
        return workerDao.insert(worker)
    }

    suspend fun update(worker: WorkerEntity) = workerDao.update(worker)

    /** Marca al trabajador como inactivo sin borrar su historial. */
    suspend fun deactivate(worker: WorkerEntity) =
        workerDao.update(worker.copy(isActive = false))

    /** Reactiva a un trabajador previamente desactivado. */
    suspend fun activate(worker: WorkerEntity) =
        workerDao.update(worker.copy(isActive = true))
}
