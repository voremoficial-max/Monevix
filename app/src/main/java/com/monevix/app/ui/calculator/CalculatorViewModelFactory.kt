package com.monevix.app.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monevix.app.data.worker.WorkerRepository
import com.monevix.app.data.worktype.WorkTypeRepository
import com.monevix.app.data.settlement.SettlementRepository

class CalculatorViewModelFactory(
    private val workerRepository: WorkerRepository,
    private val workTypeRepository: WorkTypeRepository,
    private val settlementRepository: SettlementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(workerRepository, workTypeRepository, settlementRepository) as T
        }
        throw IllegalArgumentException("Clase de ViewModel desconocida: ${modelClass.name}")
    }
}
