package com.monevix.app.data.workentry

import com.monevix.app.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class WorkEntryRepository(private val database: AppDatabase) {
    private val dao = database.workEntryDao()

    fun observePendingByWorker(workerId: Long): Flow<List<WorkEntryEntity>> = dao.observePendingByWorker(workerId)
    fun observePending(): Flow<List<WorkEntryEntity>> = dao.observePending()
    suspend fun insertAll(entries: List<WorkEntryEntity>) = dao.insertAll(entries)
    suspend fun getAll(): List<WorkEntryEntity> = dao.getAll()
    suspend fun countPending(): Int = dao.countPending()
}
