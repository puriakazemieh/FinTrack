package com.kazemieh.transactions


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterHelper.getRange
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRange
import com.kazemieh.common.DateRangeLabel
import com.kazemieh.common.Direction
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.domain.usecase.ObserveTagsUseCase
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.preferences.FinTrackPreferences
import fintrack.core.designsystem.generated.resources.*
import fintrack.core.designsystem.generated.resources.label_year_short
import fintrack.core.designsystem.generated.resources.last_month
import fintrack.core.designsystem.generated.resources.last_week
import fintrack.core.designsystem.generated.resources.next_month
import fintrack.core.designsystem.generated.resources.next_week
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.tomorrow
import fintrack.core.designsystem.generated.resources.yesterday
import fintrack.core.designsystem.generated.resources.label_day
import fintrack.core.designsystem.generated.resources.range_text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


class TransactionsViewModel(
    private val transactionUseCaseGroup: TransactionUseCaseGroup,
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsState())
    val state = _state.asStateFlow()

    init {
        onIntent(TransactionsIntent.OnDateRange(DateFilterType.THIS_MONTH))
        loadAmountRange()
        observeDisabledTools()
    }

    private fun observeDisabledTools() {
        preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_DISABLED_TOOLS, "")
            .onEach { csv ->
                _state.update { it.copy(disabledTools = ToolFeature.parseDisabled(csv)) }
            }.launchIn(viewModelScope)
    }

    private fun loadAmountRange() {
        viewModelScope.launch {
            val range = transactionUseCaseGroup.getTransactionAmountRangeUseCase()
            _state.update {
                it.copy(
                    minAmountLimit = range.first.toFloat(),
                    maxAmountLimit = range.second.toFloat(),
                    selectedMinAmount = range.first.toFloat(),
                    selectedMaxAmount = range.second.toFloat()
                )
            }
        }
    }

    fun onIntent(intent: TransactionsIntent) {
        when (intent) {
            is TransactionsIntent.ShowTransactionBottomSheet -> _state.update {
                it.copy(
                    showAddTransaction = !_state.value.showAddTransaction,
                    transactionWithRelations = intent.transactionWithRelations
                )
            }

            is TransactionsIntent.DeleteTransactionBottomSheet -> _state.update {
                it.copy(
                    showDeleteTransaction = !_state.value.showDeleteTransaction,
                    transactionWithRelations = intent.transactionWithRelations
                )
            }

            is TransactionsIntent.OnTransactionTypeSelected -> _state.update {
                it.copy(
                    selectedTransactionType = intent.type,
                    selectedCategories = emptySet(),
                    isAllCategorySelected = true,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            is TransactionsIntent.OnAmountRangeChanged -> _state.update {
                it.copy(
                    selectedMinAmount = intent.min,
                    selectedMaxAmount = intent.max,
                    enableAnimationChart = !it.enableAnimationChart
                )
            }

            TransactionsIntent.OnToggleSourceSheet -> _state.update {
                it.copy(isSourceSheetVisible = !it.isSourceSheetVisible)
            }

            is TransactionsIntent.OnSourcesSelected -> _state.update {
                it.copy(
                    selectedSources = intent.sources,
                    isSourceSheetVisible = false,
                    isAllSourceSelected = intent.isAllSourceSelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            TransactionsIntent.OnToggleTagSheet -> _state.update {
                it.copy(isTagSheetVisible = !it.isTagSheetVisible)
            }

            is TransactionsIntent.OnTagSelected -> _state.update {
                it.copy(
                    selectedTag = intent.tag,
                    isTagSheetVisible = false,
                    isAllTAgSelected = intent.isAllTAgSelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            TransactionsIntent.OnTogglePersonSheet -> _state.update {
                it.copy(isPersonSheetVisible = !it.isPersonSheetVisible)
            }

            is TransactionsIntent.OnPersonSelected -> _state.update {
                it.copy(
                    selectedPerson = intent.persons,
                    isPersonSheetVisible = false,
                    isAllPersonSelected = intent.isAllPersonSelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            TransactionsIntent.OnToggleCategorySheet -> _state.update {
                it.copy(isCategorySheetVisible = !it.isCategorySheetVisible)
            }

            is TransactionsIntent.OnCategoriesSelected -> _state.update {
                it.copy(
                    selectedCategories = intent.categories,
                    isCategorySheetVisible = false,
                    isAllCategorySelected = intent.isAllCategorySelected,
                    selectedTransactionType = if (intent.isAllCategorySelected) TransactionType.ALL else it.selectedTransactionType,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            TransactionsIntent.OnToggleDateSheet -> _state.update {
                it.copy(isDateSheetVisible = !it.isDateSheetVisible)
            }

            TransactionsIntent.OnToggleCustomDateSheet -> _state.update {
                it.copy(isCustomDateSheetVisible = !it.isCustomDateSheetVisible, isError = false)
            }

            is TransactionsIntent.OnDateRange -> {
                val result = getRangeAsUiText(intent.dateFilterType)
                val range = result.first
                val uiText = result.second
                val subLabel = getPeriodSubLabel(range)

                _state.update {
                    it.copy(
                        dateFilterType = range?.filterType ?: DateFilterType.THIS_MONTH,
                        isDateSheetVisible = false,
                        startDateTimeStamp = range?.start,
                        endDateTimeStamp = range?.end,
                        isShowArrowButton = true,
                        textDate = uiText,
                        textPeriodRange = subLabel,
                        startDate = null,
                        endDate = null,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }
            }

            is TransactionsIntent.OnDateSheetSubmit -> {
                if (intent.endTimeStamp != null && intent.startTimeStamp != null &&
                    intent.endDate != null && intent.startDate != null
                ) {
                    val result = getRangeAsUiText(
                        DateFilterType.CUSTOM_RANGE,
                        intent.startTimeStamp,
                        intent.endTimeStamp
                    )
                    val range = result.first
                    
                    // Display range with month names instead of raw numeric strings
                    val startP = DateFilterHelper.persianDateFromMillis(intent.startTimeStamp, TimeZone.currentSystemDefault())
                    val endP = DateFilterHelper.persianDateFromMillis(intent.endTimeStamp, TimeZone.currentSystemDefault())
                    
                    val actualRangeText = if (startP.year == endP.year && startP.month == endP.month && startP.day == endP.day) {
                        UiText.DynamicString("${startP.day.toPersianDigits()} ${startP.persianMonth().displayName} ${startP.year.toPersianDigits()}")
                    } else if (startP.year == endP.year) {
                        UiText.StringResourceText(
                            Res.string.range_text,
                            listOf(
                                "${startP.day.toPersianDigits()} ${startP.persianMonth().displayName}",
                                "${endP.day.toPersianDigits()} ${endP.persianMonth().displayName} ${endP.year.toPersianDigits()}"
                            )
                        )
                    } else {
                        UiText.StringResourceText(
                            Res.string.range_text,
                            listOf(
                                "${startP.day.toPersianDigits()} ${startP.persianMonth().displayName} ${startP.year.toPersianDigits()}",
                                "${endP.day.toPersianDigits()} ${endP.persianMonth().displayName} ${endP.year.toPersianDigits()}"
                            )
                        )
                    }
                    val uiText = actualRangeText
                    val subLabel = getPeriodSubLabel(range)

                    _state.update {
                        it.copy(
                            dateFilterType = range?.filterType ?: DateFilterType.THIS_MONTH,
                            isDateSheetVisible = false,
                            startDateTimeStamp = range?.start,
                            endDateTimeStamp = range?.end,
                            textDate = uiText,
                            textPeriodRange = subLabel,
                            startDate = intent.startDate,
                            endDate = intent.endDate,
                            isShowArrowButton = false,
                            isCustomDateSheetVisible = false,
                            enableAnimationChart = !_state.value.enableAnimationChart
                        )
                    }
                } else {
                    _state.update { it.copy(isError = true) }
                }

            }

            TransactionsIntent.OnNextClick -> {
                val result = DateFilterHelper.shiftDateRange(
                    start = state.value.startDateTimeStamp,
                    end = state.value.endDateTimeStamp,
                    filterType = state.value.dateFilterType,
                    direction = Direction.NEXT
                )
                val uiText = result.label.toUiText(result.start)
                val subLabel = getPeriodSubLabel(result)

                _state.update {
                    it.copy(
                        textDate = uiText,
                        textPeriodRange = subLabel,
                        dateFilterType = result.filterType,
                        startDateTimeStamp = result.start,
                        endDateTimeStamp = result.end,
                        isShowArrowButton = true,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }

            }

            TransactionsIntent.OnPrevClick -> {
                val result = DateFilterHelper.shiftDateRange(
                    start = state.value.startDateTimeStamp,
                    end = state.value.endDateTimeStamp,
                    filterType = state.value.dateFilterType,
                    direction = Direction.PREVIOUS
                )
                val uiText = result.label.toUiText(result.start)
                val subLabel = getPeriodSubLabel(result)

                _state.update {
                    it.copy(
                        textDate = uiText,
                        textPeriodRange = subLabel,
                        dateFilterType = result.filterType,
                        startDateTimeStamp = result.start,
                        endDateTimeStamp = result.end,
                        isShowArrowButton = true,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }

            }

            is TransactionsIntent.OnToggleSearch -> _state.update { it.copy(isSearchActive = !it.isSearchActive) }

            is TransactionsIntent.OnToggleFilterSheet -> _state.update { it.copy(isFilterSheetVisible = !it.isFilterSheetVisible) }

            TransactionsIntent.ResetFilters -> {
                _state.update {
                    TransactionsState().copy(
                        resetFiltersHandled = true,
                        enableAnimationChart = !it.enableAnimationChart
                    )
                }
                onIntent(TransactionsIntent.OnDateRange(DateFilterType.THIS_MONTH))
            }

            is TransactionsIntent.ApplyFilter -> {
                viewModelScope.launch {
                    val categories = intent.categoryId?.let { id ->
                        transactionUseCaseGroup.getCategoryUseCase(id)
                    }?.let { setOf(it) } ?: emptySet()

                    val sources = intent.sourceId?.let { id ->
                        transactionUseCaseGroup.observeSourcesUseCase().first().find { it.id == id }
                    }?.let { setOf(it) } ?: emptySet()

                    val tags = intent.tagId?.let { id ->
                        transactionUseCaseGroup.observeTagsUseCase().first().find { it.id == id }
                    }?.let { setOf(it) } ?: emptySet()

                    val persons = intent.personId?.let { id ->
                        transactionUseCaseGroup.observePersonsUseCase().first().find { it.id == id }
                    }?.let { setOf(it) } ?: emptySet()

                    _state.update {
                        it.copy(
                            selectedCategories = categories,
                            isAllCategorySelected = categories.isEmpty(),
                            selectedSources = sources,
                            isAllSourceSelected = sources.isEmpty(),
                            selectedTag = tags,
                            isAllTAgSelected = tags.isEmpty(),
                            selectedPerson = persons,
                            isAllPersonSelected = persons.isEmpty(),
                            selectedTransactionType = intent.transactionType ?: (if (categories.isNotEmpty()) categories.first().type else it.selectedTransactionType),
                            searchQuery = intent.query ?: it.searchQuery,
                            isSearchActive = intent.query != null || it.isSearchActive,
                            enableAnimationChart = !it.enableAnimationChart
                        )
                    }
                }
            }
        }
    }

    fun getRangeAsUiText(
        type: DateFilterType,
        customFrom: Long? = null,
        customTo: Long? = null,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Pair<DateRange?, UiText> {
        val range = getRange(type, customFrom, customTo, timeZone)
        return Pair(range, range?.label?.toUiText(range.start) ?: UiText.DynamicString(""))
    }

    private fun DateRangeLabel?.toUiText(currentStart: Long? = null): UiText {
        return when (val label = this) {
            is DateRangeLabel.Text -> UiText.DynamicString(label.value)
            is DateRangeLabel.Filter -> {
                when (label.type) {
                    DateFilterType.TODAY -> UiText.StringResourceText(Res.string.today)
                    DateFilterType.YESTERDAY -> UiText.StringResourceText(Res.string.yesterday)
                    DateFilterType.TOMORROW -> UiText.StringResourceText(Res.string.tomorrow)
                    DateFilterType.THIS_WEEK -> UiText.StringResourceText(Res.string.this_week)
                    DateFilterType.LAST_WEEK -> UiText.StringResourceText(Res.string.last_week)
                    DateFilterType.NEXT_WEEK -> UiText.StringResourceText(Res.string.next_week)
                    DateFilterType.THIS_MONTH -> UiText.StringResourceText(Res.string.this_month)
                    DateFilterType.LAST_MONTH -> UiText.StringResourceText(Res.string.last_month)
                    DateFilterType.NEXT_MONTH -> UiText.StringResourceText(Res.string.next_month)
                    DateFilterType.THIS_YEAR -> UiText.StringResourceText(Res.string.label_this_year)
                    DateFilterType.LAST_YEAR,
                    DateFilterType.NEXT_YEAR -> {
                        val year = DateFilterHelper.persianDateFromMillis(currentStart ?: 0, TimeZone.currentSystemDefault()).year
                        UiText.DynamicString(year.toLong().toPersianDigits())
                    }
                    DateFilterType.CUSTOM_RANGE -> UiText.StringResourceText(Res.string.custom_range)
                }
            }
            null -> UiText.DynamicString("")
        }
    }

    private fun getPeriodSubLabel(range: DateRange?, timeZone: TimeZone = TimeZone.currentSystemDefault()): UiText {
        if (range == null) return UiText.DynamicString("")
        
        val daysBetween = (Instant.fromEpochMilliseconds(range.end).toLocalDateTime(timeZone).date.toEpochDays() -
                Instant.fromEpochMilliseconds(range.start).toLocalDateTime(timeZone).date.toEpochDays()) + 1

        if (range.filterType == DateFilterType.CUSTOM_RANGE) {
            return UiText.StringResourceText(
                Res.string.label_day,
                listOf(daysBetween.toPersianDigits())
            )
        }

        if (range.filterType == DateFilterType.TODAY) return UiText.DynamicString("")

        val startPersian = DateFilterHelper.persianDateFromMillis(range.start, timeZone)
        val endPersian = DateFilterHelper.persianDateFromMillis(range.end, timeZone)
        
        if (startPersian.year == endPersian.year && startPersian.month == endPersian.month && startPersian.day == endPersian.day) {
            return UiText.DynamicString("")
        }
        
        if (startPersian.year == endPersian.year && startPersian.month == endPersian.month) {
             val rangeStr = "${startPersian.day.toPersianDigits()} — ${endPersian.day.toPersianDigits()} ${startPersian.persianMonth().displayName}"
             return UiText.StringResourceText(
                Res.string.label_period_range_summary,
                listOf(rangeStr, daysBetween.toPersianDigits())
            )
        }
        
        val startP = if (startPersian.year == endPersian.year) {
            "${startPersian.day.toPersianDigits()} ${startPersian.persianMonth().displayName}"
        } else {
            DateFilterHelper.getDateText(range.start, timeZone)
        }
        
        val endP = if (startPersian.year == endPersian.year) {
            "${endPersian.day.toPersianDigits()} ${endPersian.persianMonth().displayName} ${endPersian.year.toPersianDigits()}"
        } else {
            DateFilterHelper.getDateText(range.end, timeZone)
        }

        return UiText.StringResourceText(
            Res.string.label_range_days_summary,
            listOf(startP, endP, daysBetween.toPersianDigits())
        )
    }


}

data class TransactionsState(
    val selectedTransactionType: TransactionType = TransactionType.ALL,

    val dateFilterType: DateFilterType = DateFilterType.THIS_MONTH,
    val isDateSheetVisible: Boolean = false,
    val isCustomDateSheetVisible: Boolean = false,
    val isShowArrowButton: Boolean = true,
    val startDate: String? = null,
    val endDate: String? = null,
    val startDateTimeStamp: Long? = null,
    val endDateTimeStamp: Long? = null,
    val textDate: UiText = UiText.StringResourceText(Res.string.this_month),
    val textPeriodRange: UiText = UiText.DynamicString(""),

    val isSourceSheetVisible: Boolean = false,
    val selectedSources: Set<Source> = emptySet(),
    val isAllSourceSelected: Boolean = true,

    val isCategorySheetVisible: Boolean = false,
    val selectedCategories: Set<Category> = emptySet(),
    val isAllCategorySelected: Boolean = true,

    val isTagSheetVisible: Boolean = false,
    val selectedTag: Set<Tag> = emptySet(),
    val isAllTAgSelected: Boolean = true,

    val isPersonSheetVisible: Boolean = false,
    val selectedPerson: Set<Person> = emptySet(),
    val isAllPersonSelected: Boolean = true,

    val isError: Boolean = false,
    val enableAnimationChart: Boolean = true,

    val minAmountLimit: Float = 0f,
    val maxAmountLimit: Float = 1_000_000f,
    val selectedMinAmount: Float = 0f,
    val selectedMaxAmount: Float = 1_000_000f,

    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val isFilterActive: Boolean = false,
    val isFilterSheetVisible: Boolean = false,

    val showAddTransaction: Boolean = false,
    val showDeleteTransaction: Boolean = false,
    val transactionWithRelations: TransactionWithRelations? = null,
    val resetFiltersHandled: Boolean = false,
    val disabledTools: Set<ToolFeature> = emptySet()
)

sealed interface TransactionsIntent {
    data class OnTransactionTypeSelected(val type: TransactionType) : TransactionsIntent
    data class ShowTransactionBottomSheet(val transactionWithRelations: TransactionWithRelations? = null) :
        TransactionsIntent

    data class DeleteTransactionBottomSheet(val transactionWithRelations: TransactionWithRelations? = null) :
        TransactionsIntent

    data object OnToggleSourceSheet : TransactionsIntent
    data class OnSourcesSelected(
        val sources: Set<Source>,
        val isAllSourceSelected: Boolean = true
    ) : TransactionsIntent

    data object OnToggleCategorySheet : TransactionsIntent
    data class OnCategoriesSelected(
        val categories: Set<Category>,
        val isAllCategorySelected: Boolean = true
    ) : TransactionsIntent

    data object OnToggleTagSheet : TransactionsIntent
    data class OnTagSelected(
        val tag: Set<Tag>,
        val isAllTAgSelected: Boolean = true
    ) : TransactionsIntent

    data object OnTogglePersonSheet : TransactionsIntent
    data class OnPersonSelected(
        val persons: Set<Person>,
        val isAllPersonSelected: Boolean = true
    ) : TransactionsIntent

    data object OnToggleDateSheet : TransactionsIntent
    data object OnToggleCustomDateSheet : TransactionsIntent
    data class OnDateRange(val dateFilterType: DateFilterType) : TransactionsIntent
    data object OnPrevClick : TransactionsIntent
    data object OnNextClick : TransactionsIntent
    data object OnToggleSearch : TransactionsIntent
    data object OnToggleFilterSheet : TransactionsIntent
    data class OnAmountRangeChanged(val min: Float, val max: Float) : TransactionsIntent
    data object ResetFilters : TransactionsIntent
    data class ApplyFilter(
        val categoryId: Long? = null,
        val sourceId: Long? = null,
        val tagId: Long? = null,
        val personId: Long? = null,
        val transactionType: TransactionType? = null,
        val query: String? = null
    ) : TransactionsIntent

    data class OnDateSheetSubmit(
        val startDate: String?,
        val startTimeStamp: Long?,
        val endDate: String?,
        val endTimeStamp: Long?
    ) : TransactionsIntent


}
