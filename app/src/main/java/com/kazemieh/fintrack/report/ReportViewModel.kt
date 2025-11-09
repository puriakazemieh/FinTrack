package com.kazemieh.fintrack.report


import androidx.lifecycle.ViewModel
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.Direction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class ReportViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    fun onIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.OnTransactionTypeSelected -> _state.update {
                it.copy(
                    selectedTransactionType = intent.type,
                    selectedCategories = emptySet(),
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            is ReportIntent.OnSourcesSelected -> _state.update {
                it.copy(
                    selectedSources = intent.sources, isSourceSheetVisible = false,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            is ReportIntent.OnCategoriesSelected -> _state.update {
                it.copy(
                    selectedCategories = intent.categories, isCategorySheetVisible = false,
                    enableAnimationChart = !_state.value.enableAnimationChart
                )
            }

            ReportIntent.OnToggleSourceSheet -> _state.update {
                it.copy(isSourceSheetVisible = !it.isSourceSheetVisible)
            }

            ReportIntent.OnToggleCategorySheet -> _state.update {
                it.copy(isCategorySheetVisible = !it.isCategorySheetVisible)
            }

            ReportIntent.OnToggleDateSheet -> _state.update {
                it.copy(isDateSheetVisible = !it.isDateSheetVisible)
            }

            ReportIntent.OnToggleCustomDateSheet -> _state.update {
                it.copy(isCustomDateSheetVisible = !it.isCustomDateSheetVisible, isError = false)
            }

            is ReportIntent.OnDateRange -> {
                val range = DateFilterHelper.getRange(intent.dateFilterType)

                _state.update {
                    it.copy(
                        dateFilterType = range?.filterType ?: DateFilterType.THIS_MONTH,
                        isDateSheetVisible = false,
                        startDateTimeStamp = range?.start,
                        endDateTimeStamp = range?.end,
                        isShowArrowButton = true,
                        textDate = range?.label ?: DateFilterType.THIS_MONTH.titleResId,
                        startDate = null,
                        endDate = null,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }
            }

            is ReportIntent.OnDateSheetSubmit -> {
                if (
                    intent.endTimeStamp != null &&
                    intent.startTimeStamp != null &&
                    intent.endDate != null &&
                    intent.startDate != null
                ) {
                    val range = DateFilterHelper.getRange(
                        DateFilterType.CUSTOM_RANGE,
                        intent.startTimeStamp,
                        intent.endTimeStamp
                    )
                    _state.update {
                        it.copy(
                            dateFilterType = range?.filterType ?: DateFilterType.THIS_MONTH,
                            isDateSheetVisible = false,
                            startDateTimeStamp = range?.start,
                            endDateTimeStamp = range?.end,
                            textDate = range?.label ?: DateFilterType.CUSTOM_RANGE.titleResId,
                            startDate = intent.startDate,
                            endDate = intent.endDate,
                            isShowArrowButton = false,
                            isCustomDateSheetVisible = false,
                            enableAnimationChart = !_state.value.enableAnimationChart
                        )
                    }
                } else {
                    _state.update {
                        it.copy(isError = true)
                    }
                }

            }

            ReportIntent.OnNextClick -> {
                val result = DateFilterHelper.shiftDateRange(
                    start = state.value.startDateTimeStamp,
                    end = state.value.endDateTimeStamp,
                    filterType = state.value.dateFilterType,
                    direction = Direction.NEXT
                )
                _state.update {
                    it.copy(
                        textDate = result.label,
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
                _state.update {
                    it.copy(
                        textDate = result.label,
                        dateFilterType = result.filterType,
                        startDateTimeStamp = result.start,
                        endDateTimeStamp = result.end,
                        enableAnimationChart = !_state.value.enableAnimationChart
                    )
                }

            }


        }
    }


}

data class ReportState(
    val selectedTransactionType: Int = 0,
    val dateFilterType: DateFilterType = DateFilterType.THIS_MONTH,
    val isDateSheetVisible: Boolean = false,
    val isCustomDateSheetVisible: Boolean = false,
    val isShowArrowButton: Boolean = true,
    val isSourceSheetVisible: Boolean = false,
    val isCategorySheetVisible: Boolean = false,
    val selectedSources: Set<Pair<Int, String>> = emptySet(),
    val selectedCategories: Set<Pair<Int, String>> = emptySet(),
    val startDate: String? = null,
    val endDate: String? = null,
    val isError: Boolean = false,
    val startDateTimeStamp: Long? = null,
    val endDateTimeStamp: Long? = null,
    val enableAnimationChart: Boolean = true,
    val textDate: Any = DateFilterType.THIS_MONTH.titleResId,
)


sealed interface ReportIntent {
    data class OnTransactionTypeSelected(val type: Int) : ReportIntent
    data object OnToggleSourceSheet : ReportIntent
    data object OnToggleCategorySheet : ReportIntent
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

    data class OnSourcesSelected(val sources: Set<Pair<Int, String>> = emptySet()) :
        ReportIntent

    data class OnCategoriesSelected(val categories: Set<Pair<Int, String>>) : ReportIntent


}
