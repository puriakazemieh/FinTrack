package com.kazemieh.fixed_expense.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRange
import com.kazemieh.common.Direction
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FixedExpenseListViewModel(
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(FixedExpenseListState())
    val state: StateFlow<FixedExpenseListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow(DateFilterHelper.getRange(DateFilterType.THIS_MONTH))

    init {
        observeExpenses()
    }

    fun onIntent(intent: FixedExpenseListIntent) {
        when (intent) {
            is FixedExpenseListIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is FixedExpenseListIntent.ChangeFilterType -> {
                val newRange = DateFilterHelper.getRange(intent.type)
                _dateRange.value = newRange
                _state.update { it.copy(dateRange = newRange) }
            }
            is FixedExpenseListIntent.ShiftRange -> {
                val current = _dateRange.value ?: return
                val newRange = DateFilterHelper.shiftDateRange(current.start, current.end, current.filterType, intent.direction)
                _dateRange.value = newRange
                _state.update { it.copy(dateRange = newRange) }
            }
            is FixedExpenseListIntent.ToggleActive -> toggleActive(intent.expenseId)
            is FixedExpenseListIntent.OnDeleteClick -> _state.update {
                it.copy(selectedExpense = intent.expense, isDeleteShow = intent.expense != null)
            }
            is FixedExpenseListIntent.ConfirmDelete -> confirmDelete()
            is FixedExpenseListIntent.CloneOnceFromPrevious -> cloneOnceFromPrevious()
        }
    }

    // Non-recurring (ONCE) expenses don't auto-repeat, so offer to copy the most recent earlier
    // one into the current period — the fixed-expense parallel to the budget clone.
    private fun cloneOnceFromPrevious() {
        val range = _state.value.dateRange ?: return
        viewModelScope.launch {
            val latest = _state.value.expenses
                .filter { it.recurrence == RecurrenceType.ONCE && it.startDate < range.start }
                .maxByOrNull { it.startDate } ?: return@launch
            val clone = latest.copy(
                id = 0L,
                startDate = range.start,
                nextDueDate = range.start,
                updatedAt = 0
            )
            fixedExpenseUseCases.addFixedExpenseUseCase(clone, "", "")
        }
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            combine(
                fixedExpenseUseCases.observeAllFixedExpensesUseCase(),
                _searchQuery,
                _dateRange
            ) { all, query, range ->
                // Mirror budgets: keep only expenses whose start date falls in the selected range.
                val inRange = all.filter { range == null || it.startDate in range.start..range.end }
                val filtered = inRange.filter {
                    it.categoryName?.contains(query, ignoreCase = true) == true ||
                            it.description?.contains(query, ignoreCase = true) == true ||
                            query.isBlank()
                }
                val grouped = filtered.groupBy { it.recurrence }
                val total = filtered.filter { it.isActive }.sumOf { it.amount }
                Triple(all, grouped, total)
            }.collect { (all, grouped, total) ->
                val range = _dateRange.value
                val canCloneOnce = grouped[RecurrenceType.ONCE].isNullOrEmpty() &&
                        all.any { it.recurrence == RecurrenceType.ONCE && (range == null || it.startDate < range.start) }
                _state.update {
                    it.copy(
                        expenses = all,
                        grouped = grouped,
                        totalApprox = total,
                        canCloneOnce = canCloneOnce,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
    }

    private fun toggleActive(expenseId: Long) {
        viewModelScope.launch {
            val expense = _state.value.grouped.values.flatten().find { it.id == expenseId } ?: return@launch
            fixedExpenseUseCases.updateFixedExpenseUseCase(expense.copy(isActive = !expense.isActive))
        }
    }

    private fun confirmDelete() {
        val expense = _state.value.selectedExpense ?: return
        viewModelScope.launch {
            fixedExpenseUseCases.deleteFixedExpenseUseCase(expense.id)
            _state.update { it.copy(isDeleteShow = false, selectedExpense = null) }
        }
    }
}

data class FixedExpenseListState(
    // Full, unfiltered list (used by the dashboard widget).
    val expenses: List<FixedExpense> = emptyList(),
    val grouped: Map<RecurrenceType, List<FixedExpense>> = emptyMap(),
    val dateRange: DateRange? = DateFilterHelper.getRange(DateFilterType.THIS_MONTH),
    val searchQuery: String = "",
    val totalApprox: Long = 0,
    val canCloneOnce: Boolean = false,
    val isDeleteShow: Boolean = false,
    val selectedExpense: FixedExpense? = null,
)

sealed interface FixedExpenseListIntent {
    data class UpdateSearchQuery(val query: String) : FixedExpenseListIntent
    data class ChangeFilterType(val type: DateFilterType) : FixedExpenseListIntent
    data class ShiftRange(val direction: Direction) : FixedExpenseListIntent
    data class ToggleActive(val expenseId: Long) : FixedExpenseListIntent
    data class OnDeleteClick(val expense: FixedExpense? = null) : FixedExpenseListIntent
    data object ConfirmDelete : FixedExpenseListIntent
    data object CloneOnceFromPrevious : FixedExpenseListIntent
}
