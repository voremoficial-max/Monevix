package com.monevix.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monevix.app.data.settlement.SettlementRepository

class MonthlySummaryViewModelFactory(
    private val settlementRepository: SettlementRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonthlySummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonthlySummaryViewModel(settlementRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
