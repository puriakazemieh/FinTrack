package com.kazemieh.fixed_expense.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRange
import com.kazemieh.common.Direction
import com.kazemieh.common.model.*
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_fixed_expense_added_to_transactions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FixedExpenseListViewModel(
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(FixedExpenseListState())
    val state: StateFlow<FixedExpenseListState> = _state.asStateFlow()

    private val _effect = Channel<FixedExpenseListEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow(DateFilterHelper.getRange(DateFilterType.THIS_MONTH))
    private val _filterCategories = MutableStateFlow<Set<Category>>(emptySet())
    private val _filterSources = MutableStateFlow<Set<Source>>(emptySet())
    private val _filterTags = MutableStateFlow<Set<Tag>>(emptySet())
    private val _filterPersons = MutableStateFlow<Set<Person>>(emptySet())

    init {
        observeExpenses()
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

    private fun registerAsTransaction(expense: FixedExpense) {
        viewModelScope.launch {
            fixedExpenseUseCases.postFixedExpenseAsTransactionUseCase(expense)
            _effect.send(FixedExpenseListEffect.ShowMessage(Res.string.msg_fixed_expense_added_to_transactions))
        }
    }

    private fun confirmDelete() {
        val expense = _state.value.selectedExpense ?: return
        viewModelScope.launch {
            fixedExpenseUseCases.deleteFixedExpenseUseCase(expense.id)
            _state.update { it.copy(isDeleteShow = false, selectedExpense = null) }
        }
    }
}

data class FixedExpenseListState(
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
    data class ShowMessage(val messageRes: org.jetbrains.compose.resources.StringResource) : FixedExpenseListEffect
}
