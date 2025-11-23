package com.kazemieh.transaction.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.formatted
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.transaction.ui.component.TransactionUi
import com.kazemieh.transaction.ui.component.TransactionWithRelationsUi
import com.kazemieh.transaction.ui.component.toDomain
import com.kazemieh.transaction.ui.component.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class TransactionReportViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionReportState())
    val state: StateFlow<TransactionReportState> = _state.asStateFlow()

    fun onIntent(intent: TransactionReportIntent) {
        when (intent) {
            is TransactionReportIntent.LoadTransactionsByFilter -> loadTransactionsByFilter()

            is TransactionReportIntent.DeleteTransactionReport -> deleteTransaction(intent.transaction)

            is TransactionReportIntent.SelectedType -> {
                _state.update {
                    it.copy(selectedTransactionType = intent.selectedTransactionType)
                }
                loadTransactionsByFilter()
            }

            is TransactionReportIntent.SelectedSource -> {
                _state.update {
                    it.copy(selectedSource = intent.selectedSource)
                }
                loadTransactionsByFilter()
            }

            is TransactionReportIntent.SelectedCategory -> {
                _state.update {
                    it.copy(selectedCategories = intent.selectedCategories)
                }
                loadTransactionsByFilter()
            }

            is TransactionReportIntent.SelectedTag -> {
                _state.update {
                    it.copy(selectedTags = intent.selectedTag)
                }
                loadTransactionsByFilter()
            }

            is TransactionReportIntent.SelectedPerson -> {
                _state.update {
                    it.copy(selectedPersons = intent.selectedPerson)
                }
                loadTransactionsByFilter()
            }

            is TransactionReportIntent.SelectedDate -> {
                _state.update {
                    it.copy(fromTimestamp = intent.fromTimestamp, toTimestamp = intent.toTimestamp)
                }
                loadTransactionsByFilter()
            }
        }
    }

    private fun loadTransactionsByFilter() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCases.getAllTransactionsFiltered(
                type = if (state.value.selectedTransactionType == 0) null else state.value.selectedTransactionType,
                categoryIds = state.value.selectedCategories.map { it.first },
                sourceIds = state.value.selectedSource.map { it.first },
                tagIds = state.value.selectedTags.map { it.first },
                personIds = state.value.selectedPersons.map { it.first },
                fromTimestamp = state.value.fromTimestamp,
                toTimestamp = state.value.toTimestamp?.plus(24.hours.inWholeMilliseconds)
            )
                .collect { transactions ->

                    val groupedByCategory = transactions.groupBy { it.category }
                    val pieChartItems = groupedByCategory.map { (category, items) ->
                        val totalAmount = items.sumOf { it.transaction.amount }
                        PieChartItem(
                            id = category.id,
                            label = category.name,
                            value = totalAmount.toLong()
                        )
                    }

                    val balance = transactions.sumOf { it.transaction.amount }

                    val uiTransactionWithRelations = transactions.map { it.toUi() }

                    _state.update {
                        it.copy(
                            uiTransactionWithRelations = uiTransactionWithRelations,
                            balance = balance.formatted(),
                            isPositiveBalance = balance >= 0,
                            isLoading = false,
                            pieChartData = pieChartItems
                        )
                    }
                }
        }
    }

    private fun deleteTransaction(transaction: TransactionUi) {
        viewModelScope.launch {
            transactionUseCases.deleteTransaction(transaction.toDomain())
            loadTransactionsByFilter()
        }
    }

}

sealed interface TransactionReportIntent {
    data class DeleteTransactionReport(val transaction: TransactionUi) : TransactionReportIntent
    data object LoadTransactionsByFilter : TransactionReportIntent

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
    val uiTransactionWithRelations: List<TransactionWithRelationsUi> = emptyList(),
    val isLoading: Boolean = false,
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,
    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null,
    val selectedTransactionType: Int = 0,
    val selectedSource: Set<Pair<Int, String>> = emptySet(),
    val selectedCategories: Set<Pair<Int, String>> = emptySet(),
    val selectedTags: Set<Pair<Int, String>> = emptySet(),
    val selectedPersons: Set<Pair<Int, String>> = emptySet(),
    val pieChartData: List<PieChartItem> = listOf(),
    val error: String? = null
)


enum class TransactionFilterType(val count: Int) {
    ALL(0),
    INCOME(1),
    EXPENSE(2);

    companion object {
        fun fromInt(value: Int) = entries.first { it.count == value }
    }
}