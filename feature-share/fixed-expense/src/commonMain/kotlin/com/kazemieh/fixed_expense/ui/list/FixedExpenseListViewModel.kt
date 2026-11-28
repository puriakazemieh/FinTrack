package com.kazemieh.fixed_expense.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRange
import com.kazemieh.common.Direction
import com.kazemieh.common.model.*
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.FixedExpensePostingStatus
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FixedExpenseListViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(FixedExpenseListState())
    val state: StateFlow<FixedExpenseListState> = _state.asStateFlow()

    private val _effect = Channel<FixedExpenseListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow(DateFilterHelper.getRange(DateFilterType.THIS_MONTH))
    private val _filterCategories = MutableStateFlow<Set<Category>>(emptySet())
    private val _filterSources = MutableStateFlow<Set<Source>>(emptySet())
    private val _filterTags = MutableStateFlow<Set<Tag>>(emptySet())
    private val _filterPersons = MutableStateFlow<Set<Person>>(emptySet())

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FixedExpenseListViewed)
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("fixed_expense_list"))
        observeExpenses()
        viewModelScope.launch {
            fixedExpenseUseCases.observeFixedExpensePostingStatusUseCase()
                .catch { error ->
                    if (error is CancellationException) throw error
                    _state.update { it.copy(postingStatusFailed = true) }
                }.collect { statuses ->
                    _state.update { it.copy(postingStatuses = statuses, postingStatusFailed = false) }
                }
        }
    }

    fun onIntent(intent: FixedExpenseListIntent) {
        when (intent) {
            is FixedExpenseListIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is FixedExpenseListIntent.ChangeFilterType -> {
                val newRange = DateFilterHelper.getRange(intent.type)
                _dateRange.value = newRange
                _state.update { it.copy(dateRange = newRange) }
            }
            is FixedExpenseListIntent.ShiftRange -> {
                val current = _dateRange.value ?: return
                val newRange = DateFilterHelper.shiftDateRange(current.start, current.end, current.filterType, intent.direction)
                _dateRange.value = newRange
                _state.update { it.copy(dateRange = newRange) }
            }
            is FixedExpenseListIntent.ToggleActive -> toggleActive(intent.expenseId)
            is FixedExpenseListIntent.RegisterAsTransaction -> registerAsTransaction(intent.expense)
            FixedExpenseListIntent.ConfirmRepeat -> _state.value.repeatConfirmation?.let { post(it) }
            FixedExpenseListIntent.DismissRepeat -> {
                if (_state.value.postingIds.isEmpty()) _state.update { it.copy(repeatConfirmation = null) }
            }
            is FixedExpenseListIntent.OnDeleteClick -> _state.update {
                it.copy(selectedExpense = intent.expense, isDeleteShow = intent.expense != null)
            }
            is FixedExpenseListIntent.ConfirmDelete -> confirmDelete()
            is FixedExpenseListIntent.CloneOnceFromPrevious -> cloneOnceFromPrevious()
            FixedExpenseListIntent.OnFilterClick -> _state.update { it.copy(showFilterSheet = true) }
            FixedExpenseListIntent.OnFilterSheetDismiss -> _state.update { it.copy(showFilterSheet = false) }
            FixedExpenseListIntent.OnFilterReset -> {
                _filterCategories.value = emptySet()
                _filterSources.value = emptySet()
                _filterTags.value = emptySet()
                _filterPersons.value = emptySet()
            }
            is FixedExpenseListIntent.OnFilterUpdate -> {
                _filterCategories.value = intent.categories
                _filterSources.value = intent.sources
                _filterTags.value = intent.tags
                _filterPersons.value = intent.persons
            }
        }
    }

    // Non-recurring (ONCE) expenses don't auto-repeat, so offer to copy the most recent earlier
    // one into the current period — the fixed-expense parallel to the budget clone.
    private fun cloneOnceFromPrevious() {
        val range = _state.value.dateRange ?: return
        viewModelScope.launch {
            val latest = _state.value.expenses
                .filter { it.recurrence == RecurrenceType.ONCE && it.startDate < range.start }
                .maxByOrNull { it.startDate } ?: return@launch
            val clone = latest.copy(
                id = 0L,
                startDate = range.start,
                nextDueDate = range.start,
                updatedAt = 0
            )
            fixedExpenseUseCases.addFixedExpenseUseCase(clone, "", "")
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeExpenses() {
        viewModelScope.launch {
            combine(
                _filterCategories,
                _filterSources,
                _filterTags,
                _filterPersons,
                _searchQuery,
                _dateRange
            ) { args ->
                Filters(
                    cats = args[0] as Set<Category>,
                    srcs = args[1] as Set<Source>,
                    tags = args[2] as Set<Tag>,
                    pers = args[3] as Set<Person>,
                    query = args[4] as String,
                    range = args[5] as DateRange?
                )
            }.flatMapLatest { f ->
                fixedExpenseUseCases.observeFixedExpensesFilteredUseCase(
                    query = null,
                    categoryIds = f.cats.mapNotNull { it.id },
                    sourceIds = f.srcs.mapNotNull { it.id },
                    tagIds = f.tags.mapNotNull { it.id },
                    personIds = f.pers.mapNotNull { it.id }
                ).map { list ->
                    val inRange = list.filter { expense ->
                        val endDate = expense.endDate
                        f.range == null || (expense.startDate <= f.range.end && (endDate == null || endDate >= f.range.start))
                    }
                    val filtered = inRange.filter {
                        it.categoryName?.contains(f.query, ignoreCase = true) == true ||
                                it.description?.contains(f.query, ignoreCase = true) == true ||
                                it.title.contains(f.query, ignoreCase = true) ||
                                f.query.isBlank()
                    }
                    val grouped = filtered.groupBy { it.recurrence }
                    val total = filtered.filter { it.isActive }.sumOf { it.amount }
                    
                    val canCloneOnce = grouped[RecurrenceType.ONCE].isNullOrEmpty() &&
                            list.any { it.recurrence == RecurrenceType.ONCE && (f.range == null || it.startDate < f.range.start) }
                    
                    Result(list, grouped, total, canCloneOnce)
                }
            }.collect { res ->
                _state.update {
                    it.copy(
                        expenses = res.all,
                        grouped = res.grouped,
                        totalApprox = res.total,
                        canCloneOnce = res.canCloneOnce,
                        searchQuery = _searchQuery.value,
                        dateRange = _dateRange.value,
                        filterCategories = _filterCategories.value,
                        filterSources = _filterSources.value,
                        filterTags = _filterTags.value,
                        filterPersons = _filterPersons.value
                    )
                }
            }
        }
    }

    private data class Filters(
        val cats: Set<Category>,
        val srcs: Set<Source>,
        val tags: Set<Tag>,
        val pers: Set<Person>,
        val query: String,
        val range: DateRange?
    )

    private data class Result(
        val all: List<FixedExpense>,
        val grouped: Map<RecurrenceType, List<FixedExpense>>,
        val total: Long,
        val canCloneOnce: Boolean
    )

    private fun toggleActive(expenseId: Long) {
        viewModelScope.launch {
            val expense = _state.value.grouped.values.flatten().find { it.id == expenseId } ?: return@launch
            fixedExpenseUseCases.updateFixedExpenseUseCase(expense.copy(isActive = !expense.isActive))
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun registerAsTransaction(expense: FixedExpense) {
        val current = _state.value
        if (current.postingStatusFailed || current.postingIds.isNotEmpty() || current.repeatConfirmation != null) return
        val status = current.postingStatuses[expense.id] ?: return
        if (!status.canRegister || current.expenses.none { it.id == expense.id }) return
        val command = FixedExpensePostingCommand(expense, status.periodDueDate,
            if (status.hasPrimaryPosting) "fixed-repeat:${Uuid.random()}" else null)
        if (status.hasPrimaryPosting) _state.update { it.copy(repeatConfirmation = command,
            postingMessages = it.postingMessages - expense.id, postingFailures = it.postingFailures - expense.id) }
        else post(command)
    }

    private fun post(command: FixedExpensePostingCommand) {
        val id = command.expense.id
        // Claim on the main-thread intent path, before launching: rapid taps cannot enqueue twice.
        if (_state.value.postingIds.isNotEmpty()) return
        _state.update { it.copy(postingIds = setOf(id), postingMessages = it.postingMessages - id,
            postingFailures = it.postingFailures - id) }
        viewModelScope.launch {
            try {
                val result = fixedExpenseUseCases.postFixedExpenseAsTransactionUseCase(
                    command.expense, command.periodDueDate, command.repeatOperationId)
                val message = UiText.StringResourceText(if (result > 0L)
                    Res.string.msg_fixed_expense_added_to_transactions else Res.string.fixed_expense_post_not_applied)
                _state.update { it.copy(repeatConfirmation = null,
                    postingMessages = it.postingMessages + (id to message),
                    postingFailures = if (result > 0L) it.postingFailures - id else it.postingFailures + id) }
                if (result > 0L) {
                    runCatching { analytics.track(com.kazemieh.common.analytics.ProductEvent.FixedExpenseAutoLogged) }
                }
                _effect.trySend(FixedExpenseListEffect.ShowMessage(message))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Keep the same confirmation nonce for retry after an uncertain response.
                val message = UiText.StringResourceText(Res.string.fixed_expense_post_failed)
                _state.update { it.copy(postingMessages = it.postingMessages +
                    (id to message),
                    postingFailures = it.postingFailures + id) }
                _effect.trySend(FixedExpenseListEffect.ShowMessage(message))
            } finally {
                _state.update { it.copy(postingIds = it.postingIds - id) }
            }
        }
    }

    private fun confirmDelete() {
        val expense = _state.value.selectedExpense ?: return
        viewModelScope.launch {
            fixedExpenseUseCases.deleteFixedExpenseUseCase(expense.id)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.FixedExpenseDeleted)
            _state.update { it.copy(isDeleteShow = false, selectedExpense = null) }
        }
    }
}

data class FixedExpenseListState(
    val postingStatuses: Map<Long, FixedExpensePostingStatus> = emptyMap(),
    val postingStatusFailed: Boolean = false,
    val postingIds: Set<Long> = emptySet(),
    val postingMessages: Map<Long, UiText> = emptyMap(),
    val postingFailures: Set<Long> = emptySet(),
    val repeatConfirmation: FixedExpensePostingCommand? = null,
    // Full, unfiltered list (used by the dashboard widget).
    val expenses: List<FixedExpense> = emptyList(),
    val grouped: Map<RecurrenceType, List<FixedExpense>> = emptyMap(),
    val dateRange: DateRange? = DateFilterHelper.getRange(DateFilterType.THIS_MONTH),
    val searchQuery: String = "",
    val totalApprox: Long = 0,
    val canCloneOnce: Boolean = false,
    val isDeleteShow: Boolean = false,
    val selectedExpense: FixedExpense? = null,
    val showFilterSheet: Boolean = false,
    val filterCategories: Set<Category> = emptySet(),
    val filterSources: Set<Source> = emptySet(),
    val filterTags: Set<Tag> = emptySet(),
    val filterPersons: Set<Person> = emptySet()
)

sealed interface FixedExpenseListIntent {
    data object ConfirmRepeat : FixedExpenseListIntent
    data object DismissRepeat : FixedExpenseListIntent
    data class UpdateSearchQuery(val query: String) : FixedExpenseListIntent
    data class ChangeFilterType(val type: DateFilterType) : FixedExpenseListIntent
    data class ShiftRange(val direction: Direction) : FixedExpenseListIntent
    data class ToggleActive(val expenseId: Long) : FixedExpenseListIntent
    data class RegisterAsTransaction(val expense: FixedExpense) : FixedExpenseListIntent
    data class OnDeleteClick(val expense: FixedExpense? = null) : FixedExpenseListIntent
    data object ConfirmDelete : FixedExpenseListIntent
    data object CloneOnceFromPrevious : FixedExpenseListIntent
    data object OnFilterClick : FixedExpenseListIntent
    data object OnFilterSheetDismiss : FixedExpenseListIntent
    data object OnFilterReset : FixedExpenseListIntent
    data class OnFilterUpdate(
        val categories: Set<Category>,
        val sources: Set<Source>,
        val tags: Set<Tag>,
        val persons: Set<Person>
    ) : FixedExpenseListIntent
}

sealed interface FixedExpenseListEffect {
    data class ShowMessage(val message: UiText) : FixedExpenseListEffect
}

data class FixedExpensePostingCommand(
    val expense: FixedExpense,
    val periodDueDate: Long,
    val repeatOperationId: String?
)
