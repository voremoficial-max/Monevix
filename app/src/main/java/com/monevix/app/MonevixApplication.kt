package com.monevix.app

import android.app.Application
import com.monevix.app.data.AppDatabase
import com.monevix.app.data.worker.WorkerRepository
import com.monevix.app.data.worktype.WorkTypeRepository
import com.monevix.app.data.settlement.SettlementRepository
import com.monevix.app.data.workentry.WorkEntryRepository
import com.monevix.app.data.payment.PaymentRepository

/**
 * Clase Application de Monevix.
 *
 * Actúa como un contenedor manual de dependencias (sin librerías externas
 * como Hilt, para mantener el proyecto simple en estas primeras fases):
 * crea la base de datos y los repositorios una sola vez y los expone para
 * que las pantallas puedan construir sus ViewModels con [ViewModelFactory].
 */
class MonevixApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val workerRepository: WorkerRepository by lazy {
        WorkerRepository(database.workerDao())
    }

    val workTypeRepository: WorkTypeRepository by lazy {
        WorkTypeRepository(database.workTypeDao())
    }

    val settlementRepository: SettlementRepository by lazy {
        SettlementRepository(database)
    }

    val workEntryRepository: WorkEntryRepository by lazy {
        WorkEntryRepository(database)
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(database)
    }
}
