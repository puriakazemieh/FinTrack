package com.kazemieh.installment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRange
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.Direction
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.installment_deleted
import fintrack.core.designsystem.generated.resources.payment
import fintrack.core.designsystem.generated.resources.transaction_failed
import kotlin.time.Clock

sealed interface InstallmentIntent {
    data object Init : InstallmentIntent
    data class UpdateSearchQuery(val query: String) : InstallmentIntent
    data class MarkAsPaid(
        val installmentId: Long,
        val transactionDescription: String,
        val reminderTitle: String,
        val reminderMessage: String
    ) : InstallmentIntent
    data class Delete(val installmentId: Long) : InstallmentIntent

    data object OnFilterClick : InstallmentIntent
    data object OnFilterSheetDismiss : InstallmentIntent
    data object OnFilterReset : InstallmentIntent
    data class OnFilterUpdate(
        val categories: Set<Category>,
        val sources: Set<Source>,
        val tags: Set<Tag>,
        val persons: Set<Person>
    ) : InstallmentIntent

    data class ChangeFilterType(val type: DateFilterType) : InstallmentIntent
    data class ShiftRange(val direction: Direction) : InstallmentIntent
}

sealed interface InstallmentEffect {
    data class ShowMessage(val message: String) : InstallmentEffect
}

data class InstallmentState(
    val upcomingMonth: List<InstallmentWithRelations> = emptyList(),
    val future: List<InstallmentWithRelations> = emptyList(),
    val overdue: List<InstallmentWithRelations> = emptyList(),
    val completed: List<InstallmentWithRelations> = emptyList(),
    
    val filteredUpcomingMonth: List<InstallmentWithRelations> = emptyList(),
    val filteredFuture: List<InstallmentWithRelations> = emptyList(),
    val filteredOverdue: List<InstallmentWithRelations> = emptyList(),
    val filteredCompleted: List<InstallmentWithRelations> = emptyList(),
    
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showFilterSheet: Boolean = false,

    val filterCategories: Set<Category> = emptySet(),
    val filterSources: Set<Source> = emptySet(),
    val filterTags: Set<Tag> = emptySet(),
    val filterPersons: Set<Person> = emptySet(),
    val dateRange: DateRange? = DateFilterHelper.getRange(DateFilterType.THIS_MONTH),
    val totalAmount: Long = 0
)

class InstallmentViewModel(
    private val useCases: InstallmentUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(InstallmentState())
    val state: StateFlow<InstallmentState> = _state.asStateFlow()

    private val _effects = Channel<InstallmentEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<InstallmentEffect> = _effects.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filterState = MutableStateFlow(FilterParams())

    fun onIntent(intent: InstallmentIntent) {
        when (intent) {
            InstallmentIntent.Init -> observeInstallments()
            is InstallmentIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is InstallmentIntent.MarkAsPaid -> markAsPaid(intent)
            is InstallmentIntent.Delete -> deleteInstallment(intent.installmentId)
            
            InstallmentIntent.OnFilterClick -> _state.update { it.copy(showFilterSheet = true) }
            InstallmentIntent.OnFilterSheetDismiss -> _state.update { it.copy(showFilterSheet = false) }
            InstallmentIntent.OnFilterReset -> {
                _filterState.update { FilterParams() }
                _state.update { it.copy(filterCategories = emptySet(), filterSources = emptySet(), filterTags = emptySet(), filterPersons = emptySet()) }
            }
            is InstallmentIntent.OnFilterUpdate -> {
                _filterState.update { it.copy(categories = intent.categories, sources = intent.sources, tags = intent.tags, persons = intent.persons) }
                _state.update { it.copy(filterCategories = intent.categories, filterSources = intent.sources, filterTags = intent.tags, filterPersons = intent.persons) }
            }
            is InstallmentIntent.ChangeFilterType -> {
                val range = DateFilterHelper.getRange(intent.type)
                _filterState.update { it.copy(dateRange = range) }
                _state.update { it.copy(dateRange = range) }
            }
            is InstallmentIntent.ShiftRange -> {
                val current = _filterState.value.dateRange ?: return
                val shifted = DateFilterHelper.shiftDateRange(current.start, current.end, current.filterType, intent.direction)
                _filterState.update { it.copy(dateRange = shifted) }
                _state.update { it.copy(dateRange = shifted) }
            }
        }
    }

    private fun observeInstallments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCases.observeInstallmentsUseCase()
                .combine(_searchQuery) { installments, query -> installments to query }
                .combine(_filterState) { (installments, query), filters ->
                    val now = Clock.System.now().toEpochMilliseconds()
                    val oneMonthFromNow = now + (30L * 24 * 60 * 60 * 1000)
                    
                    val filtered = installments.filter { item ->
                        val inst = item.installment
                        val inCategory = filters.categories.isEmpty() || filters.categories.any { it.id == inst.categoryId }
                        val inSource = filters.sources.isEmpty() || filters.sources.any { it.id == inst.sourceId }
                        val inTag = filters.tags.isEmpty() || item.tags.any { it.id in filters.tags.map { t -> t.id } }
                        val inPerson = filters.persons.isEmpty() || item.persons.any { it.id in filters.persons.map { p -> p.id } }
                        
                        val range = filters.dateRange
                        val inTimeRange = if (range != null) {
                            inst.nextDueDate >= range.start && inst.nextDueDate < range.end
                        } else true

                        inCategory && inSource && inTag && inPerson && inTimeRange
                    }

                    val overdue = mutableListOf<InstallmentWithRelations>()
                    val upcomingMonth = mutableListOf<InstallmentWithRelations>()
                    val future = mutableListOf<InstallmentWithRelations>()
                    val completed = mutableListOf<InstallmentWithRelations>()

                    filtered.forEach { item ->
                        if (item.installment.isCompleted) {
                            completed.add(item)
                        } else if (item.installment.nextDueDate < now) {
                            overdue.add(item)
                        } else if (item.installment.nextDueDate <= oneMonthFromNow) {
                            upcomingMonth.add(item)
                        } else {
                            future.add(item)
                        }
                    }

                    fun List<InstallmentWithRelations>.filterByQuery(q: String) = filter {
                        it.installment.title.contains(q, ignoreCase = true) ||
                                it.installment.description?.contains(q, ignoreCase = true) == true
                    }

                    InstallmentData(
                        overdue = overdue,
                        upcomingMonth = upcomingMonth,
                        future = future,
                        completed = completed,
                        filteredOverdue = overdue.filterByQuery(query),
                        filteredUpcomingMonth = upcomingMonth.filterByQuery(query),
                        filteredFuture = future.filterByQuery(query),
                        filteredCompleted = completed.filterByQuery(query)
                    )
                }
                .collectLatest { data ->
                    val total = (data.upcomingMonth + data.future + data.overdue + data.completed)
                        .sumOf { it.installment.installmentAmount }
                    _state.update {
                        it.copy(
                            upcomingMonth = data.upcomingMonth,
                            future = data.future,
                            overdue = data.overdue,
                            completed = data.completed,
                            filteredUpcomingMonth = data.filteredUpcomingMonth,
                            filteredFuture = data.filteredFuture,
                            filteredOverdue = data.filteredOverdue,
                            filteredCompleted = data.filteredCompleted,
                            isLoading = false,
                            searchQuery = _searchQuery.value,
                            totalAmount = total
                        )
                    }
                }
        }
    }

    private fun markAsPaid(intent: InstallmentIntent.MarkAsPaid) {
        viewModelScope.launch {
            try {
                useCases.markInstallmentAsPaidUseCase(
                    installmentId = intent.installmentId,
                    transactionDescription = intent.transactionDescription,
                    reminderTitle = intent.reminderTitle,
                    reminderMessage = intent.reminderMessage
                )
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.payment)))
            } catch (e: Exception) {
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.transaction_failed)))
            }
        }
    }

    private fun deleteInstallment(id: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteInstallmentUseCase(id)
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.installment_deleted)))
            } catch (e: Exception) {
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.transaction_failed)))
            }
        }
    }
}

private data class FilterParams(
    val categories: Set<Category> = emptySet(),
    val sources: Set<Source> = emptySet(),
    val tags: Set<Tag> = emptySet(),
    val persons: Set<Person> = emptySet(),
    val dateRange: DateRange? = DateFilterHelper.getRange(DateFilterType.THIS_MONTH)
)

private data class InstallmentData(
    val overdue: List<InstallmentWithRelations>,
    val upcomingMonth: List<InstallmentWithRelations>,
    val future: List<InstallmentWithRelations>,
    val completed: List<InstallmentWithRelations>,
    val filteredOverdue: List<InstallmentWithRelations>,
    val filteredUpcomingMonth: List<InstallmentWithRelations>,
    val filteredFuture: List<InstallmentWithRelations>,
    val filteredCompleted: List<InstallmentWithRelations>
)
