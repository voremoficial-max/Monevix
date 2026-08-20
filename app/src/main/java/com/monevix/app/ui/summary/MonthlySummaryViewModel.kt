package com.monevix.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monevix.app.data.settlement.SettlementRepository
import com.monevix.app.data.settlement.WorkerMonthlySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlySummaryViewModel(
    private val settlementRepository: SettlementRepository
) : ViewModel() {

    /** 0 = mes actual, -1 = mes anterior, etc. Nunca se permite ir a meses futuros. */
    private val _monthOffset = MutableStateFlow(0)
    val monthOffset = _monthOffset.asStateFlow()

    val periodLabel: StateFlow<String> = _monthOffset
        .map { offset -> labelFor(offset) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), labelFor(0))

    val rows: StateFlow<List<WorkerMonthlySummary>> = _monthOffset
        .flatMapLatest { offset ->
            val (from, to) = rangeFor(offset)
            settlementRepository.observeMonthlySummary(from, to)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val canGoNext: Boolean get() = _monthOffset.value < 0

    fun previousMonth() {
        _monthOffset.value -= 1
    }

    fun nextMonth() {
        if (canGoNext) _monthOffset.value += 1
    }

    companion object {
        private fun rangeFor(offset: Int): Pair<Long, Long> {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, offset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val from = calendar.timeInMillis
            calendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val to = calendar.timeInMillis
            return from to to
        }

        private fun labelFor(offset: Int): String {
            val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, offset) }
            val formatter = SimpleDateFormat("MMMM yyyy", Locale("es", "CO"))
            return formatter.format(calendar.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
        }
    }
}
