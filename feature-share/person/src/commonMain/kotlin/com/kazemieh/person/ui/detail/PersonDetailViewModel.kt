package com.kazemieh.person.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.repository.CheckRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.BudgetRepository
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.settle_debt_desc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class PersonDetailViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val personId: Long,
    private val observePersonsUseCase: ObservePersonsUseCase,
    private val debtUseCases: DebtUseCaseGroup,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val installmentRepository: InstallmentRepository,
    private val checkRepository: CheckRepository,
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PersonDetailState())
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("person_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observePersonsUseCase().collect { persons ->
                val person = persons.find { it.id == personId }
                _state.update { it.copy(person = person) }
            }
        }

        viewModelScope.launch {
            debtUseCases.observeDebtsByPersonUseCase(personId)
                .combine(_searchQuery) { debts, query ->
                    val filtered = debts.filter {
                        it.debt.description?.contains(query, ignoreCase = true) == true
                    }
                    debts to filtered
                }
                .collect { (all, filtered) ->
                    val totalCredits = all.filter { it.debt.type == DebtType.OWED_TO_ME && !it.debt.isSettled }
                        .sumOf { it.debt.amount }
                    val totalDebts = all.filter { it.debt.type == DebtType.OWED_BY_ME && !it.debt.isSettled }
                        .sumOf { it.debt.amount }

                    _state.update {
                        it.copy(
                            debts = all,
                            filteredDebts = filtered,
                            totalCredits = totalCredits,
                            totalDebts = totalDebts,
                            balance = totalCredits - totalDebts,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }

        viewModelScope.launch {
            installmentRepository.observeInstallments().collect { installments ->
                _state.update {
                    it.copy(installments = installments.filter { item ->
                        item.persons.any { person -> person.id == personId }
                    })
                }
            }
        }

        viewModelScope.launch {
            checkRepository.observeAllChecks().collect { checks ->
                _state.update { it.copy(checks = checks.filter { check -> check.personId == personId }) }
            }
        }

        viewModelScope.launch {
            fixedExpenseRepository.observeFixedExpensesFiltered(
                query = null,
                categoryIds = emptyList(),
                sourceIds = emptyList(),
                tagIds = emptyList(),
                personIds = listOf(personId)
            ).collect { expenses ->
                _state.update { it.copy(fixedExpenses = expenses) }
            }
        }

        viewModelScope.launch {
            budgetRepository.observeBudgetsWithProgress(0L, Long.MAX_VALUE).collect { budgets ->
                _state.update { it.copy(budgets = budgets.filter { it.budget.personIds?.contains(personId) == true }) }
            }
        }

        viewModelScope.launch {
            val filter = TransactionFilterParams(
                persons = setOf(Person(id = personId, name = "")),
                isAllPersons = false
            )
            observeTransactionsUseCase(filter, PageRequest(limit = Int.MAX_VALUE, offset = 0))
                .map { it.items }
                .combine(_searchQuery) { transactions, query ->
                    if (query.isBlank()) {
                        transactions
                    } else {
                        transactions.filter {
                            it.transaction.description?.contains(query, ignoreCase = true) == true ||
                                it.category?.name?.contains(query, ignoreCase = true) == true
                        }
                    }
                }
                .collect { filtered ->
                    _state.update { it.copy(transactions = filtered) }
                }
        }
    }

    fun onIntent(intent: PersonDetailIntent) {
        when (intent) {
            is PersonDetailIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is PersonDetailIntent.SettleDebt -> settleDebt(intent.debtId, intent.description)
            is PersonDetailIntent.DeleteDebt -> deleteDebt(intent.debtId)
        }
    }

    private fun settleDebt(debtId: Long, description: String) {
        viewModelScope.launch {
            val finalDesc = description.ifEmpty { getString(Res.string.settle_debt_desc, state.value.person?.name ?: "") }
            debtUseCases.settleDebtUseCase(debtId, finalDesc)
        }
    }

    private fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            debtUseCases.deleteDebtUseCase(debtId)
        }
    }
}

data class PersonDetailState(
    val person: Person? = null,
    val debts: List<DebtWithRelations> = emptyList(),
    val filteredDebts: List<DebtWithRelations> = emptyList(),
    val transactions: List<TransactionWithRelations> = emptyList(),
    val installments: List<com.kazemieh.common.model.InstallmentWithRelations> = emptyList(),
    val checks: List<com.kazemieh.common.model.Check> = emptyList(),
    val fixedExpenses: List<com.kazemieh.common.model.FixedExpense> = emptyList(),
    val budgets: List<BudgetWithProgress> = emptyList(),
    val totalCredits: Long = 0,
    val totalDebts: Long = 0,
    val balance: Long = 0,
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface PersonDetailIntent {
    data class UpdateSearchQuery(val query: String) : PersonDetailIntent
    data class SettleDebt(val debtId: Long, val description: String) : PersonDetailIntent
    data class DeleteDebt(val debtId: Long) : PersonDetailIntent
}
