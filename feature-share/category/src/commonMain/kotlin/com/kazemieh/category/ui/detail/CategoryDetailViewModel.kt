package com.kazemieh.category.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.ProductEvent
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.repository.CheckRepository
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.ShoppingRepository
import com.kazemieh.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryDetailState(
    val category: Category? = null,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val debts: List<DebtWithRelations> = emptyList(),
    val installments: List<InstallmentWithRelations> = emptyList(),
    val checks: List<Check> = emptyList(),
    val fixedExpenses: List<FixedExpense> = emptyList(),
    val shoppingItems: List<com.kazemieh.common.model.ShoppingItem> = emptyList(),
    val budgets: List<BudgetWithProgress> = emptyList(),
    val isLoading: Boolean = false
)

class CategoryDetailViewModel(
    private val analytics: AnalyticsService,
    private val categoryId: Long,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val debtRepository: DebtRepository,
    private val installmentRepository: InstallmentRepository,
    private val checkRepository: CheckRepository,
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val shoppingRepository: ShoppingRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        analytics.track(ProductEvent.FeatureOpened("category_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val category = getCategoryUseCase(categoryId)
            _state.update { it.copy(category = category) }
        }

        viewModelScope.launch {
            debtRepository.observeAllDebts().collect { debts ->
                _state.update { it.copy(debts = debts.filter { debt -> debt.debt.categoryId == categoryId }) }
            }
        }

        viewModelScope.launch {
            installmentRepository.observeInstallments().collect { installments ->
                _state.update { it.copy(installments = installments.filter { it.installment.categoryId == categoryId }) }
            }
        }

        viewModelScope.launch {
            checkRepository.observeAllChecks().collect { checks ->
                _state.update { it.copy(checks = checks.filter { it.categoryId == categoryId }) }
            }
        }

        viewModelScope.launch {
            fixedExpenseRepository.observeFixedExpensesFiltered(
                query = null,
                categoryIds = listOf(categoryId),
                sourceIds = emptyList(),
                tagIds = emptyList(),
                personIds = emptyList()
            ).collect { expenses ->
                _state.update { it.copy(fixedExpenses = expenses) }
            }
        }

        viewModelScope.launch {
            shoppingRepository.observeShoppingItems(categoryIds = listOf(categoryId)).collect { items ->
                _state.update { it.copy(shoppingItems = items) }
            }
        }

        viewModelScope.launch {
            budgetRepository.observeBudgetsWithProgress(0L, Long.MAX_VALUE).collect { budgets ->
                _state.update { it.copy(budgets = budgets.filter { it.budget.categoryId == categoryId }) }
            }
        }

        viewModelScope.launch {
            observeTransactionsUseCase(
                TransactionFilterParams(
                    categories = setOf(Category(id = categoryId, name = "", colorId = 1, iconId = 1)),
                    isAllCategories = false
                ),
                PageRequest(limit = Int.MAX_VALUE, offset = 0)
            ).collect { page ->
                _state.update { it.copy(transactions = page.items, isLoading = false) }
            }
        }
    }
}
