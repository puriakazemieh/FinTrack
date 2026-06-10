package com.kazemieh.budget.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.domain.usecase.DeleteBudgetUseCase
import com.kazemieh.domain.usecase.ObserveBudgetsWithProgressUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetState(
    val budgets: List<BudgetWithProgress> = emptyList(),
    val isLoading: Boolean = false,
    val isAddBudgetShow: Boolean = false,
    val selectedBudget: BudgetWithProgress? = null
)

sealed interface BudgetIntent {
    data object LoadBudgets : BudgetIntent
    data class DeleteBudget(val id: Long) : BudgetIntent
    data class ShowAddBudget(val budget: BudgetWithProgress? = null) : BudgetIntent
}

sealed interface BudgetEffect {
    data class ShowError(val message: String) : BudgetEffect
}

class BudgetViewModel(
    private val observeBudgetsWithProgressUseCase: ObserveBudgetsWithProgressUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<BudgetEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(BudgetIntent.LoadBudgets)
    }

    fun onIntent(intent: BudgetIntent) {
        when (intent) {
            BudgetIntent.LoadBudgets -> observeBudgets()
            is BudgetIntent.DeleteBudget -> deleteBudget(intent.id)
            is BudgetIntent.ShowAddBudget -> {
                _state.update { it.copy(isAddBudgetShow = !it.isAddBudgetShow, selectedBudget = intent.budget) }
            }
        }
    }

    private fun observeBudgets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            observeBudgetsWithProgressUseCase().collect { budgets ->
                _state.update { it.copy(budgets = budgets, isLoading = false) }
            }
        }
    }

    private fun deleteBudget(id: Long) {
        viewModelScope.launch {
            deleteBudgetUseCase(id)
        }
    }
}
