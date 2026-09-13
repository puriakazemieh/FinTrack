package com.kazemieh.financialsource.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.ProductEvent
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.ObserveSourceUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.repository.CheckRepository
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SourceDetailState(
    val source: Source? = null,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val debts: List<DebtWithRelations> = emptyList(),
    val installments: List<InstallmentWithRelations> = emptyList(),
    val checks: List<Check> = emptyList(),
    val fixedExpenses: List<FixedExpense> = emptyList(),
    val budgets: List<BudgetWithProgress> = emptyList(),
    val isLoading: Boolean = false
)

class SourceDetailViewModel(
    private val analytics: AnalyticsService,
    private val sourceId: Long,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val observeSourceUseCase: ObserveSourceUseCase,
    private val debtRepository: DebtRepository,
    private val installmentRepository: InstallmentRepository,
    private val checkRepository: CheckRepository,
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SourceDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        analytics.track(ProductEvent.FeatureOpened("source_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observeSourceUseCase(sourceId).collect { source ->
                _state.update { it.copy(source = source) }
            }
        }

        viewModelScope.launch {
            debtRepository.observeAllDebts().collect { debts ->
                _state.update { it.copy(debts = debts.filter { debt -> debt.debt.sourceId == sourceId }) }
            }
        }

        viewModelScope.launch {
            installmentRepository.observeInstallments().collect { installments ->
                _state.update { it.copy(installments = installments.filter { it.installment.sourceId == sourceId }) }
            }
        }

        viewModelScope.launch {
            checkRepository.observeAllChecks().collect { checks ->
                _state.update { it.copy(checks = checks.filter { it.sourceId == sourceId }) }
            }
        }

        viewModelScope.launch {
            fixedExpenseRepository.observeFixedExpensesFiltered(
                query = null,
                categoryIds = emptyList(),
                sourceIds = listOf(sourceId),
                tagIds = emptyList(),
                personIds = emptyList()
            ).collect { expenses ->
                _state.update { it.copy(fixedExpenses = expenses) }
            }
        }

        viewModelScope.launch {
            budgetRepository.observeBudgetsWithProgress(0L, Long.MAX_VALUE).collect { budgets ->
                _state.update { it.copy(budgets = budgets.filter { it.budget.sourceId == sourceId }) }
            }
        }

        viewModelScope.launch {
            observeTransactionsUseCase(
                TransactionFilterParams(
                    sources = setOf(Source(id = sourceId, name = "", colorId = 1, iconId = 1)),
                    isAllSources = false
                ),
                PageRequest(limit = Int.MAX_VALUE, offset = 0)
            ).collect { page ->
                _state.update { it.copy(transactions = page.items, isLoading = false) }
            }
        }
    }
}
