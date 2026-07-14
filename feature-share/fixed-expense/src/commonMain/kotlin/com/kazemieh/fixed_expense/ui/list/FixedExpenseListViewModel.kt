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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class FixedExpenseListViewModel(
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(FixedExpenseListState())
    val state: StateFlow<FixedExpenseListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow(_state.value.dateRange)

    init {
        observeExpenses()
    }

    fun onIntent(intent: FixedExpenseListIntent) {
        when (intent) {
            is FixedExpenseListIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is FixedExpenseListIntent.ToggleActive -> toggleActive(intent.expenseId)
            is FixedExpenseListIntent.OnDeleteClick -> _state.update {
                it.copy(selectedExpense = intent.expense, isDeleteShow = intent.expense != null)
            }
            is FixedExpenseListIntent.ConfirmDelete -> confirmDelete()
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
        }
    }

    private fun confirmDelete() {
        val expense = _state.value.selectedExpense ?: return
        viewModelScope.launch {
            fixedExpenseUseCases.deleteFixedExpenseUseCase(expense.id)
            _state.update { it.copy(isDeleteShow = false, selectedExpense = null) }
        }
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            _dateRange.flatMapLatest { range ->
                combine(
                    fixedExpenseUseCases.observeAllFixedExpensesUseCase(),
                    _searchQuery
                ) { expenses, query ->
                    val filteredByQuery = expenses.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.categoryName?.contains(query, ignoreCase = true) == true ||
                                it.description?.contains(query, ignoreCase = true) == true
                    }

                    val filteredByRange = filteredByQuery.filter {
                        it.recurrence == RecurrenceType.NONE || (range != null && it.nextDueDate in range.start..range.end)
                    }
                    expenses to filteredByRange
                }
            }.collect { (all, filtered) ->
                _state.update {
                    it.copy(
                        expenses = all,
                        filteredExpenses = filtered,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
    }

    private fun toggleActive(expenseId: Long) {
        viewModelScope.launch {
            val expense = _state.value.expenses.find { it.id == expenseId }
            expense?.let {
                fixedExpenseUseCases.updateFixedExpenseUseCase(it.copy(isActive = !it.isActive))
            }
        }
    }

}

data class FixedExpenseListState(
    val expenses: List<FixedExpense> = emptyList(),
    val filteredExpenses: List<FixedExpense> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isDeleteShow: Boolean = false,
    val selectedExpense: FixedExpense? = null,
    val dateRange: DateRange? = DateFilterHelper.getRange(DateFilterType.THIS_MONTH)
)

sealed interface FixedExpenseListIntent {
    data class UpdateSearchQuery(val query: String) : FixedExpenseListIntent
    data class ToggleActive(val expenseId: Long) : FixedExpenseListIntent
    data class OnDeleteClick(val expense: FixedExpense? = null) : FixedExpenseListIntent
    data object ConfirmDelete : FixedExpenseListIntent
    data class ChangeFilterType(val type: DateFilterType) : FixedExpenseListIntent
    data class ShiftRange(val direction: Direction) : FixedExpenseListIntent
}
