package com.kazemieh.utilities.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.monthLength
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.jalali.JalaliCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Net income/expense total for a single day of the viewed month. */
data class DayTotal(
    val income: Long = 0,
    val expense: Long = 0
) {
    val net: Long get() = income - expense
    val hasActivity: Boolean get() = income != 0L || expense != 0L
}

data class FinancialCalendarState(
    val year: Int = JalaliCalendar.today().year,
    val month: Int = JalaliCalendar.today().month,
    val isLoading: Boolean = false,
    val dayTotals: Map<Int, DayTotal> = emptyMap(),
    val selectedDay: Int? = null,
    val selectedDayTransactions: List<TransactionWithRelations> = emptyList()
) {
    val monthLabel: String get() = PersianDateTime(year, month, 1).persianMonth().displayName
}

sealed interface FinancialCalendarIntent {
    data object PreviousMonth : FinancialCalendarIntent
    data object NextMonth : FinancialCalendarIntent
    data class SelectDay(val day: Int) : FinancialCalendarIntent
    data object ClearSelection : FinancialCalendarIntent
}

class FinancialCalendarViewModel(
    private val observeTransactions: ObserveTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FinancialCalendarState())
    val state: StateFlow<FinancialCalendarState> = _state.asStateFlow()

    private var allMonthTransactions: List<TransactionWithRelations> = emptyList()

    init {
        loadMonth()
    }

    fun onIntent(intent: FinancialCalendarIntent) {
        when (intent) {
            FinancialCalendarIntent.PreviousMonth -> {
                val (y, m) = shiftMonth(_state.value.year, _state.value.month, -1)
                _state.value = _state.value.copy(year = y, month = m, selectedDay = null, selectedDayTransactions = emptyList())
                loadMonth()
            }

            FinancialCalendarIntent.NextMonth -> {
                val (y, m) = shiftMonth(_state.value.year, _state.value.month, 1)
                _state.value = _state.value.copy(year = y, month = m, selectedDay = null, selectedDayTransactions = emptyList())
                loadMonth()
            }

            is FinancialCalendarIntent.SelectDay -> {
                val dayTx = allMonthTransactions.filter { runCatching { PersianDateTime.parse(it.transaction.date).day }.getOrNull() == intent.day }
                _state.value = _state.value.copy(selectedDay = intent.day, selectedDayTransactions = dayTx)
            }

            FinancialCalendarIntent.ClearSelection -> {
                _state.value = _state.value.copy(selectedDay = null, selectedDayTransactions = emptyList())
            }
        }
    }

    private fun shiftMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        var y = year
        var m = month + delta
        while (m > 12) { m -= 12; y += 1 }
        while (m < 1) { m += 12; y -= 1 }
        return y to m
    }

    private fun loadMonth() {
        val year = _state.value.year
        val month = _state.value.month
        val monthLength = PersianDateTime(year, month, 1).monthLength()
        val from = PersianDateTime(year, month, 1, 0, 0, 0).toEpochMilliseconds()
        val to = PersianDateTime(year, month, monthLength, 23, 59, 59).toEpochMilliseconds()

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            observeTransactions(
                TransactionFilterParams(fromTimestamp = from, toTimestamp = to),
                PageRequest(limit = 2000, offset = 0)
            ).collect { page ->
                allMonthTransactions = page.items
                val totals = mutableMapOf<Int, DayTotal>()
                page.items.forEach { item ->
                    val day = runCatching { PersianDateTime.parse(item.transaction.date).day }.getOrNull() ?: return@forEach
                    val current = totals[day] ?: DayTotal()
                    totals[day] = when (item.transaction.type) {
                        TransactionType.INCOME -> current.copy(income = current.income + item.transaction.amount)
                        TransactionType.EXPENSE -> current.copy(expense = current.expense + item.transaction.amount)
                        else -> current
                    }
                }
                _state.value = _state.value.copy(isLoading = false, dayTotals = totals)
            }
        }
    }
}
