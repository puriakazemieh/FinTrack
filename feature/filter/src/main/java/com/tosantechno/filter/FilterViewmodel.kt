package com.tosantechno.filter

import androidx.lifecycle.ViewModel
import com.kazemieh.common.DateFilterHelper
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.Direction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class FilterViewModel() : ViewModel() {

    private val _state = MutableStateFlow(FilterState())
    val state = _state.asStateFlow()


    fun onIntent(intent: FilterIntent) {
        when (intent) {
            FilterIntent.OnNextClick -> {
                if (state.value.startDateTimeStamp != null &&  state.value.endDateTimeStamp != null) {
                    val result  = DateFilterHelper.shiftDateRange(
                        start = state.value.startDateTimeStamp!!,
                        end = state.value.endDateTimeStamp!!,
                        filterType = state.value.selectedDateFilter,
                        direction = Direction.NEXT
                    )
                    _state.update {
                        it.copy(
                            textDate = result.label,
                            selectedDateFilter = result.filterType,
                            startDateTimeStamp = result.start,
                            endDateTimeStamp = result.end
                        )
                    }
                }
            }

            FilterIntent.OnPrevClick -> {
                if (state.value.startDateTimeStamp != null &&  state.value.endDateTimeStamp != null) {
                    val result  = DateFilterHelper.shiftDateRange(
                        start = state.value.startDateTimeStamp!!,
                        end = state.value.endDateTimeStamp!!,
                        filterType = state.value.selectedDateFilter,
                        direction = Direction.PREVIOUS
                    )
                    _state.update {
                        it.copy(
                            textDate = result.label,
                            selectedDateFilter = result.filterType,
                            startDateTimeStamp = result.start,
                            endDateTimeStamp = result.end
                        )
                    }
                }
            }

            is FilterIntent.OnSetDate -> {
                if (intent.startDateTimeStamp != null && intent.endDateTimeStamp != null) {
                    val textDate = DateFilterHelper.getDateRangeText(
                        intent.startDateTimeStamp,
                        intent.endDateTimeStamp,
                        intent.dateFilterType
                    )
                    _state.update {
                        it.copy(
                            textDate = textDate,
                            selectedDateFilter = intent.dateFilterType,
                            startDateTimeStamp = intent.startDateTimeStamp,
                            endDateTimeStamp = intent.endDateTimeStamp
                        )
                    }
                }
            }
        }
    }


}


data class FilterState(
    val textDate: String = DateFilterType.THIS_MONTH.title,
    val selectedDateFilter: DateFilterType = DateFilterType.NEXT_MONTH,
    val startDateTimeStamp: Long? = null,
    val endDateTimeStamp: Long? = null,

    )


sealed interface FilterIntent {
    data object OnPrevClick : FilterIntent
    data object OnNextClick : FilterIntent
    data class OnSetDate(
        val startDateTimeStamp: Long?,
        val endDateTimeStamp: Long?,
        val dateFilterType: DateFilterType
    ) : FilterIntent
}
