package com.monevix.app.data.payment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: PaymentEntity): Long

    @Insert
    suspend fun insertItems(items: List<PaymentItemEntity>)

    @Insert
    suspend fun insertAll(payments: List<PaymentEntity>)

    @Query("SELECT * FROM payments ORDER BY paidAtMillis DESC, id DESC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE workerId = :workerId ORDER BY paidAtMillis DESC, id DESC")
    fun observeByWorker(workerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY id ASC")
    suspend fun getAll(): List<PaymentEntity>

    @Query("SELECT * FROM payment_items ORDER BY id ASC")
    suspend fun getAllItems(): List<PaymentItemEntity>

    @Query("SELECT * FROM payment_items WHERE paymentId = :paymentId ORDER BY dateMillis ASC, id ASC")
    fun observeItems(paymentId: Long): Flow<List<PaymentItemEntity>>

    @Query("SELECT * FROM payment_items WHERE paymentId = :paymentId ORDER BY dateMillis ASC, id ASC")
    suspend fun getItems(paymentId: Long): List<PaymentItemEntity>

    @Query("DELETE FROM payment_items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM payments")
    suspend fun deleteAll()
}
