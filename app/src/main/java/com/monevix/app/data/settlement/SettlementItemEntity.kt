package com.monevix.app.data.settlement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "settlement_items",
    foreignKeys = [
        ForeignKey(
            entity = SettlementEntity::class,
            parentColumns = ["id"],
            childColumns = ["settlementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("settlementId")]
)
data class SettlementItemEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val settlementId: Long,
    val workTypeId: Long,
    val code: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Long,
    val subtotal: Long
)
