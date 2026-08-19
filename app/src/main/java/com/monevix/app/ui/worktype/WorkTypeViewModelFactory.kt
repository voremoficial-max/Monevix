package com.monevix.app.ui.worktype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.monevix.app.data.worktype.WorkTypeRepository

class WorkTypeViewModelFactory(
    private val repository: WorkTypeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkTypeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkTypeViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase de ViewModel desconocida: ${modelClass.name}")
    }
}
