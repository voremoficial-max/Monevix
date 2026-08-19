package com.monevix.app.data.workentry

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.monevix.app.data.payment.PaymentEntity
import com.monevix.app.data.worker.WorkerEntity

@Entity(
    tableName = "work_entries",
    foreignKeys = [
        ForeignKey(entity = WorkerEntity::class, parentColumns = ["id"], childColumns = ["workerId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = PaymentEntity::class, parentColumns = ["id"], childColumns = ["paymentId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("workerId"), Index("dateMillis"), Index("paymentId")]
)
data class WorkEntryEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val workerName: String,
    val dateMillis: Long,
    val workTypeId: Long,
    val code: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long,
    val paymentId: Long? = null
)
