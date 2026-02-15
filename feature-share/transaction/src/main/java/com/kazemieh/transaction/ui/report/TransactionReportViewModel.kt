package com.kazemieh.transaction.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kazemieh.common.formatted
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class TransactionReportViewModel(
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionReportState())
    val state: StateFlow<TransactionReportState> = _state.asStateFlow()

    private val filterParamsFlow: Flow<TransactionFilterParams> = state
        .map { state ->
            TransactionFilterParams(
                type = if (state.filterParams.type == 0) null else state.filterParams.type,
                categories = state.filterParams.categories,
                sources = state.filterParams.sources,
                tags = state.filterParams.tags,
                persons = state.filterParams.persons,
                fromTimestamp = state.filterParams.fromTimestamp,
                toTimestamp = state.filterParams.toTimestamp?.plus(24.hours.inWholeMilliseconds)
            )
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            replay = 1
        )


    val uiTransactionWithRelations: Flow<PagingData<TransactionWithRelations>> =
        filterParamsFlow
            .flatMapLatest { params -> transactionUseCaseGroup.observeTransactionsUseCase(params) }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            filterParamsFlow
                .flatMapLatest { params -> transactionUseCaseGroup.observeCategorySumsUseCase(params) }
                .collectLatest { categorySums -> updateCategorySums(categorySums) }
        }

    }

    fun onIntent(intent: TransactionReportIntent) {
        when (intent) {

            is TransactionReportIntent.SelectedType -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            type = intent.selectedTransactionType.count
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedSource -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            sources = intent.selectedSource
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedCategory -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            categories = intent.selectedCategories
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedTag -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            tags = intent.selectedTag
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedPerson -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            persons = intent.selectedPerson
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedDate -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            fromTimestamp = intent.fromTimestamp,
                            toTimestamp = intent.toTimestamp
                        )
                    )
                }
            }

            is TransactionReportIntent.SetFilters -> {
                _state.update { s ->
                    s.copy(
                        filterParams = s.filterParams.copy(
                            sources = intent.sources,
                            categories = intent.categories,
                            tags = intent.tags,
                            persons = intent.persons,
                            type = intent.type.count,
                            fromTimestamp = intent.fromTimestamp,
                            toTimestamp = intent.toTimestamp
                        )
                    )
                }
            }

        }
    }


    private fun updateCategorySums(categorySums: List<CategorySum>) {
        var balance: Long = 0
        val pieChartItems = categorySums.map { category ->
            balance += category.totalAmount
            PieChartItem(
                id = category.categoryId,
                label = category.name,
                value = category.totalAmount
            )
        }

        _state.update {
            it.copy(
                balance = balance.toInt().formatted(),
                isPositiveBalance = balance >= 0,
                pieChartData = pieChartItems
            )
        }
    }

}

sealed interface TransactionReportIntent {

    data class SelectedType(val selectedTransactionType: TransactionType = TransactionType.ALL) :
        TransactionReportIntent

    data class SelectedSource(val selectedSource: Set<Source> = emptySet()) :
        TransactionReportIntent

    data class SelectedTag(val selectedTag: Set<Tag> = emptySet()) :
        TransactionReportIntent

    data class SelectedPerson(val selectedPerson: Set<Person> = emptySet()) :
        TransactionReportIntent

    data class SelectedCategory(val selectedCategories: Set<Category> = emptySet()) :
        TransactionReportIntent

    data class SelectedDate(val fromTimestamp: Long? = null, val toTimestamp: Long? = null) :
        TransactionReportIntent

    data class SetFilters(
        val sources: Set<Source>,
        val categories: Set<Category>,
        val tags: Set<Tag>,
        val persons: Set<Person>,
        val type: TransactionType,
        val fromTimestamp: Long?,
        val toTimestamp: Long?,
    ) : TransactionReportIntent
}

data class TransactionReportState(
    val filterParams: TransactionFilterParams = TransactionFilterParams(),
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,
    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,
    val pieChartData: List<PieChartItem> = listOf(),
    val error: String? = null,
)

