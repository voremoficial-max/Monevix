package com.monevix.app.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monevix.app.data.settlement.SettlementEntity
import com.monevix.app.data.settlement.SettlementItemEntity
import com.monevix.app.data.settlement.SettlementRepository
import com.monevix.app.data.worker.WorkerRepository
import com.monevix.app.domain.CalculationItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SaveSettlementResult {
    data class Success(val id: Long) : SaveSettlementResult()
    data class Error(val message: String) : SaveSettlementResult()
}

class SettlementViewModel(
    private val settlementRepository: SettlementRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    val workers = workerRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedWorkerId = MutableStateFlow<Long?>(null)
    val selectedWorkerId = _selectedWorkerId.asStateFlow()

    private val _fromMillis = MutableStateFlow(startOfCurrentMonth())
    private val _toMillis = MutableStateFlow(endOfCurrentMonth())
    val fromMillis = _fromMillis.asStateFlow()
    val toMillis = _toMillis.asStateFlow()

    val settlements: StateFlow<List<SettlementEntity>> =
        combine(_selectedWorkerId, _fromMillis, _toMillis) { worker, from, to -> Triple(worker, from, to) }
            .flatMapLatest { (worker, from, to) ->
                settlementRepository.observeFiltered(worker, from, to)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setWorker(id: Long?) { _selectedWorkerId.value = id }
    fun setDateRange(from: Long, to: Long) {
        _fromMillis.value = from
        _toMillis.value = to
    }

    fun observeItems(id: Long): Flow<List<SettlementItemEntity>> =
        settlementRepository.observeItems(id)

    suspend fun save(workerId: Long, workerName: String, items: List<CalculationItem>): SaveSettlementResult {
        if (items.isEmpty()) return SaveSettlementResult.Error("Añade al menos un trabajo.")
        val worker = workerRepository.getById(workerId)
            ?: return SaveSettlementResult.Error("El trabajador ya no existe.")
        if (!worker.isActive) return SaveSettlementResult.Error("El trabajador está inactivo.")

        val now = System.currentTimeMillis()
        val total = items.sumOf { it.subtotal }
        val settlement = SettlementEntity(
            workerId = workerId,
            workerName = workerName,
            dateMillis = now,
            periodLabel = currentPeriodLabel(),
            total = total
        )
        val detail = items.map {
            SettlementItemEntity(
                settlementId = 0,
                workTypeId = it.workTypeId,
                code = it.code,
                name = it.name,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                subtotal = it.subtotal
            )
        }
        return try {
            SaveSettlementResult.Success(settlementRepository.create(settlement, detail))
        } catch (e: Exception) {
            SaveSettlementResult.Error("No se pudo guardar la liquidación: ${e.message ?: "error desconocido"}.")
        }
    }

    companion object {
        private fun startOfCurrentMonth(): Long {
            val c = java.util.Calendar.getInstance()
            c.set(java.util.Calendar.DAY_OF_MONTH, 1); zeroTime(c); return c.timeInMillis
        }
        private fun endOfCurrentMonth(): Long {
            val c = java.util.Calendar.getInstance()
            c.set(java.util.Calendar.DAY_OF_MONTH, c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            c.set(java.util.Calendar.HOUR_OF_DAY, 23); c.set(java.util.Calendar.MINUTE,59); c.set(java.util.Calendar.SECOND,59); c.set(java.util.Calendar.MILLISECOND,999)
            return c.timeInMillis
        }
        private fun zeroTime(c: java.util.Calendar) {
            c.set(java.util.Calendar.HOUR_OF_DAY,0); c.set(java.util.Calendar.MINUTE,0); c.set(java.util.Calendar.SECOND,0); c.set(java.util.Calendar.MILLISECOND,0)
        }
        private fun currentPeriodLabel(): String {
            val f = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("es","CO"))
            return f.format(java.util.Date())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale("es","CO")) else it.toString() }
        }
    }
}
