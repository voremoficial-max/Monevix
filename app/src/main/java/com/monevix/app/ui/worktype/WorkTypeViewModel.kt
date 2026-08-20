package com.monevix.app.ui.worktype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monevix.app.data.worktype.WorkTypeEntity
import com.monevix.app.data.worktype.WorkTypeRepository
import com.monevix.app.domain.WorkTypeValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Resultado de intentar guardar un trabajo desde el formulario. */
sealed class SaveWorkTypeResult {
    data object Success : SaveWorkTypeResult()
    data class ValidationError(val message: String) : SaveWorkTypeResult()
    data class CodeAlreadyExists(val message: String) : SaveWorkTypeResult()
}

class WorkTypeViewModel(private val repository: WorkTypeRepository) : ViewModel() {

    /** Lista completa (activos e inactivos) para la pantalla de Trabajos. */
    val workTypes: StateFlow<List<WorkTypeEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getWorkType(id: Long): WorkTypeEntity? = repository.getById(id)

    /**
     * Crea o actualiza un trabajo según si [existingId] es null.
     * Toda la validación de negocio vive aquí, no en la pantalla Compose.
     *
     * IMPORTANTE: al editar solo se actualiza el registro de WorkType. Las
     * liquidaciones ya guardadas (Fase 5) mantendrán su propia copia del
     * precio unitario usado en su momento y no se ven afectadas por este
     * cambio.
     */
    suspend fun save(
        existingId: Long?,
        code: String,
        name: String,
        unitPrice: String
    ): SaveWorkTypeResult {
        val codeError = WorkTypeValidator.validateCode(code)
        if (codeError != null) return SaveWorkTypeResult.ValidationError(codeError)

        val nameError = WorkTypeValidator.validateName(name)
        if (nameError != null) return SaveWorkTypeResult.ValidationError(nameError)

        val priceError = WorkTypeValidator.validateUnitPrice(unitPrice)
        if (priceError != null) return SaveWorkTypeResult.ValidationError(priceError)

        val codeTaken = repository.isCodeTaken(code, excludingId = existingId ?: -1L)
        if (codeTaken) {
            return SaveWorkTypeResult.CodeAlreadyExists("Ya existe un trabajo con ese código.")
        }

        val price = unitPrice.trim().toLong()

        if (existingId == null) {
            repository.create(code, name, price)
        } else {
            val current = repository.getById(existingId)
            if (current != null) {
                repository.update(
                    current.copy(
                        code = code.trim().uppercase(),
                        name = name.trim(),
                        unitPrice = price
                    )
                )
            }
        }
        return SaveWorkTypeResult.Success
    }

    fun setActive(workType: WorkTypeEntity, active: Boolean) {
        viewModelScope.launch {
            if (active) repository.activate(workType) else repository.deactivate(workType)
        }
    }
}
