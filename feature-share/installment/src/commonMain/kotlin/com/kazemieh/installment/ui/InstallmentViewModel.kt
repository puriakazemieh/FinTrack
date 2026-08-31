package com.kazemieh.installment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.*
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.plus
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.installment_deleted
import fintrack.core.designsystem.generated.resources.payment
import fintrack.core.designsystem.generated.resources.transaction_failed
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
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

}

sealed interface InstallmentEffect {
    data class ShowMessage(val message: String) : InstallmentEffect
}

data class InstallmentState(
    val upcomingMonth: List<ScheduledInstallment> = emptyList(),
    val future: List<ScheduledInstallment> = emptyList(),
    val overdue: List<ScheduledInstallment> = emptyList(),
    val completed: List<ScheduledInstallment> = emptyList(),
    
    val filteredUpcomingMonth: List<ScheduledInstallment> = emptyList(),
    val filteredFuture: List<ScheduledInstallment> = emptyList(),
    val filteredOverdue: List<ScheduledInstallment> = emptyList(),
    val filteredCompleted: List<ScheduledInstallment> = emptyList(),
    
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val showFilterSheet: Boolean = false,

    val filterCategories: Set<Category> = emptySet(),
    val filterSources: Set<Source> = emptySet(),
    val filterTags: Set<Tag> = emptySet(),
    val filterPersons: Set<Person> = emptySet(),
    val totalAmount: Long = 0
)

/** A single payable occurrence generated from an installment plan. */
data class ScheduledInstallment(
    val installmentWithRelations: InstallmentWithRelations,
    val dueDate: Long,
    val installmentNumber: Int,
    val isPayable: Boolean
)

class InstallmentViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
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
            InstallmentIntent.Init -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("installment_list"))
                observeInstallments()
            }
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
        }
    }

    private fun observeInstallments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCases.observeInstallmentsUseCase()
                .combine(_searchQuery) { installments, query -> installments to query }
                .combine(_filterState) { (installments, query), filters ->
                    val now = Clock.System.now().toEpochMilliseconds()
                    
                    val filtered = installments.filter { item ->
                        val inst = item.installment
                        val inCategory = filters.categories.isEmpty() || filters.categories.any { it.id == inst.categoryId }
                        val inSource = filters.sources.isEmpty() || filters.sources.any { it.id == inst.sourceId }
                        val inTag = filters.tags.isEmpty() || item.tags.any { it.id in filters.tags.map { t -> t.id } }
                        val inPerson = filters.persons.isEmpty() || item.persons.any { it.id in filters.persons.map { p -> p.id } }
                        
                        inCategory && inSource && inTag && inPerson
                    }

                    val overdue = mutableListOf<ScheduledInstallment>()
                    val upcomingMonth = mutableListOf<ScheduledInstallment>()
                    val future = mutableListOf<ScheduledInstallment>()
                    val completed = mutableListOf<ScheduledInstallment>()

                    filtered.forEach { item ->
                        val installment = item.installment
                        val isSettled = installment.isCompleted ||
                                installment.paidInstallments >= installment.totalInstallments

                        if (isSettled) {
                            completed.add(
                                ScheduledInstallment(
                                    installmentWithRelations = item,
                                    dueDate = installment.nextDueDate,
                                    installmentNumber = installment.totalInstallments,
                                    isPayable = false
                                )
                            )
                        } else {
                            val schedule = buildSchedule(item)
                            val nearest = schedule.firstOrNull() ?: return@forEach
                            if (nearest.dueDate < now) overdue.add(nearest) else upcomingMonth.add(nearest)
                            future.addAll(schedule.drop(1))
                        }
                    }

                    fun List<ScheduledInstallment>.filterByQuery(q: String) = filter {
                        it.installmentWithRelations.installment.title.contains(q, ignoreCase = true) ||
                                it.installmentWithRelations.installment.description?.contains(q, ignoreCase = true) == true
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
                        .distinctBy { it.installmentWithRelations.installment.id }
                        .sumOf { it.installmentWithRelations.installment.totalAmount }
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
                analytics.track(com.kazemieh.common.analytics.ProductEvent.InstallmentPaid)
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
                analytics.track(com.kazemieh.common.analytics.ProductEvent.InstallmentDeleted)
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.installment_deleted)))
            } catch (e: Exception) {
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.transaction_failed)))
            }
        }
    }

    private fun buildSchedule(item: InstallmentWithRelations): List<ScheduledInstallment> {
        val installment = item.installment
        val remainingInstallments = (installment.totalInstallments - installment.paidInstallments).coerceAtLeast(0)
        var dueDate = installment.nextDueDate
        return List(remainingInstallments) { index ->
            val scheduledDueDate = dueDate
            dueDate = calculateNextDueDate(scheduledDueDate, installment.frequency)
            ScheduledInstallment(
                installmentWithRelations = item,
                dueDate = scheduledDueDate,
                installmentNumber = installment.paidInstallments + index + 1,
                isPayable = index == 0
            )
        }
    }

    private fun calculateNextDueDate(currentDueDate: Long, frequency: InstallmentFrequency): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val current = PersianDateTime.parse(currentDueDate, timeZone)
        val next = when (frequency) {
            InstallmentFrequency.DAILY -> current.plus(1, DateTimeUnit.DAY)
            InstallmentFrequency.WEEKLY -> current.plus(7, DateTimeUnit.DAY)
            InstallmentFrequency.MONTHLY -> current.plus(1, DateTimeUnit.MONTH)
            InstallmentFrequency.YEARLY -> current.plus(1, DateTimeUnit.YEAR)
        }
        return next.toEpochMilliseconds(timeZone)
    }
}

private data class FilterParams(
    val categories: Set<Category> = emptySet(),
    val sources: Set<Source> = emptySet(),
    val tags: Set<Tag> = emptySet(),
    val persons: Set<Person> = emptySet()
)

private data class InstallmentData(
    val overdue: List<ScheduledInstallment>,
    val upcomingMonth: List<ScheduledInstallment>,
    val future: List<ScheduledInstallment>,
    val completed: List<ScheduledInstallment>,
    val filteredOverdue: List<ScheduledInstallment>,
    val filteredUpcomingMonth: List<ScheduledInstallment>,
    val filteredFuture: List<ScheduledInstallment>,
    val filteredCompleted: List<ScheduledInstallment>
)
