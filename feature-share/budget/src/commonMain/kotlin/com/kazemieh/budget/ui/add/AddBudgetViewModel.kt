package com.kazemieh.budget.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.persiandatetime.extensions.minus
import com.kazemieh.common.persiandatetime.extensions.dayOfWeekIndex
import com.kazemieh.domain.usecase.AddBudgetUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedPersonsUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedSourcesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedTagsUseCase
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.UpdateBudgetUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Instant
import kotlin.time.Clock

data class AddBudgetState(
    val id: Long? = null,
    val selectedCategory: Category? = null,
    val amount: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startAt: Long = 0,
    // The un-snapped reference date; period snapping always derives from this so switching
    // period re-anchors to the real date instead of the previously snapped value.
    val baseAt: Long = 0,
    val isAlertEnabled: Boolean = true,
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val sources: List<Source> = emptyList(),
    val persons: List<Person> = emptyList(),
    val selectedTags: Set<Tag> = emptySet(),
    val selectedPersons: Set<Person> = emptySet(),
    val selectedSource: Source? = null,
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val mostUsedTags: List<Tag> = emptyList(),
    val mostUsedPersons: List<Person> = emptyList(),
    val isLoading: Boolean = false,
    val topSheet: AddBudgetSheet? = null
)

sealed interface AddBudgetSheet {
    data object CategoryPicker : AddBudgetSheet
    data object TagPicker : AddBudgetSheet
    data object SourcePicker : AddBudgetSheet
    data object PersonPicker : AddBudgetSheet
    data object Calculator : AddBudgetSheet
}

sealed interface AddBudgetIntent {
    data class SelectCategory(val category: Category) : AddBudgetIntent
    data class UpdateAmount(val amount: String) : AddBudgetIntent
    data class UpdatePeriod(val period: BudgetPeriod) : AddBudgetIntent
    data class UpdateAlert(val isEnabled: Boolean) : AddBudgetIntent
    data object SaveBudget : AddBudgetIntent
    data class LoadCategories(val type: TransactionType = TransactionType.EXPENSE) : AddBudgetIntent
    data object LoadExtraData : AddBudgetIntent
    data class SelectTags(val tags: Set<Tag>) : AddBudgetIntent
    data class SelectPersons(val persons: Set<Person>) : AddBudgetIntent
    data class SelectSource(val source: Source?) : AddBudgetIntent
    data class InitialData(
        val budget: Budget?,
        val category: Category?,
        val defaultStartAt: Long? = null,
        val defaultRangeEnd: Long? = null
    ) : AddBudgetIntent
    data class ToggleSheet(val sheet: AddBudgetSheet?) : AddBudgetIntent
}

sealed interface AddBudgetEffect {
    data object BudgetSaved : AddBudgetEffect
    data class ShowError(val message: String) : AddBudgetEffect
}

class AddBudgetViewModel(
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeTagsUseCase: ObserveTagsUseCase,
    private val observeSourcesUseCase: ObserveSourcesUseCase,
    private val observePersonsUseCase: ObservePersonsUseCase,
    private val observeMostUsedCategoriesUseCase: ObserveMostUsedCategoriesUseCase,
    private val observeMostUsedSourcesUseCase: ObserveMostUsedSourcesUseCase,
    private val observeMostUsedTagsUseCase: ObserveMostUsedTagsUseCase,
    private val observeMostUsedPersonsUseCase: ObserveMostUsedPersonsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddBudgetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddBudgetEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeMostUsed()
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
        viewModelScope.launch {
            observeMostUsedTagsUseCase(limit = 3).collect { tags ->
                _state.update { it.copy(mostUsedTags = tags) }
            }
        }
        viewModelScope.launch {
            observeMostUsedPersonsUseCase(limit = 3).collect { persons ->
                _state.update { it.copy(mostUsedPersons = persons) }
            }
        }
    }

    fun onIntent(intent: AddBudgetIntent) {
        when (intent) {
            is AddBudgetIntent.SelectCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            is AddBudgetIntent.UpdateAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddBudgetIntent.UpdatePeriod -> {
                val base = _state.value.baseAt.takeIf { it != 0L } ?: _state.value.startAt
                val adjustedStartAt = adjustStartAt(base, intent.period)
                _state.update { it.copy(period = intent.period, startAt = adjustedStartAt) }
            }
            is AddBudgetIntent.UpdateAlert -> _state.update { it.copy(isAlertEnabled = intent.isEnabled) }
            AddBudgetIntent.SaveBudget -> saveBudget()
            is AddBudgetIntent.LoadCategories -> loadCategories(intent.type)
            AddBudgetIntent.LoadExtraData -> loadExtraData()
            is AddBudgetIntent.SelectTags -> _state.update { it.copy(selectedTags = intent.tags) }
            is AddBudgetIntent.SelectPersons -> _state.update { it.copy(selectedPersons = intent.persons) }
            is AddBudgetIntent.SelectSource -> _state.update { it.copy(selectedSource = intent.source) }
            is AddBudgetIntent.InitialData -> {
                if (intent.budget != null) {
                    _state.update {
                        it.copy(
                            id = intent.budget.id,
                            selectedCategory = intent.category,
                            amount = intent.budget.amount.toString(),
                            period = intent.budget.period,
                            startAt = intent.budget.startAt,
                            baseAt = intent.budget.startAt,
                            isAlertEnabled = intent.budget.isAlertEnabled
                        )
                    }
                } else {
                    // Anchor to "today" when the viewed range contains it (so a daily budget starts
                    // today, a weekly one this week, etc.); otherwise anchor to the viewed range's
                    // start so a budget added while browsing another period lands in that period.
                    val now = Clock.System.now().toEpochMilliseconds()
                    val rangeStart = intent.defaultStartAt
                    val rangeEnd = intent.defaultRangeEnd
                    val base = if (rangeStart != null && rangeEnd != null && now in rangeStart..rangeEnd) {
                        now
                    } else {
                        rangeStart ?: now
                    }
                    val adjusted = adjustStartAt(base, BudgetPeriod.MONTHLY)
                    _state.update {
                        AddBudgetState(
                            categories = it.categories,
                            tags = it.tags,
                            sources = it.sources,
                            mostUsedCategories = it.mostUsedCategories,
                            mostUsedSources = it.mostUsedSources,
                            startAt = adjusted,
                            baseAt = base,
                            period = BudgetPeriod.MONTHLY
                        )
                    }
                }
            }
            is AddBudgetIntent.ToggleSheet -> _state.update { it.copy(topSheet = intent.sheet) }
        }
    }

    private fun adjustStartAt(time: Long, period: BudgetPeriod): Long {
        val tz = TimeZone.currentSystemDefault()
        val pdt = Instant.fromEpochMilliseconds(time).toPersianDateTime(tz)
        return when (period) {
            BudgetPeriod.DAILY -> pdt.copy(hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            BudgetPeriod.WEEKLY -> {
                val daysToSaturday = pdt.dayOfWeekIndex
                pdt.minus(daysToSaturday, DateTimeUnit.DAY).copy(hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            }
            BudgetPeriod.MONTHLY -> pdt.copy(day = 1, hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
            BudgetPeriod.YEARLY -> pdt.copy(month = 1, day = 1, hour = 0, minute = 0, second = 0).toEpochMilliseconds(tz)
        }
    }

    private fun loadCategories(type: TransactionType) {
        viewModelScope.launch {
            observeCategoriesUseCase(type).collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadExtraData() {
        viewModelScope.launch {
            launch {
                observeTagsUseCase().collect { tags ->
                    _state.update { s -> s.copy(tags = tags) }
                }
            }
            launch {
                observeSourcesUseCase().collect { sources ->
                    _state.update { it.copy(sources = sources) }
                }
            }
            launch {
                observePersonsUseCase().collect { persons ->
                    _state.update { it.copy(persons = persons) }
                }
            }
        }
    }

    private fun saveBudget() {
        val currentState = _state.value
        val amountLong = currentState.amount.toLongOrNull() ?: 0L
        val categoryId = currentState.selectedCategory?.id ?: return

        viewModelScope.launch {
            val budget = Budget(
                id = currentState.id,
                categoryId = categoryId,
                amount = amountLong,
                period = currentState.period,
                startAt = currentState.startAt,
                tagIds = currentState.selectedTags.mapNotNull { it.id },
                sourceId = currentState.selectedSource?.id,
                personIds = currentState.selectedPersons.mapNotNull { it.id },
                isAlertEnabled = currentState.isAlertEnabled
            )

            if (budget.id == null) {
                addBudgetUseCase(budget)
            } else {
                updateBudgetUseCase(budget)
            }
            _effect.send(AddBudgetEffect.BudgetSaved)
        }
    }
}
