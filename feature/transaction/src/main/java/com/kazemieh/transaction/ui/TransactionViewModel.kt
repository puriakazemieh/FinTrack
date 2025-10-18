package com.kazemieh.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.formatted
import com.kazemieh.common.toPositive
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.transaction.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private val _effect = Channel<TransactionEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onEvent(TransactionEvent.LoadTransactions)
    }

    fun onEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.LoadTransactions -> {
                loadTransactions()
            }

            is TransactionEvent.DeleteTransaction -> {
                deleteTransaction(event.transaction)
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCases.getAllTransactions().collect { transactions ->

                var totalIncome = 0
                var totalExpense = 0
                var balance = 0

                val uiTransactionWithRelations = transactions.map { transactionWithRelations ->
                    if (transactionWithRelations.transaction.amount > 0)
                        totalIncome += transactionWithRelations.transaction.amount
                    else totalExpense += transactionWithRelations.transaction.amount
                    balance += transactionWithRelations.transaction.amount
                    transactionWithRelations.copy()
                    transactionWithRelations.transaction.toUi()
                    transactionWithRelations.toUi()
                }


                _state.update {
                    it.copy(
                        uiTransactionWithRelations = uiTransactionWithRelations,
                        formatedTotalIncome = totalIncome.formatted(),
                        totalIncome = totalIncome.toPositive().toLong(),
                        totalExpense = totalExpense.toPositive().toLong(),
                        formatedTotalExpense = totalExpense.formatted(),
                        balance = balance.formatted(),
                        isPositiveBalance = balance >= 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionUi) {
        viewModelScope.launch {
            transactionUseCases.deleteTransaction(transaction.toDomain())
            _effect.send(TransactionEffect.ShowMessage(R.string.transaction_deleted))
            loadTransactions()
        }
    }
}

sealed interface TransactionEvent {
    object LoadTransactions : TransactionEvent
    data class DeleteTransaction(val transaction: TransactionUi) : TransactionEvent
}

data class TransactionState(
    val uiTransactionWithRelations: List<TransactionWithRelationsUi> = emptyList(),
    val isLoading: Boolean = false,
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,
    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,
    val pieChartItems: List<PieChartItem> = listOf(),
    val error: String? = null
)

sealed interface TransactionEffect {
    data class ShowMessage(val message: Int) : TransactionEffect
}
