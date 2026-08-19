package com.monevix.app.data.workentry

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {
    @Insert
    suspend fun insert(entry: WorkEntryEntity): Long

    @Insert
    suspend fun insertAll(entries: List<WorkEntryEntity>)

    @Query("SELECT * FROM work_entries ORDER BY dateMillis DESC, id DESC")
    suspend fun getAll(): List<WorkEntryEntity>

    @Query("SELECT * FROM work_entries WHERE workerId = :workerId AND paymentId IS NULL ORDER BY dateMillis DESC, id DESC")
    fun observePendingByWorker(workerId: Long): Flow<List<WorkEntryEntity>>

    @Query("SELECT * FROM work_entries WHERE workerId = :workerId AND paymentId IS NULL ORDER BY dateMillis DESC, id DESC")
    suspend fun getPendingByWorker(workerId: Long): List<WorkEntryEntity>

    @Query("SELECT * FROM work_entries WHERE paymentId IS NULL ORDER BY workerName ASC, dateMillis DESC, id DESC")
    fun observePending(): Flow<List<WorkEntryEntity>>

    @Query("UPDATE work_entries SET paymentId = :paymentId WHERE id IN (:entryIds) AND paymentId IS NULL")
    suspend fun markPaid(entryIds: List<Long>, paymentId: Long)

    @Query("DELETE FROM work_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM work_entries WHERE paymentId IS NULL")
    suspend fun countPending(): Int
}
