package com.monevix.app.data.settlement

import androidx.room.withTransaction
import com.monevix.app.data.AppDatabase
import kotlinx.coroutines.flow.Flow
import com.monevix.app.data.workentry.WorkEntryEntity

class SettlementRepository(private val database: AppDatabase) {
    private val dao = database.settlementDao()

    fun observeAll(): Flow<List<SettlementEntity>> = dao.observeAll()

    fun observeByWorker(workerId: Long): Flow<List<SettlementEntity>> =
        dao.observeByWorker(workerId)

    fun observeFiltered(workerId: Long?, fromMillis: Long, toMillis: Long): Flow<List<SettlementEntity>> =
        dao.observeFiltered(workerId, fromMillis, toMillis)

    fun observeItems(settlementId: Long): Flow<List<SettlementItemEntity>> =
        dao.observeItems(settlementId)

    suspend fun getItems(settlementId: Long): List<SettlementItemEntity> =
        dao.getItems(settlementId)

    suspend fun create(settlement: SettlementEntity, items: List<SettlementItemEntity>): Long =
        database.withTransaction {
            val id = dao.insertSettlement(settlement)
            dao.insertItems(items.map { it.copy(settlementId = id) })
            id
        }

    suspend fun createWithWorkEntries(
        settlement: SettlementEntity,
        items: List<SettlementItemEntity>,
        entries: List<WorkEntryEntity>
    ): Long = database.withTransaction {
        val settlementId = dao.insertSettlement(settlement)
        dao.insertItems(items.map { it.copy(settlementId = settlementId) })
        database.workEntryDao().insertAll(entries)
        settlementId
    }

    suspend fun count(): Int = dao.count()

    fun observeMonthlySummary(fromMillis: Long, toMillis: Long): Flow<List<WorkerMonthlySummary>> =
        dao.observeMonthlySummary(fromMillis, toMillis)

    fun observeMonthlyTrend(): Flow<List<WorkerMonthlyTrend>> =
        dao.observeMonthlyTrend()
}
