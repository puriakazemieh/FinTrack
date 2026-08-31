package com.kazemieh.budget.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRange
import com.kazemieh.common.Direction
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.domain.usecase.AddBudgetUseCase
import com.kazemieh.domain.usecase.DeleteBudgetUseCase
import com.kazemieh.domain.usecase.ObserveBudgetsWithProgressUseCase
import com.kazemieh.domain.usecase.HasAnyBudgetsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class BudgetState(
    val dailyBudgets: List<BudgetWithProgress> = emptyList(),
    val weeklyBudgets: List<BudgetWithProgress> = emptyList(),
    val monthlyBudgets: List<BudgetWithProgress> = emptyList(),
    val yearlyBudgets: List<BudgetWithProgress> = emptyList(),
    val isLoading: Boolean = false,
    val isAddBudgetShow: Boolean = false,
    val selectedBudget: BudgetWithProgress? = null,
    val searchQuery: String = "",
    val pendingDeleteBudget: BudgetWithProgress? = null,
    val dateRange: DateRange? = DateFilterHelper.getRange(DateFilterType.THIS_MONTH),
    val showCloneButton: Boolean = false, // Global or per section? Let's do global for now as fallback
    val canCloneDaily: Boolean = false,
    val canCloneWeekly: Boolean = false,
    val canCloneMonthly: Boolean = false,
    val canCloneYearly: Boolean = false
)

sealed interface BudgetIntent {
    data object LoadBudgets : BudgetIntent
    data class UpdateSearchQuery(val query: String) : BudgetIntent
    data class DeleteBudget(val budget: BudgetWithProgress) : BudgetIntent
    data object ConfirmDeleteBudget : BudgetIntent
    data object CancelDeleteBudget : BudgetIntent
    data class ShowAddBudget(val budget: BudgetWithProgress? = null) : BudgetIntent
    data class ChangeFilterType(val type: DateFilterType) : BudgetIntent
    data class ShiftRange(val direction: Direction) : BudgetIntent
    data class CloneFromPrevious(val period: BudgetPeriod? = null) : BudgetIntent
}

sealed interface BudgetEffect {
    data class ShowError(val message: String) : BudgetEffect
    data object BudgetsCloned : BudgetEffect
}

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val observeBudgetsWithProgressUseCase: ObserveBudgetsWithProgressUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val hasAnyBudgetsUseCase: HasAnyBudgetsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<BudgetEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow(_state.value.dateRange)

    private var observeJob: Job? = null

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.BudgetListViewed)
        observeBudgets()
    }

    fun onIntent(intent: BudgetIntent) {
        when (intent) {
            BudgetIntent.LoadBudgets -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("budget_list"))
                observeBudgets()
            }
            is BudgetIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is BudgetIntent.DeleteBudget -> _state.update { it.copy(pendingDeleteBudget = intent.budget) }
            BudgetIntent.ConfirmDeleteBudget -> deleteBudget()
            BudgetIntent.CancelDeleteBudget -> _state.update { it.copy(pendingDeleteBudget = null) }
            is BudgetIntent.ShowAddBudget -> {
                _state.update { it.copy(isAddBudgetShow = !it.isAddBudgetShow, selectedBudget = intent.budget) }
            }
            is BudgetIntent.ChangeFilterType -> {
                val newRange = DateFilterHelper.getRange(intent.type)
                _dateRange.value = newRange
                _state.update { it.copy(dateRange = newRange) }
            }
            is BudgetIntent.ShiftRange -> {
                val current = _dateRange.value ?: return
                val newRange = DateFilterHelper.shiftDateRange(current.start, current.end, current.filterType, intent.direction)
                _dateRange.value = newRange
                _state.update { it.copy(dateRange = newRange) }
            }
            is BudgetIntent.CloneFromPrevious -> cloneFromPrevious(intent.period)
        }
    }

    private fun observeBudgets() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            _dateRange.flatMapLatest { range ->
                combine(
                    observeBudgetsWithProgressUseCase(range?.start ?: 0L, range?.end ?: Long.MAX_VALUE),
                    _searchQuery
                ) { budgets, query ->
                    val filtered = budgets.filter {
                        it.category?.name?.contains(query, ignoreCase = true) == true
                    }
                    
                    val daily = filtered.filter { it.budget.period == BudgetPeriod.DAILY }
                    val weekly = filtered.filter { it.budget.period == BudgetPeriod.WEEKLY }
                    val monthly = filtered.filter { it.budget.period == BudgetPeriod.MONTHLY }
                    val yearly = filtered.filter { it.budget.period == BudgetPeriod.YEARLY }

                    var canCloneDaily = false
                    var canCloneWeekly = false
                    var canCloneMonthly = false
                    var canCloneYearly = false

                    if (range != null) {
                        val earlier = observeBudgetsWithProgressUseCase(0L, range.start - 1).first()
                        canCloneDaily = daily.isEmpty() && earlier.any { it.budget.period == BudgetPeriod.DAILY }
                        canCloneWeekly = weekly.isEmpty() && earlier.any { it.budget.period == BudgetPeriod.WEEKLY }
                        canCloneMonthly = monthly.isEmpty() && earlier.any { it.budget.period == BudgetPeriod.MONTHLY }
                        canCloneYearly = yearly.isEmpty() && earlier.any { it.budget.period == BudgetPeriod.YEARLY }
                    val allWarned = mutableSetOf<Long>()
                    filtered.forEach { bw ->
                        if (bw.spentAmount > bw.budget.amount && !allWarned.contains(bw.budget.id)) {
                            analytics.track(com.kazemieh.common.analytics.ProductEvent.BudgetExceededWarning)
                            allWarned.add(bw.budget.id ?: 0L)
                        }
                    }

                    BudgetGroups(daily, weekly, monthly, yearly, canCloneDaily, canCloneWeekly, canCloneMonthly, canCloneYearly)
                }
            }.collect { groups ->
                _state.update {
                    it.copy(
                        dailyBudgets = groups.daily,
                        weeklyBudgets = groups.weekly,
                        monthlyBudgets = groups.monthly,
                        yearlyBudgets = groups.yearly,
                        canCloneDaily = groups.canCloneDaily,
                        canCloneWeekly = groups.canCloneWeekly,
                        canCloneMonthly = groups.canCloneMonthly,
                        canCloneYearly = groups.canCloneYearly,
                        isLoading = false,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
    }

    private data class BudgetGroups(
        val daily: List<BudgetWithProgress>,
        val weekly: List<BudgetWithProgress>,
        val monthly: List<BudgetWithProgress>,
        val yearly: List<BudgetWithProgress>,
        val canCloneDaily: Boolean,
        val canCloneWeekly: Boolean,
        val canCloneMonthly: Boolean,
        val canCloneYearly: Boolean
    )

    private fun cloneFromPrevious(period: BudgetPeriod?) {
        val currentRange = _dateRange.value ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val earlier = observeBudgetsWithProgressUseCase(0L, currentRange.start - 1).first()
            val candidates = if (period == null) earlier else earlier.filter { it.budget.period == period }
            val toClone = candidates.groupBy { it.budget.period }.flatMap { (_, list) ->
                val maxStart = list.maxOfOrNull { it.budget.startAt } ?: return@flatMap emptyList()
                list.filter { it.budget.startAt == maxStart }
            }

            toClone.forEach { item ->
                val newBudget = item.budget.copy(
                    id = null,
                    startAt = currentRange.start,
                    updatedAt = 0,
                    syncStatus = SyncStatus.MODIFIED
                )
                addBudgetUseCase(newBudget)
            }
            _state.update { it.copy(isLoading = false) }
            _effect.send(BudgetEffect.BudgetsCloned)
        }
    }

    private fun deleteBudget() {
        val budgetToDelete = _state.value.pendingDeleteBudget ?: return
        viewModelScope.launch {
            deleteBudgetUseCase(budgetToDelete.budget.id ?: 0L)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.BudgetDeleted)
            _state.update { it.copy(pendingDeleteBudget = null) }
        }
    }
}
