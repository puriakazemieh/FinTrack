package com.kazemieh.fixed_expense.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FixedExpenseListViewModel(
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(FixedExpenseListState())
    val state: StateFlow<FixedExpenseListState> = _state.asStateFlow()

    init {
        observeExpenses()
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            fixedExpenseUseCases.observeAllFixedExpensesUseCase().collect { expenses ->
                _state.update { it.copy(expenses = expenses) }
            }
        }
    }

    fun onIntent(intent: FixedExpenseListIntent) {
        when (intent) {
            is FixedExpenseListIntent.ToggleActive -> toggleActive(intent.expenseId)
            is FixedExpenseListIntent.DeleteExpense -> deleteExpense(intent.expenseId)
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

    private fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            fixedExpenseUseCases.deleteFixedExpenseUseCase(expenseId)
        }
    }
}

data class FixedExpenseListState(
    val expenses: List<FixedExpense> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface FixedExpenseListIntent {
    data class ToggleActive(val expenseId: Long) : FixedExpenseListIntent
    data class DeleteExpense(val expenseId: Long) : FixedExpenseListIntent
}
