package com.monevix.app.data.payment

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "payment_items",
    foreignKeys = [ForeignKey(entity = PaymentEntity::class, parentColumns = ["id"], childColumns = ["paymentId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("paymentId")]
)
data class PaymentItemEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentId: Long,
    val workEntryId: Long,
    val dateMillis: Long,
    val code: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long
)
