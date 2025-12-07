package com.kazemieh.transaction.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.kazemieh.common.formatted
import com.kazemieh.common.model.CategorySum
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.transaction.ui.component.TransactionUi
import com.kazemieh.transaction.ui.component.TransactionWithRelationsUi
import com.kazemieh.transaction.ui.component.toDomain
import com.kazemieh.transaction.ui.component.toUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class TransactionReportViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionReportState())
    val state: StateFlow<TransactionReportState> = _state.asStateFlow()

    private val filterParamsFlow: Flow<TransactionFilterParams> = state
        .map { state ->
            TransactionFilterParams(
                type = if (state.filterParams.type == 0) null else state.filterParams.type,
                selectedCategories = state.filterParams.selectedCategories,
                selectedSource = state.filterParams.selectedSource,
                selectedTags = state.filterParams.selectedTags,
                selectedPersons = state.filterParams.selectedPersons,
                fromTimestamp = state.filterParams.fromTimestamp,
                toTimestamp = state.filterParams.toTimestamp?.plus(24.hours.inWholeMilliseconds)
            )
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )


    val uiTransactionWithRelations: Flow<PagingData<TransactionWithRelationsUi>> =
        filterParamsFlow
            .flatMapLatest { params ->
                transactionUseCases.getAllTransactionsFiltered(
                    type = params.type,
                    categoryIds = params.selectedCategories.map { it.first },
                    sourceIds = params.selectedSource.map { it.first },
                    tagIds = params.selectedTags.map { it.first },
                    personIds = params.selectedPersons.map { it.first },
                    fromTimestamp = params.fromTimestamp,
                    toTimestamp = params.toTimestamp
                ).map { pagingData -> pagingData.map { it.toUi() } }
            }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            filterParamsFlow
                .onStart { _state.update { it.copy(isLoading = true) } }
                .flatMapLatest { params ->
                    transactionUseCases.getCategorySum(
                        type = params.type,
                        categoryIds = params.selectedCategories.map { it.first },
                        sourceIds = params.selectedSource.map { it.first },
                        tagIds = params.selectedTags.map { it.first },
                        personIds = params.selectedPersons.map { it.first },
                        fromTimestamp = params.fromTimestamp,
                        toTimestamp = params.toTimestamp
                    )
                }
                .collectLatest { categorySums ->
                    updateCategorySums(categorySums)
                }

        }

    }

    fun onIntent(intent: TransactionReportIntent) {
        when (intent) {

            is TransactionReportIntent.DeleteTransactionReport -> deleteTransaction(intent.transaction)

            is TransactionReportIntent.SelectedType -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            type = intent.selectedTransactionType
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedSource -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            selectedSource = intent.selectedSource
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedCategory -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            selectedCategories = intent.selectedCategories
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedTag -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            selectedTags = intent.selectedTag
                        )
                    )
                }
            }

            is TransactionReportIntent.SelectedPerson -> {
                _state.update { state ->
                    state.copy(
                        filterParams = state.filterParams.copy(
                            selectedPersons = intent.selectedPerson
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
                isLoading = false,
                pieChartData = pieChartItems
            )
        }
    }

    private fun deleteTransaction(transaction: TransactionUi) {
        viewModelScope.launch {
            transactionUseCases.deleteTransaction(transaction.toDomain())
        }
    }

}

sealed interface TransactionReportIntent {
    data class DeleteTransactionReport(val transaction: TransactionUi) : TransactionReportIntent

    data class SelectedType(val selectedTransactionType: Int = 0) : TransactionReportIntent

    data class SelectedSource(val selectedSource: Set<Pair<Int, String>> = emptySet()) :
        TransactionReportIntent

    data class SelectedTag(val selectedTag: Set<Pair<Int, String>> = emptySet()) :
        TransactionReportIntent

    data class SelectedPerson(val selectedPerson: Set<Pair<Int, String>> = emptySet()) :
        TransactionReportIntent

    data class SelectedCategory(val selectedCategories: Set<Pair<Int, String>> = emptySet()) :
        TransactionReportIntent

    data class SelectedDate(val fromTimestamp: Long? = null, val toTimestamp: Long? = null) :
        TransactionReportIntent
}

data class TransactionReportState(
    val filterParams: TransactionFilterParams = TransactionFilterParams(),
    val isLoading: Boolean = false,
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,
    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,
    val pieChartData: List<PieChartItem> = listOf(),
    val error: String? = null,
)

data class TransactionFilterParams(
    val type: Int? = null,
    val selectedSource: Set<Pair<Int, String>> = emptySet(),
    val selectedCategories: Set<Pair<Int, String>> = emptySet(),
    val selectedTags: Set<Pair<Int, String>> = emptySet(),
    val selectedPersons: Set<Pair<Int, String>> = emptySet(),
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null
)

enum class TransactionFilterType(val count: Int) {
    ALL(0),
    INCOME(1),
    EXPENSE(2);

    companion object {
        fun fromInt(value: Int) = entries.first { it.count == value }
    }
}