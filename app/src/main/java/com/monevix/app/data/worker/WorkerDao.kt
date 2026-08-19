package com.monevix.app.data.worker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {

    /** Todos los trabajadores, activos e inactivos, ordenados por nombre. */
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun observeAll(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers ORDER BY id ASC")
    suspend fun getAll(): List<WorkerEntity>

    /** Solo trabajadores activos, para selectores (ej. calculadora). */
    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    fun observeActive(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE id = :workerId LIMIT 1")
    suspend fun getById(workerId: Long): WorkerEntity?

    @Insert
    suspend fun insert(worker: WorkerEntity): Long

    @Insert
    suspend fun insertAll(workers: List<WorkerEntity>)

    @Query("DELETE FROM workers")
    suspend fun deleteAll()

    @Update
    suspend fun update(worker: WorkerEntity)

    @Query("SELECT COUNT(*) FROM workers WHERE LOWER(name) = LOWER(:name) AND id != :excludingId")
    suspend fun countByName(name: String, excludingId: Long = -1L): Int
}
