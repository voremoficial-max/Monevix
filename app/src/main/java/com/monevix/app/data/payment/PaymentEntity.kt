package com.monevix.app.data.payment

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.monevix.app.data.worker.WorkerEntity

@Entity(
    tableName = "payments",
    foreignKeys = [ForeignKey(entity = WorkerEntity::class, parentColumns = ["id"], childColumns = ["workerId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("workerId"), Index("paidAtMillis")]
)
data class PaymentEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: Long,
    val workerName: String,
    val companyName: String,
    val paidAtMillis: Long,
    val periodType: String,
    val periodLabel: String,
    val total: Long
)
