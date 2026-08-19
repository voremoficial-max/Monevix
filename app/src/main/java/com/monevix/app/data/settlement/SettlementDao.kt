package com.monevix.app.data.settlement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Insert
    suspend fun insertSettlement(settlement: SettlementEntity): Long

    @Insert
    suspend fun insertAll(settlements: List<SettlementEntity>)

    @Insert
    suspend fun insertItems(items: List<SettlementItemEntity>)

    @Query("DELETE FROM settlement_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM settlements")
    suspend fun deleteAll()

    @Query("SELECT * FROM settlements ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements ORDER BY id ASC")
    suspend fun getAll(): List<SettlementEntity>

    @Query("SELECT * FROM settlement_items ORDER BY id ASC")
    suspend fun getAllItems(): List<SettlementItemEntity>

    @Query("SELECT * FROM settlements WHERE workerId = :workerId ORDER BY dateMillis DESC, id DESC")
    fun observeByWorker(workerId: Long): Flow<List<SettlementEntity>>

    @Query("""
        SELECT * FROM settlements
        WHERE (:workerId IS NULL OR workerId = :workerId)
          AND dateMillis >= :fromMillis
          AND dateMillis <= :toMillis
        ORDER BY dateMillis DESC, id DESC
    """)
    fun observeFiltered(workerId: Long?, fromMillis: Long, toMillis: Long): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlement_items WHERE settlementId = :settlementId ORDER BY id")
    suspend fun getItems(settlementId: Long): List<SettlementItemEntity>

    @Query("SELECT * FROM settlement_items WHERE settlementId = :settlementId ORDER BY id")
    fun observeItems(settlementId: Long): Flow<List<SettlementItemEntity>>

    @Query("SELECT COUNT(*) FROM settlements")
    suspend fun count(): Int

    /**
     * Resumen mensual agrupado por trabajador: total pagado, número de
     * liquidaciones y cantidad total de trabajos realizados en el rango de
     * fechas indicado (normalmente el primer y último milisegundo de un mes).
     */
    @Query(
        """
        SELECT
            s.workerId AS workerId,
            s.workerName AS workerName,
            SUM(s.total) AS totalPaid,
            COUNT(DISTINCT s.id) AS settlementCount,
            COALESCE((
                SELECT SUM(si.quantity) FROM settlement_items si
                WHERE si.settlementId IN (
                    SELECT id FROM settlements s2
                    WHERE s2.workerId = s.workerId
                      AND s2.dateMillis >= :fromMillis AND s2.dateMillis <= :toMillis
                )
            ), 0) AS totalQuantity
        FROM settlements s
        WHERE s.dateMillis >= :fromMillis AND s.dateMillis <= :toMillis
        GROUP BY s.workerId, s.workerName
        ORDER BY totalPaid DESC
        """
    )
    fun observeMonthlySummary(fromMillis: Long, toMillis: Long): Flow<List<WorkerMonthlySummary>>

    @Query(
        """
        SELECT
            s.workerId AS workerId,
            s.workerName AS workerName,
            strftime('%Y-%m', s.dateMillis / 1000, 'unixepoch', 'localtime') AS monthKey,
            SUM(s.total) AS totalPaid,
            COALESCE((
                SELECT SUM(si.quantity) FROM settlement_items si
                WHERE si.settlementId IN (
                    SELECT id FROM settlements s2
                    WHERE s2.workerId = s.workerId
                      AND strftime('%Y-%m', s2.dateMillis / 1000, 'unixepoch', 'localtime') = strftime('%Y-%m', s.dateMillis / 1000, 'unixepoch', 'localtime')
                )
            ), 0) AS totalQuantity
        FROM settlements s
        GROUP BY s.workerId, s.workerName, monthKey
        ORDER BY monthKey ASC, totalPaid DESC
        """
    )
    fun observeMonthlyTrend(): Flow<List<WorkerMonthlyTrend>>
}

/**
 * Fila del resumen mensual: lo pagado, el número de liquidaciones y la
 * cantidad de trabajos realizados por un trabajador dentro de un mes.
 */
data class WorkerMonthlySummary(
    val workerId: Long,
    val workerName: String,
    val totalPaid: Long,
    val settlementCount: Int,
    val totalQuantity: Int
)

data class WorkerMonthlyTrend(
    val workerId: Long,
    val workerName: String,
    val monthKey: String,
    val totalPaid: Long,
    val totalQuantity: Int
)
