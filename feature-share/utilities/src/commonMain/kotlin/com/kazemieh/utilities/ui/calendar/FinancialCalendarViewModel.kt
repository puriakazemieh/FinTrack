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
import com.kazemieh.designsystem.CalendarSystem
import com.kazemieh.designsystem.gregorianMonthLength
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.jalali.JalaliCalendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

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
    val selectedDayTransactions: List<TransactionWithRelations> = emptyList(),
    val calendarSystem: CalendarSystem = CalendarSystem.JALALI,
) {
    val monthLabel: String get() = when (calendarSystem) {
        CalendarSystem.JALALI -> PersianDateTime(year, month, 1).persianMonth().displayName
        CalendarSystem.GREGORIAN -> month.toString().padStart(2, '0')
    }
}

sealed interface FinancialCalendarIntent {
    data object PreviousMonth : FinancialCalendarIntent
    data object NextMonth : FinancialCalendarIntent
    data class SelectDay(val day: Int) : FinancialCalendarIntent
    data class SetCalendarSystem(val calendarSystem: CalendarSystem) : FinancialCalendarIntent
    data object ClearSelection : FinancialCalendarIntent
}

class FinancialCalendarViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val observeTransactions: ObserveTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FinancialCalendarState())
    val state: StateFlow<FinancialCalendarState> = _state.asStateFlow()

    private var allMonthTransactions: List<TransactionWithRelations> = emptyList()
    private var monthLoadingJob: Job? = null

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("calendar"))
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
                val dayTx = allMonthTransactions.filter { item ->
                    dayForTimestamp(item.transaction.timeStamp, _state.value.calendarSystem) == intent.day
                }
                _state.value = _state.value.copy(selectedDay = intent.day, selectedDayTransactions = dayTx)
            }

            is FinancialCalendarIntent.SetCalendarSystem -> {
                if (intent.calendarSystem == _state.value.calendarSystem) return
                val currentJalali = JalaliCalendar.today()
                val isInitialMonth = _state.value.calendarSystem == CalendarSystem.JALALI &&
                    _state.value.year == currentJalali.year && _state.value.month == currentJalali.month &&
                    _state.value.selectedDay == null
                val timestamp = if (isInitialMonth) {
                    Clock.System.now().toEpochMilliseconds()
                } else {
                    monthStartTimestamp(
                        _state.value.year,
                        _state.value.month,
                        _state.value.calendarSystem,
                    )
                }
                val (year, month) = yearMonthForTimestamp(timestamp, intent.calendarSystem)
                _state.value = _state.value.copy(
                    year = year,
                    month = month,
                    calendarSystem = intent.calendarSystem,
                    selectedDay = null,
                    selectedDayTransactions = emptyList(),
                )
                loadMonth()
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
        val calendarSystem = _state.value.calendarSystem
        val monthLength = monthLength(year, month, calendarSystem)
        val from = monthStartTimestamp(year, month, calendarSystem)
        val to = monthStartTimestamp(year, month, calendarSystem, monthLength).plus(86_399_999)

        monthLoadingJob?.cancel()
        monthLoadingJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            observeTransactions(
                TransactionFilterParams(fromTimestamp = from, toTimestamp = to),
                PageRequest(limit = 2000, offset = 0)
            ).collect { page ->
                allMonthTransactions = page.items
                val totals = mutableMapOf<Int, DayTotal>()
                page.items.forEach { item ->
                    val day = dayForTimestamp(item.transaction.timeStamp, calendarSystem) ?: return@forEach
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

    private fun monthLength(year: Int, month: Int, calendarSystem: CalendarSystem): Int = when (calendarSystem) {
        CalendarSystem.JALALI -> PersianDateTime(year, month, 1).monthLength()
        CalendarSystem.GREGORIAN -> gregorianMonthLength(year, month)
    }

    private fun monthStartTimestamp(year: Int, month: Int, calendarSystem: CalendarSystem, day: Int = 1): Long =
        when (calendarSystem) {
            CalendarSystem.JALALI -> PersianDateTime(year, month, day, 0, 0, 0).toEpochMilliseconds()
            CalendarSystem.GREGORIAN -> LocalDate(year, month, day)
                .atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }

    private fun yearMonthForTimestamp(timestamp: Long, calendarSystem: CalendarSystem): Pair<Int, Int> = when (calendarSystem) {
        CalendarSystem.JALALI -> PersianDateTime.parse(timestamp).let { it.year to it.month }
        CalendarSystem.GREGORIAN -> Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date.let { it.year to it.monthNumber }
    }

    private fun dayForTimestamp(timestamp: Long, calendarSystem: CalendarSystem): Int? = runCatching {
        when (calendarSystem) {
            CalendarSystem.JALALI -> PersianDateTime.parse(timestamp).day
            CalendarSystem.GREGORIAN -> Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth
        }
    }.getOrNull()
}
