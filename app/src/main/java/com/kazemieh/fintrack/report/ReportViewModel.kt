package com.kazemieh.fintrack.report


import androidx.lifecycle.ViewModel
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class ReportViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ReportFilterState())
    val state = _state.asStateFlow()

    fun onIntent(intent: ReportFilterIntent) {
        when (intent) {
            is ReportFilterIntent.OnTransactionTypeSelected -> _state.update {
                it.copy(selectedTransactionType = intent.type, selectedCategories = emptySet())
            }

            is ReportFilterIntent.OnSourcesSelected -> _state.update {
                it.copy(selectedSources = intent.sources, isSourceSheetVisible = false)
            }

            is ReportFilterIntent.OnCategoriesSelected -> _state.update {
                it.copy(selectedCategories = intent.categories, isCategorySheetVisible = false)
            }

            is ReportFilterIntent.OnDateFilterSelected -> {

                val range = DateFilterHelper.getRange(intent.filter)
                _state.update {
                    it.copy(
                        selectedDateFilter = intent.filter,
                        isDateSheetVisible = false,
                        timeStampRangeStart = range?.fromTimestamp,
                        timeStampRangeEnd = range?.toTimestamp,
                        customRangeStart = null,
                        customRangeEnd = null
                    )
                }
            }

            is ReportFilterIntent.OnCustomRangeStartSelected -> _state.update {
                it.copy(customRangeStart = intent.date, timeStampRangeStart = intent.timeStamp)
            }

            is ReportFilterIntent.OnCustomRangeEndSelected -> _state.update {
                it.copy(customRangeEnd = intent.date, timeStampRangeEnd = intent.timeStamp)
            }

            ReportFilterIntent.OnToggleSourceSheet -> _state.update {
                it.copy(isSourceSheetVisible = !it.isSourceSheetVisible)
            }

            ReportFilterIntent.OnToggleCategorySheet -> _state.update {
                it.copy(isCategorySheetVisible = !it.isCategorySheetVisible)
            }

            ReportFilterIntent.OnToggleDateSheet -> _state.update {
                it.copy(isDateSheetVisible = !it.isDateSheetVisible)
            }

            ReportFilterIntent.OnToggleCustomDateSheet -> _state.update {
                it.copy(
                    isCustomDateSheetVisible = !it.isCustomDateSheetVisible,
                    timeStampRangeEnd = null,
                    timeStampRangeStart = null,
                    customRangeEnd = null,
                    customRangeStart = null
                )
            }


            ReportFilterIntent.OnDateSheetSubmit -> {
                if (
                    _state.value.timeStampRangeEnd != null &&
                    _state.value.timeStampRangeStart != null &&
                    _state.value.customRangeEnd != null &&
                    _state.value.customRangeStart != null
                ) {
                    _state.update {
                        it.copy(isCustomDateSheetVisible = false)
                    }
                    // todo add custom date to change filter
                }

                _state.update {
                    it.copy(isCustomDateSheetVisible = false, isDateSheetVisible = false)
                }
            }
        }
    }
}

data class ReportFilterState(
    val selectedTransactionType: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val selectedSources: Set<Pair<Int, String>> = emptySet(),
    val selectedCategories: Set<Pair<Int, String>> = emptySet(),
    val selectedDateFilter: DateFilterType = DateFilterType.NEXT_MONTH,
    val customRangeStart: String? = null,
    val customRangeEnd: String? = null,
    val timeStampRangeStart: Long? = null,
    val timeStampRangeEnd: Long? = null,
    val isDateSheetVisible: Boolean = false,
    val isCustomDateSheetVisible: Boolean = false,
    val isCustomDateEndSheetVisible: Boolean = false,
    val isSourceSheetVisible: Boolean = false,
    val isCategorySheetVisible: Boolean = false
)

enum class TransactionTypeFilter(val count: Int) { INCOME(1), EXPENSE(2), ALL(0) }


sealed interface ReportFilterIntent {
    data class OnTransactionTypeSelected(val type: TransactionTypeFilter) : ReportFilterIntent
    data class OnSourcesSelected(val sources: Set<Pair<Int, String>> = emptySet()) :
        ReportFilterIntent

    data class OnCategoriesSelected(val categories: Set<Pair<Int, String>>) : ReportFilterIntent
    data object OnToggleSourceSheet : ReportFilterIntent
    data object OnToggleCategorySheet : ReportFilterIntent
    data object OnToggleDateSheet : ReportFilterIntent
    data object OnToggleCustomDateSheet : ReportFilterIntent
    data object OnDateSheetSubmit : ReportFilterIntent
    data class OnDateFilterSelected(val filter: DateFilterType) : ReportFilterIntent
    data class OnCustomRangeStartSelected(val date: String, val timeStamp: Long) :
        ReportFilterIntent

    data class OnCustomRangeEndSelected(val date: String, val timeStamp: Long) : ReportFilterIntent
}
