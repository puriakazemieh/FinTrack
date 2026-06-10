package com.kazemieh.budget.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.usecase.AddBudgetUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.UpdateBudgetUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddBudgetState(
    val id: Long? = null,
    val selectedCategory: Category? = null,
    val amount: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startAt: Long = 0, // Should be today's timestamp
    val isAlertEnabled: Boolean = true,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface AddBudgetIntent {
    data class SelectCategory(val category: Category) : AddBudgetIntent
    data class UpdateAmount(val amount: String) : AddBudgetIntent
    data class UpdatePeriod(val period: BudgetPeriod) : AddBudgetIntent
    data class UpdateAlert(val isEnabled: Boolean) : AddBudgetIntent
    data object SaveBudget : AddBudgetIntent
    data class LoadCategories(val type: TransactionType = TransactionType.EXPENSE) : AddBudgetIntent
    data class InitialData(val budget: Budget?, val category: Category?) : AddBudgetIntent
}

sealed interface AddBudgetEffect {
    data object BudgetSaved : AddBudgetEffect
    data class ShowError(val message: String) : AddBudgetEffect
}

class AddBudgetViewModel(
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddBudgetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddBudgetEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddBudgetIntent) {
        when (intent) {
            is AddBudgetIntent.SelectCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            is AddBudgetIntent.UpdateAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddBudgetIntent.UpdatePeriod -> _state.update { it.copy(period = intent.period) }
            is AddBudgetIntent.UpdateAlert -> _state.update { it.copy(isAlertEnabled = intent.isEnabled) }
            AddBudgetIntent.SaveBudget -> saveBudget()
            is AddBudgetIntent.LoadCategories -> loadCategories(intent.type)
            is AddBudgetIntent.InitialData -> {
                intent.budget?.let { b ->
                    _state.update {
                        it.copy(
                            id = b.id,
                            selectedCategory = intent.category,
                            amount = b.amount.toString(),
                            period = b.period,
                            startAt = b.startAt,
                            isAlertEnabled = b.isAlertEnabled
                        )
                    }
                }
            }
        }
    }

    private fun loadCategories(type: TransactionType) {
        viewModelScope.launch {
            observeCategoriesUseCase(type).collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun saveBudget() {
        val currentState = _state.value
        val amountLong = currentState.amount.toLongOrNull() ?: 0L
        val categoryId = currentState.selectedCategory?.id ?: return

        viewModelScope.launch {
            val budget = Budget(
                id = currentState.id,
                categoryId = categoryId,
                amount = amountLong,
                period = currentState.period,
                startAt = currentState.startAt,
                isAlertEnabled = currentState.isAlertEnabled
            )

            if (budget.id == null) {
                addBudgetUseCase(budget)
            } else {
                updateBudgetUseCase(budget)
            }
            _effect.send(AddBudgetEffect.BudgetSaved)
        }
    }
}
