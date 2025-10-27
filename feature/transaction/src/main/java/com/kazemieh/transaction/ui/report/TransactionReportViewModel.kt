package com.kazemieh.transaction.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.formatted
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.TransactionType
import com.kazemieh.transaction.ui.component.TransactionUi
import com.kazemieh.transaction.ui.component.TransactionWithRelationsUi
import com.kazemieh.transaction.ui.component.toDomain
import com.kazemieh.transaction.ui.component.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

        }
    }

    private fun loadTransactionsByFilter() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCases.getAllTransactionsByType(_state.value.selectedTransactionType.count)
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

    data class SelectedType(val selectedTransactionType: TransactionFilterType = TransactionFilterType.ALL) :
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
    val selectedTransactionType: TransactionFilterType = TransactionFilterType.ALL,
    val pieChartData: List<PieChartItem> = listOf(),
    val error: String? = null
)


enum class TransactionFilterType(val count: Int)  {
    ALL(0),
    INCOME(1),
    EXPENSE(2);

    companion object {
        fun fromInt(value: Int) = TransactionFilterType.entries.first { it.count == value }
    }
}