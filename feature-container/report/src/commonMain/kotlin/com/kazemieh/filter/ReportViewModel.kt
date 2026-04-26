package com.kazemieh.filter


import androidx.lifecycle.ViewModel
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
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.custom_range
import fintrack.core.designsystem.generated.resources.last_month
import fintrack.core.designsystem.generated.resources.last_week
import fintrack.core.designsystem.generated.resources.next_month
import fintrack.core.designsystem.generated.resources.next_week
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.tomorrow
import fintrack.core.designsystem.generated.resources.yesterday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone


class ReportViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    fun onIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.ShowTransactionBottomSheet -> _state.update {
                it.copy(
                    showAddTransaction = !_state.value.showAddTransaction,
                    transactionWithRelations = intent.transactionWithRelations
                )
            }

            is ReportIntent.DeleteTransactionBottomSheet -> _state.update {
                it.copy(
                    showDeleteTransaction = !_state.value.showDeleteTransaction,
                    transactionWithRelations = intent.transactionWithRelations
                )
            }

            is ReportIntent.OnTransactionTypeSelected -> _state.update {
                it.copy(
                    selectedTransactionType = intent.type,
                    selectedCategories = emptySet(),
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            ReportIntent.OnToggleSourceSheet -> _state.update {
                it.copy(isSourceSheetVisible = !it.isSourceSheetVisible)
            }

            is ReportIntent.OnSourcesSelected -> _state.update {
                it.copy(
                    selectedSources = intent.sources,
                    isSourceSheetVisible = false,
                    isAllSourceSelected = intent.isAllSourceSelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            ReportIntent.OnToggleTagSheet -> _state.update {
                it.copy(isTagSheetVisible = !it.isTagSheetVisible)
            }

            is ReportIntent.OnTagSelected -> _state.update {
                it.copy(
                    selectedTag = intent.tag,
                    isTagSheetVisible = false,
                    isAllTAgSelected = intent.isAllTAgSelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            ReportIntent.OnTogglePersonSheet -> _state.update {
                it.copy(isPersonSheetVisible = !it.isPersonSheetVisible)
            }

            is ReportIntent.OnPersonSelected -> _state.update {
                it.copy(
                    selectedPerson = intent.persons,
                    isPersonSheetVisible = false,
                    isAllPersonSelected = intent.isAllPersonSelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            ReportIntent.OnToggleCategorySheet -> _state.update {
                it.copy(isCategorySheetVisible = !it.isCategorySheetVisible)
            }

            is ReportIntent.OnCategoriesSelected -> _state.update {
                it.copy(
                    selectedCategories = intent.categories,
                    isCategorySheetVisible = false,
                    isAllCategorySelected = intent.isAllCategorySelected,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            ReportIntent.OnToggleDateSheet -> _state.update {
                it.copy(isDateSheetVisible = !it.isDateSheetVisible)
            }

            ReportIntent.OnToggleCustomDateSheet -> _state.update {
                it.copy(isCustomDateSheetVisible = !it.isCustomDateSheetVisible, isError = false)
            }

            is ReportIntent.OnDateRange -> {
//                val range = getRange(intent.dateFilterType)
                val result = getRangeAsUiText(intent.dateFilterType)
                val range = result.first
                val uiText = result.second
                _state.update {
                    it.copy(
                        dateFilterType = range?.filterType ?: DateFilterType.THIS_MONTH,
                        isDateSheetVisible = false,
                        startDateTimeStamp = range?.start,
                        endDateTimeStamp = range?.end,
                        isShowArrowButton = true,
                        textDate = uiText,
                        startDate = null,
                        endDate = null,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }
            }

            is ReportIntent.OnDateSheetSubmit -> {
                if (intent.endTimeStamp != null && intent.startTimeStamp != null &&
                    intent.endDate != null && intent.startDate != null
                ) {
                    val result = getRangeAsUiText(
                        DateFilterType.CUSTOM_RANGE,
                        intent.startTimeStamp,
                        intent.endTimeStamp
                    )
                    val range = result.first
                    val uiText = result.second

                    _state.update {
                        it.copy(
                            dateFilterType = range?.filterType ?: DateFilterType.THIS_MONTH,
                            isDateSheetVisible = false,
                            startDateTimeStamp = range?.start,
                            endDateTimeStamp = range?.end,
                            textDate = uiText,
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

            ReportIntent.OnNextClick -> {
                val result = DateFilterHelper.shiftDateRange(
                    start = state.value.startDateTimeStamp,
                    end = state.value.endDateTimeStamp,
                    filterType = state.value.dateFilterType,
                    direction = Direction.NEXT
                )
                val uiText = when (result.label) {
                    is DateRangeLabel.Text -> UiText.DynamicString((result.label as DateRangeLabel.Text).value)
                    is DateRangeLabel.Filter -> {
                        val resource = when ((result.label as DateRangeLabel.Filter).type) {
                            DateFilterType.TODAY -> Res.string.today
                            DateFilterType.YESTERDAY -> Res.string.yesterday
                            DateFilterType.TOMORROW -> Res.string.tomorrow
                            DateFilterType.THIS_WEEK -> Res.string.this_week
                            DateFilterType.LAST_WEEK -> Res.string.last_week
                            DateFilterType.NEXT_WEEK -> Res.string.next_week
                            DateFilterType.THIS_MONTH -> Res.string.this_month
                            DateFilterType.LAST_MONTH -> Res.string.last_month
                            DateFilterType.NEXT_MONTH -> Res.string.next_month
                            DateFilterType.CUSTOM_RANGE -> Res.string.custom_range
                        }
                        UiText.StringResourceText(resource)
                    }
                }
                _state.update {
                    it.copy(
                        textDate = uiText,
                        dateFilterType = result.filterType,
                        startDateTimeStamp = result.start,
                        endDateTimeStamp = result.end,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }

            }

            ReportIntent.OnPrevClick -> {
                val result = DateFilterHelper.shiftDateRange(
                    start = state.value.startDateTimeStamp,
                    end = state.value.endDateTimeStamp,
                    filterType = state.value.dateFilterType,
                    direction = Direction.PREVIOUS
                )
                val uiText = when (result.label) {
                    is DateRangeLabel.Text -> UiText.DynamicString((result.label as DateRangeLabel.Text).value)
                    is DateRangeLabel.Filter -> {
                        val resource = when ((result.label as DateRangeLabel.Filter).type) {
                            DateFilterType.TODAY -> Res.string.today
                            DateFilterType.YESTERDAY -> Res.string.yesterday
                            DateFilterType.TOMORROW -> Res.string.tomorrow
                            DateFilterType.THIS_WEEK -> Res.string.this_week
                            DateFilterType.LAST_WEEK -> Res.string.last_week
                            DateFilterType.NEXT_WEEK -> Res.string.next_week
                            DateFilterType.THIS_MONTH -> Res.string.this_month
                            DateFilterType.LAST_MONTH -> Res.string.last_month
                            DateFilterType.NEXT_MONTH -> Res.string.next_month
                            DateFilterType.CUSTOM_RANGE -> Res.string.custom_range
                        }
                        UiText.StringResourceText(resource)
                    }
                }
                _state.update {
                    it.copy(
                        textDate = uiText,
                        dateFilterType = result.filterType,
                        startDateTimeStamp = result.start,
                        endDateTimeStamp = result.end,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
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

        val uiText = when (val label = range?.label) {
            is DateRangeLabel.Text -> UiText.DynamicString(label.value)
            is DateRangeLabel.Filter -> {

                val resource = when (label.type) {
                    DateFilterType.TODAY -> Res.string.today
                    DateFilterType.YESTERDAY -> Res.string.yesterday
                    DateFilterType.TOMORROW -> Res.string.tomorrow
                    DateFilterType.THIS_WEEK -> Res.string.this_week
                    DateFilterType.LAST_WEEK -> Res.string.last_week
                    DateFilterType.NEXT_WEEK -> Res.string.next_week
                    DateFilterType.THIS_MONTH -> Res.string.this_month
                    DateFilterType.LAST_MONTH -> Res.string.last_month
                    DateFilterType.NEXT_MONTH -> Res.string.next_month
                    DateFilterType.CUSTOM_RANGE -> Res.string.custom_range
                }
                UiText.StringResourceText(resource)
            }

            null -> UiText.DynamicString("")
        }

        return Pair(range, uiText)
    }


}

data class ReportState(
    val selectedTransactionType: TransactionType = TransactionType.ALL,

    val dateFilterType: DateFilterType = DateFilterType.THIS_MONTH,
    val isDateSheetVisible: Boolean = false,
    val isCustomDateSheetVisible: Boolean = false,
    val isShowArrowButton: Boolean = true,
    val startDate: String? = null,
    val endDate: String? = null,
    val startDateTimeStamp: Long? = null,
    val endDateTimeStamp: Long? = null,
    val textDate: UiText = UiText.DynamicString(""),

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

    val showAddTransaction: Boolean = false,
    val showDeleteTransaction: Boolean = false,
    val transactionWithRelations: TransactionWithRelations? = null,
)

sealed interface ReportIntent {
    data class OnTransactionTypeSelected(val type: TransactionType) : ReportIntent
    data class ShowTransactionBottomSheet(val transactionWithRelations: TransactionWithRelations? = null) :
        ReportIntent

    data class DeleteTransactionBottomSheet(val transactionWithRelations: TransactionWithRelations? = null) :
        ReportIntent

    data object OnToggleSourceSheet : ReportIntent
    data class OnSourcesSelected(
        val sources: Set<Source>,
        val isAllSourceSelected: Boolean = true
    ) : ReportIntent

    data object OnToggleCategorySheet : ReportIntent
    data class OnCategoriesSelected(
        val categories: Set<Category>,
        val isAllCategorySelected: Boolean = true
    ) : ReportIntent

    data object OnToggleTagSheet : ReportIntent
    data class OnTagSelected(
        val tag: Set<Tag>,
        val isAllTAgSelected: Boolean = true
    ) : ReportIntent

    data object OnTogglePersonSheet : ReportIntent
    data class OnPersonSelected(
        val persons: Set<Person>,
        val isAllPersonSelected: Boolean = true
    ) : ReportIntent

    data object OnToggleDateSheet : ReportIntent
    data object OnToggleCustomDateSheet : ReportIntent
    data class OnDateRange(val dateFilterType: DateFilterType) : ReportIntent
    data object OnPrevClick : ReportIntent
    data object OnNextClick : ReportIntent
    data class OnDateSheetSubmit(
        val startDate: String?,
        val startTimeStamp: Long?,
        val endDate: String?,
        val endTimeStamp: Long?
    ) : ReportIntent


}
