package com.monevix.app.data.worktype

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkTypeDao {

    /** Todos los trabajos, activos e inactivos, ordenados por código. */
    @Query("SELECT * FROM work_types ORDER BY code ASC")
    fun observeAll(): Flow<List<WorkTypeEntity>>

    @Query("SELECT * FROM work_types ORDER BY id ASC")
    suspend fun getAll(): List<WorkTypeEntity>

    /** Solo trabajos activos, para selectores (ej. Calculadora en Fase 4). */
    @Query("SELECT * FROM work_types WHERE isActive = 1 ORDER BY code ASC")
    fun observeActive(): Flow<List<WorkTypeEntity>>

    @Query("SELECT * FROM work_types WHERE id = :workTypeId LIMIT 1")
    suspend fun getById(workTypeId: Long): WorkTypeEntity?

    /** Búsqueda por código o nombre, usada por la Calculadora (Fase 4). */
    @Query(
        """
        SELECT * FROM work_types
        WHERE isActive = 1
        AND (code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%')
        ORDER BY code ASC
        """
    )
    fun search(query: String): Flow<List<WorkTypeEntity>>

    @Insert
    suspend fun insert(workType: WorkTypeEntity): Long

    @Insert
    suspend fun insertAll(workTypes: List<WorkTypeEntity>)

    @Query("DELETE FROM work_types")
    suspend fun deleteAll()

    @Update
    suspend fun update(workType: WorkTypeEntity)

    @Query("SELECT COUNT(*) FROM work_types WHERE UPPER(code) = UPPER(:code) AND id != :excludingId")
    suspend fun countByCode(code: String, excludingId: Long = -1L): Int
}
