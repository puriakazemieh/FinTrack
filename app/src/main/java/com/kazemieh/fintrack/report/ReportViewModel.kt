package com.kazemieh.fintrack.report


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class ReportViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ReportFilterState())
    val state = _state.asStateFlow()

    fun onIntent(intent: ReportFilterIntent) {
        when (intent) {
            is ReportFilterIntent.OnTransactionTypeSelected -> _state.update {
                it.copy(selectedTransactionType = intent.type)
            }

            is ReportFilterIntent.OnSourcesSelected -> _state.update {
                it.copy(selectedSources = intent.sources, isSourceSheetVisible = false)
            }

            is ReportFilterIntent.OnCategoriesSelected -> _state.update {
                it.copy(selectedCategories = intent.categories, isCategorySheetVisible = false)
            }

            is ReportFilterIntent.OnPeriodChanged -> _state.update {
                it.copy(selectedPeriod = intent.period)
            }

            ReportFilterIntent.OnToggleSourceSheet -> _state.update {
                it.copy(isSourceSheetVisible = !it.isSourceSheetVisible)
            }

            ReportFilterIntent.OnToggleCategorySheet -> _state.update {
                it.copy(isCategorySheetVisible = !it.isCategorySheetVisible)
            }
        }
    }
}

data class ReportFilterState(
    val selectedTransactionType: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val selectedSources: Set<Pair<Int, String>> = emptySet(),
    val selectedCategories: Set<Pair<Int, String>> = emptySet(),
    val selectedPeriod: ReportPeriod = ReportPeriod.ThisMonth,
    val isSourceSheetVisible: Boolean = false,
    val isCategorySheetVisible: Boolean = false
)

enum class TransactionTypeFilter { INCOME, EXPENSE, ALL }
enum class ReportPeriod { ThisMonth, LastMonth, Custom }


sealed interface ReportFilterIntent {
    data class OnTransactionTypeSelected(val type: TransactionTypeFilter) : ReportFilterIntent
    data class OnSourcesSelected(val sources: Set<Pair<Int, String>> = emptySet()) : ReportFilterIntent
    data class OnCategoriesSelected(val categories: Set<Pair<Int, String>>) : ReportFilterIntent
    data class OnPeriodChanged(val period: ReportPeriod) : ReportFilterIntent
    data object OnToggleSourceSheet : ReportFilterIntent
    data object OnToggleCategorySheet : ReportFilterIntent
}
