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
                it.copy(selectedTransactionType = intent.type, selectedCategories = emptySet())
            }

            is ReportIntent.OnSourcesSelected -> _state.update {
                it.copy(selectedSources = intent.sources, isSourceSheetVisible = false)
            }

            is ReportIntent.OnCategoriesSelected -> _state.update {
                it.copy(selectedCategories = intent.categories, isCategorySheetVisible = false)
            }

            is ReportIntent.OnCustomDateStartSelected -> _state.update {
                it.copy(startDate = intent.date, startDateTimeStamp = intent.timeStamp)
            }

            is ReportIntent.OnCustomDateEndSelected -> _state.update {
                it.copy(endDate = intent.date, endDateTimeStamp = intent.timeStamp)
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
                        textDate = range?.label ?: DateFilterType.THIS_MONTH.title,
                        startDate = null,
                        endDate = null
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
                            textDate = range?.label ?: DateFilterType.CUSTOM_RANGE.title,
                            startDate = intent.startDate,
                            endDate = intent.endDate,
                            isCustomDateSheetVisible = false,
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
                        endDateTimeStamp = result.end
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
                        endDateTimeStamp = result.end
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
    val isSourceSheetVisible: Boolean = false,
    val isCategorySheetVisible: Boolean = false,
    val selectedSources: Set<Pair<Int, String>> = emptySet(),
    val selectedCategories: Set<Pair<Int, String>> = emptySet(),
    val startDate: String? = null,
    val endDate: String? = null,
    val isError: Boolean = false,
    val startDateTimeStamp: Long? = null,
    val endDateTimeStamp: Long? = null,
    val textDate: String = DateFilterType.THIS_MONTH.title,
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

    data class OnCustomDateStartSelected(val date: String, val timeStamp: Long) :
        ReportIntent

    data class OnCustomDateEndSelected(val date: String, val timeStamp: Long) : ReportIntent
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
