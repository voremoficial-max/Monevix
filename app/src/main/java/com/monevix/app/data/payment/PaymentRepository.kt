package com.monevix.app.data.payment

import androidx.room.withTransaction
import com.monevix.app.data.AppDatabase
import com.monevix.app.data.workentry.WorkEntryDao
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val database: AppDatabase) {
    private val paymentDao = database.paymentDao()
    private val workEntryDao = database.workEntryDao()

    fun observeAll(): Flow<List<PaymentEntity>> = paymentDao.observeAll()
    fun observeByWorker(workerId: Long): Flow<List<PaymentEntity>> = paymentDao.observeByWorker(workerId)
    fun observeItems(paymentId: Long): Flow<List<PaymentItemEntity>> = paymentDao.observeItems(paymentId)
    suspend fun getItems(paymentId: Long): List<PaymentItemEntity> = paymentDao.getItems(paymentId)
    suspend fun getAll(): List<PaymentEntity> = paymentDao.getAll()
    suspend fun getAllItems(): List<PaymentItemEntity> = paymentDao.getAllItems()

    suspend fun payWorker(
        workerId: Long,
        workerName: String,
        companyName: String,
        periodType: String,
        periodLabel: String,
        paidAtMillis: Long
    ): Long = database.withTransaction {
        val entries = workEntryDao.getPendingByWorker(workerId)
        require(entries.isNotEmpty()) { "Este trabajador no tiene trabajos pendientes." }
        val total = entries.sumOf { it.subtotal }
        val paymentId = paymentDao.insert(
            PaymentEntity(
                workerId = workerId,
                workerName = workerName,
                companyName = companyName,
                paidAtMillis = paidAtMillis,
                periodType = periodType,
                periodLabel = periodLabel,
                total = total
            )
        )
        paymentDao.insertItems(entries.map {
            PaymentItemEntity(
                paymentId = paymentId,
                workEntryId = it.id,
                dateMillis = it.dateMillis,
                code = it.code,
                name = it.name,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                subtotal = it.subtotal
            )
        })
        workEntryDao.markPaid(entries.map { it.id }, paymentId)
        paymentId
    }
}
