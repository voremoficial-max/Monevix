package com.monevix.app.data.settlement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.monevix.app.data.worker.WorkerEntity

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = WorkerEntity::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("workerId"), Index("dateMillis")]
)
data class SettlementEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: Long,
    val workerName: String,
    val dateMillis: Long,
    val periodLabel: String,
    val total: Long
)
