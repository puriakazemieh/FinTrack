package com.kazemieh.tag.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Note
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.repository.NoteRepository
import com.kazemieh.domain.repository.ShoppingRepository
import com.kazemieh.domain.repository.CheckRepository
import com.kazemieh.domain.repository.DebtRepository
import com.kazemieh.domain.repository.FixedExpenseRepository
import com.kazemieh.domain.repository.InstallmentRepository
import com.kazemieh.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TagDetailState(
    val tag: Tag? = null,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val notes: List<Note> = emptyList(),
    val shoppingItems: List<com.kazemieh.common.model.ShoppingItem> = emptyList(),
    val debts: List<DebtWithRelations> = emptyList(),
    val installments: List<InstallmentWithRelations> = emptyList(),
    val checks: List<Check> = emptyList(),
    val fixedExpenses: List<FixedExpense> = emptyList(),
    val budgets: List<BudgetWithProgress> = emptyList(),
    val isLoading: Boolean = false
)

class TagDetailViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val tagId: Long,
    private val observeTagsUseCase: ObserveTagsUseCase,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val noteRepository: NoteRepository,
    private val shoppingRepository: ShoppingRepository,
    private val debtRepository: DebtRepository,
    private val installmentRepository: InstallmentRepository,
    private val checkRepository: CheckRepository,
    private val fixedExpenseRepository: FixedExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TagDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("tag_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observeTagsUseCase().collect { tags ->
                val tag = tags.find { it.id == tagId }
                _state.update { it.copy(tag = tag) }
            }
        }

        viewModelScope.launch {
            observeTransactionsUseCase(
                TransactionFilterParams(tags = setOf(Tag(id = tagId, name = "", colorId = 1, iconId = 1)), isAllTags = false),
                PageRequest(limit = Int.MAX_VALUE, offset = 0)
            ).collect { page ->
                _state.update { it.copy(transactions = page.items, isLoading = false) }
            }
        }

        viewModelScope.launch {
            noteRepository.observeNotes().collect { allNotes ->
                val related = allNotes.filter { note -> note.tags.any { it.id == tagId } }
                _state.update { it.copy(notes = related) }
            }
        }

        viewModelScope.launch {
            shoppingRepository.observeShoppingItems(tagIds = listOf(tagId)).collect { items ->
                _state.update { it.copy(shoppingItems = items) }
            }
        }

        viewModelScope.launch {
            debtRepository.observeAllDebts().collect { debts ->
                _state.update { it.copy(debts = debts.filter { debt -> debt.tags.any { it.id == tagId } }) }
            }
        }

        viewModelScope.launch {
            installmentRepository.observeInstallments().collect { installments ->
                _state.update { it.copy(installments = installments.filter { installment -> installment.tags.any { it.id == tagId } }) }
            }
        }

        viewModelScope.launch {
            checkRepository.observeAllChecks().collect { checks ->
                _state.update { it.copy(checks = checks.filter { check -> check.tagIds?.contains(tagId) == true }) }
            }
        }

        viewModelScope.launch {
            fixedExpenseRepository.observeFixedExpensesFiltered(
                query = null,
                categoryIds = emptyList(),
                sourceIds = emptyList(),
                tagIds = listOf(tagId),
                personIds = emptyList()
            ).collect { expenses ->
                _state.update { it.copy(fixedExpenses = expenses) }
            }
        }

        viewModelScope.launch {
            budgetRepository.observeBudgetsWithProgress(0L, Long.MAX_VALUE).collect { budgets ->
                _state.update { it.copy(budgets = budgets.filter { it.budget.tagIds?.contains(tagId) == true }) }
            }
        }
    }
}

