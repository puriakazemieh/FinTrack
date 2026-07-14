package com.kazemieh.fixed_expense.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.GetSourceByIdUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedSourcesUseCase
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.persiandatetime.extensions.minus
import com.kazemieh.common.persiandatetime.extensions.dayOfWeekIndex
import fintrack.core.designsystem.generated.resources.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

class AddFixedExpenseViewModel(
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val getSourceByIdUseCase: GetSourceByIdUseCase,
    private val observeMostUsedCategoriesUseCase: ObserveMostUsedCategoriesUseCase,
    private val observeMostUsedSourcesUseCase: ObserveMostUsedSourcesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddFixedExpenseState())
    val state: StateFlow<AddFixedExpenseState> = _state.asStateFlow()

    private val _effect = Channel<AddFixedExpenseEffect>()
    val effect: Flow<AddFixedExpenseEffect> = _effect.receiveAsFlow()

    init {
        observeMostUsed()
    }

    fun onIntent(intent: AddFixedExpenseIntent) {
        when (intent) {
            is AddFixedExpenseIntent.SetTitle -> _state.update { it.copy(title = intent.title) }
            is AddFixedExpenseIntent.SetAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddFixedExpenseIntent.SetCategory -> _state.update { it.copy(category = intent.category) }
            is AddFixedExpenseIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddFixedExpenseIntent.SetRecurrence -> _state.update {
                // Anchor the start date to the period start (today / this week / this month /
                // this year) exactly like budgets when a recurring type is picked.
                val anchored = adjustStartDate(it.baseAt.takeIf { b -> b != 0L } ?: it.startDate, intent.recurrence)
                it.copy(recurrence = intent.recurrence, startDate = anchored)
            }
            is AddFixedExpenseIntent.SetStartDate -> _state.update {
                it.copy(startDate = intent.startDate, baseAt = intent.startDate)
            }
            is AddFixedExpenseIntent.SetEndDate -> _state.update { it.copy(endDate = intent.endDate) }
            is AddFixedExpenseIntent.SetAutoPost -> _state.update { it.copy(isAutoPostEnabled = intent.enabled) }
            is AddFixedExpenseIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddFixedExpenseIntent.LoadExpense -> loadExpense(intent.expenseId)
            AddFixedExpenseIntent.Reset -> reset()
            AddFixedExpenseIntent.Submit -> submit()
        }
    }

    private fun observeMostUsed() {
        viewModelScope.launch {
            observeMostUsedCategoriesUseCase(TransactionType.EXPENSE, limit = 3).collect { categories ->
                _state.update { it.copy(mostUsedCategories = categories) }
            }
        }
        viewModelScope.launch {
            observeMostUsedSourcesUseCase(limit = 3).collect { sources ->
                _state.update { it.copy(mostUsedSources = sources) }
            }
        }
    }

    private fun reset() {
        val now = Clock.System.now().toEpochMilliseconds()
        _state.update {
            AddFixedExpenseState(
                startDate = adjustStartDate(now, RecurrenceType.MONTHLY),
                baseAt = now,
                mostUsedCategories = it.mostUsedCategories,
                mostUsedSources = it.mostUsedSources
            )
        }
    }

    // Snap the start date to the beginning of the selected period, mirroring budgets.
    private fun adjustStartDate(time: Long, recurrence: RecurrenceType): Long {
        val tz = TimeZone.currentSystemDefault()
        val pdt = Instant.fromEpochMilliseconds(time).toPersianDateTime(tz)
        return when (recurrence) {
            RecurrenceType.DAILY -> pdt.copy(hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            RecurrenceType.WEEKLY -> {
                val daysToSaturday = pdt.dayOfWeekIndex
                pdt.minus(daysToSaturday, DateTimeUnit.DAY).copy(hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            }
            RecurrenceType.MONTHLY -> pdt.copy(day = 1, hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            RecurrenceType.YEARLY -> pdt.copy(month = 1, day = 1, hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            else -> time // CUSTOM / ONCE keep the picked date
        }
    }

    private fun loadExpense(expenseId: Long) {
        viewModelScope.launch {
            val expense = fixedExpenseUseCases.getFixedExpenseByIdUseCase(expenseId) ?: return@launch
            val category = getCategoryUseCase(expense.categoryId)
            val source = getSourceByIdUseCase(expense.sourceId)
            _state.update {
                it.copy(
                    expenseId = expense.id,
                    title = expense.title,
                    amount = expense.amount.toString(),
                    category = category,
                    source = source,
                    recurrence = expense.recurrence,
                    startDate = expense.startDate,
                    baseAt = expense.startDate,
                    endDate = expense.endDate,
                    nextDueDate = expense.nextDueDate,
                    isAutoPostEnabled = expense.isAutoPostEnabled,
                    isActive = expense.isActive,
                    description = expense.description ?: ""
                )
            }
        }
    }

    private fun submit() {
        val currentState = _state.value
        val amountValue = currentState.amount.toLongOrNull()
        
        if (currentState.title.isBlank()) {
            viewModelScope.launch { _effect.send(AddFixedExpenseEffect.Error(Res.string.error_title_required)) }
            return
        }
        
        if (amountValue == null) return

        if (currentState.isAutoPostEnabled && currentState.category == null) {
            viewModelScope.launch { _effect.send(AddFixedExpenseEffect.Error(Res.string.error_category_required_for_auto_post)) }
            return
        }

        viewModelScope.launch {
            val endDate = if (currentState.recurrence == RecurrenceType.CUSTOM) currentState.endDate else null
            val expense = FixedExpense(
                id = currentState.expenseId ?: 0L,
                title = currentState.title,
                amount = amountValue,
                categoryId = currentState.category?.id ?: 0L,
                sourceId = currentState.source?.id ?: 0L,
                recurrence = currentState.recurrence,
                startDate = currentState.startDate,
                endDate = endDate,
                // Keep the existing next due date when editing; a new expense starts at start date.
                nextDueDate = if (currentState.expenseId != null) currentState.nextDueDate else currentState.startDate,
                isAutoPostEnabled = currentState.isAutoPostEnabled,
                isActive = currentState.isActive,
                description = currentState.description
            )
            val reminderTitle = getString(Res.string.notif_installment_label)
            val reminderMessage = getString(
                Res.string.msg_notif_fixed_expense_due,
                currentState.category?.name ?: ""
            )
            if (currentState.expenseId != null) {
                fixedExpenseUseCases.updateFixedExpenseUseCase(expense)
            } else {
                fixedExpenseUseCases.addFixedExpenseUseCase(expense, reminderTitle, reminderMessage)
            }
            // Clear the form so the next "add" starts empty.
            reset()
            _effect.send(AddFixedExpenseEffect.Saved)
        }
    }
}

data class AddFixedExpenseState(
    val expenseId: Long? = null,
    val title: String = "",
    val amount: String = "",
    val category: Category? = null,
    val source: Source? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val startDate: Long = Clock.System.now().toEpochMilliseconds(),
    // Un-snapped reference date the period anchoring derives from (mirrors budgets).
    val baseAt: Long = 0,
    val endDate: Long? = null,
    val nextDueDate: Long = Clock.System.now().toEpochMilliseconds(),
    val isAutoPostEnabled: Boolean = false,
    val isActive: Boolean = true,
    val description: String = "",
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList()
)

sealed interface AddFixedExpenseIntent {
    data class SetTitle(val title: String) : AddFixedExpenseIntent
    data class SetAmount(val amount: String) : AddFixedExpenseIntent
    data class SetCategory(val category: Category?) : AddFixedExpenseIntent
    data class SetSource(val source: Source?) : AddFixedExpenseIntent
    data class SetRecurrence(val recurrence: RecurrenceType) : AddFixedExpenseIntent
    data class SetStartDate(val startDate: Long) : AddFixedExpenseIntent
    data class SetEndDate(val endDate: Long?) : AddFixedExpenseIntent
    data class SetAutoPost(val enabled: Boolean) : AddFixedExpenseIntent
    data class SetDescription(val description: String) : AddFixedExpenseIntent
    data class LoadExpense(val expenseId: Long) : AddFixedExpenseIntent
    data object Reset : AddFixedExpenseIntent
    data object Submit : AddFixedExpenseIntent
}

sealed interface AddFixedExpenseEffect {
    data object Saved : AddFixedExpenseEffect
    data class Error(val messageRes: org.jetbrains.compose.resources.StringResource) : AddFixedExpenseEffect
}
