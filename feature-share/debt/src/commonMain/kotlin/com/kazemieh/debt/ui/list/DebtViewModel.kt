package com.kazemieh.debt.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DebtViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val debtUseCases: DebtUseCaseGroup
) : ViewModel() {

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.DebtListViewed)
    }

    private val _state = MutableStateFlow(DebtState())
    val state: StateFlow<DebtState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterState = MutableStateFlow(FilterParams())

    fun onIntent(intent: DebtIntent) {
        when (intent) {
            is DebtIntent.ObserveAllDebts -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("debt_list"))
                observeAllDebts()
            }
            is DebtIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is DebtIntent.ObserveDebtsByPerson -> observeDebtsByPerson(intent.personId)
            is DebtIntent.SettleDebt -> settleDebt(intent.debtId, intent.postAsTransaction)
            is DebtIntent.DeleteDebt -> deleteDebt(intent.debtId)
            
            DebtIntent.OnFilterClick -> _state.update { it.copy(showFilterSheet = true) }
            DebtIntent.OnFilterSheetDismiss -> _state.update { it.copy(showFilterSheet = false) }
            DebtIntent.OnFilterReset -> {
                _filterState.update { FilterParams() }
                _state.update { it.copy(filterCategories = emptySet(), filterSources = emptySet(), filterTags = emptySet(), filterPersons = emptySet()) }
            }
            is DebtIntent.OnFilterUpdate -> {
                _filterState.update { it.copy(categories = intent.categories, sources = intent.sources, tags = intent.tags, persons = intent.persons) }
                _state.update { it.copy(filterCategories = intent.categories, filterSources = intent.sources, filterTags = intent.tags, filterPersons = intent.persons) }
            }
        }
    }

    private fun observeAllDebts() {
        viewModelScope.launch {
            debtUseCases.observeDebtsUseCase()
                .combine(_searchQuery) { debts, query -> debts to query }
                .combine(_filterState) { (debts, query), filters ->
                    val filtered = debts.filter { item ->
                        val inCategory = filters.categories.isEmpty() || filters.categories.any { it.id == item.debt.categoryId }
                        val inSource = filters.sources.isEmpty() || filters.sources.any { it.id == item.debt.sourceId }
                        val inTag = filters.tags.isEmpty() || item.tags.any { it.id in filters.tags.map { t -> t.id } }
                        val inPerson = filters.persons.isEmpty() || filters.persons.any { it.id == item.person.id }
                        
                        val matchesQuery = item.person.name.contains(query, ignoreCase = true) ||
                                item.debt.description?.contains(query, ignoreCase = true) == true
                        
                        inCategory && inSource && inTag && inPerson && matchesQuery
                    }
                    debts to filtered
                }
                .collect { (all, filtered) ->
                    val totalCredits = all.filter { it.debt.type == DebtType.OWED_TO_ME && !it.debt.isSettled }.sumOf { it.debt.amount }
                    val totalDebts = all.filter { it.debt.type == DebtType.OWED_BY_ME && !it.debt.isSettled }.sumOf { it.debt.amount }
                    
                    _state.update {
                        it.copy(
                            debts = all,
                            filteredDebts = filtered,
                            searchQuery = _searchQuery.value,
                            totalCredits = totalCredits,
                            totalDebts = totalDebts
                        )
                    }
                }
        }
    }

    private fun observeDebtsByPerson(personId: Long) {
        viewModelScope.launch {
            debtUseCases.observeDebtsByPersonUseCase(personId).collect { debts ->
                _state.update { it.copy(debts = debts) }
            }
        }
    }

    private fun settleDebt(debtId: Long, postAsTransaction: Boolean) {
        viewModelScope.launch {
            debtUseCases.settleDebtUseCase(
                debtId = debtId,
                description = "",
                postAsTransaction = postAsTransaction
            )
            analytics.track(com.kazemieh.common.analytics.ProductEvent.DebtSettled)
        }
    }

    private fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            debtUseCases.deleteDebtUseCase(debtId)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.DebtDeleted)
        }
    }
}

data class DebtState(
    val debts: List<DebtWithRelations> = emptyList(),
    val filteredDebts: List<DebtWithRelations> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val totalCredits: Long = 0,
    val totalDebts: Long = 0,
    val showFilterSheet: Boolean = false,
    val filterCategories: Set<Category> = emptySet(),
    val filterSources: Set<Source> = emptySet(),
    val filterTags: Set<Tag> = emptySet(),
    val filterPersons: Set<Person> = emptySet()
)

sealed interface DebtIntent {
    data object ObserveAllDebts : DebtIntent
    data class UpdateSearchQuery(val query: String) : DebtIntent
    data class ObserveDebtsByPerson(val personId: Long) : DebtIntent
    data class SettleDebt(val debtId: Long, val postAsTransaction: Boolean) : DebtIntent
    data class DeleteDebt(val debtId: Long) : DebtIntent
    
    data object OnFilterClick : DebtIntent
    data object OnFilterSheetDismiss : DebtIntent
    data object OnFilterReset : DebtIntent
    data class OnFilterUpdate(
        val categories: Set<Category>,
        val sources: Set<Source>,
        val tags: Set<Tag>,
        val persons: Set<Person>
    ) : DebtIntent
}

private data class FilterParams(
    val categories: Set<Category> = emptySet(),
    val sources: Set<Source> = emptySet(),
    val tags: Set<Tag> = emptySet(),
    val persons: Set<Person> = emptySet()
)
