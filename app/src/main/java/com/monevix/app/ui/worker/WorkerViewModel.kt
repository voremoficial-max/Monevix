package com.monevix.app.ui.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monevix.app.data.worker.WorkerEntity
import com.monevix.app.data.worker.WorkerRepository
import com.monevix.app.domain.WorkerValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Resultado de intentar guardar un trabajador desde el formulario. */
sealed class SaveWorkerResult {
    data object Success : SaveWorkerResult()
    data class ValidationError(val message: String) : SaveWorkerResult()
    data class NameAlreadyExists(val message: String) : SaveWorkerResult()
}

class WorkerViewModel(private val repository: WorkerRepository) : ViewModel() {

    /** Lista completa (activos e inactivos) para la pantalla de Personal. */
    val workers: StateFlow<List<WorkerEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getWorker(id: Long): WorkerEntity? = repository.getById(id)

    /**
     * Crea o actualiza un trabajador según si [existingId] es null.
     * Toda la validación de negocio vive aquí, no en la pantalla Compose.
     */
    suspend fun save(
        existingId: Long?,
        name: String,
        documentId: String,
        phone: String
    ): SaveWorkerResult {
        val nameError = WorkerValidator.validateName(name)
        if (nameError != null) return SaveWorkerResult.ValidationError(nameError)

        val phoneError = WorkerValidator.validatePhone(phone)
        if (phoneError != null) return SaveWorkerResult.ValidationError(phoneError)

        val nameTaken = repository.isNameTaken(name, excludingId = existingId ?: -1L)
        if (nameTaken) {
            return SaveWorkerResult.NameAlreadyExists("Ya existe un trabajador con ese nombre.")
        }

        if (existingId == null) {
            repository.create(name, documentId, phone)
        } else {
            val current = repository.getById(existingId)
            if (current != null) {
                repository.update(
                    current.copy(
                        name = name.trim(),
                        documentId = documentId.trim().ifBlank { null },
                        phone = phone.trim().ifBlank { null }
                    )
                )
            }
        }
        return SaveWorkerResult.Success
    }

    fun setActive(worker: WorkerEntity, active: Boolean) {
        viewModelScope.launch {
            if (active) repository.activate(worker) else repository.deactivate(worker)
        }
    }
}
