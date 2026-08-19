package com.monevix.app.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monevix.app.data.settlement.SettlementRepository
import com.monevix.app.data.worker.WorkerRepository

class SettlementViewModelFactory(
    private val settlementRepository: SettlementRepository,
    private val workerRepository: WorkerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettlementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettlementViewModel(settlementRepository, workerRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
