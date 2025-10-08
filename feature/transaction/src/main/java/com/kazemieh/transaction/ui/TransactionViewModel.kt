package com.kazemieh.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionWithRelations
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
                val totalIncome = transactions
                    .filter { it.transaction.amount > 0 }
                    .sumOf { it.transaction.amount }

                val totalExpense = transactions
                    .filter { it.transaction.amount < 0 }
                    .sumOf { it.transaction.amount }

                val balance = totalIncome + totalExpense

                val newBalance =
                    if (balance < 0) "- ${balance * -1}" else if (balance > 0) "+ $balance" else "0"

                val newTotalExpense = if (totalExpense < 0) "- ${totalExpense * -1}" else "0"

                val newTotalIncome = if (totalIncome > 0) "+ $totalIncome" else "0"

                _state.update {
                    it.copy(
                        transactions = transactions,
                        totalIncome = newTotalIncome,
                        totalExpense = newTotalExpense,
                        balance = newBalance,
                        isPositiveBalance = balance >= 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionUseCases.deleteTransaction(transaction)
            _effect.send(TransactionEffect.ShowMessage(R.string.transaction_deleted))
            loadTransactions()
        }
    }
}

sealed interface TransactionEvent {
    object LoadTransactions : TransactionEvent
    data class DeleteTransaction(val transaction: Transaction) : TransactionEvent
}

data class TransactionState(
    val transactions: List<TransactionWithRelations> = emptyList(),
    val isLoading: Boolean = false,
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val totalIncome: String = "0",
    val totalExpense: String = "0",
    val error: String? = null
)

sealed interface TransactionEffect {
    data class ShowMessage(val message: Int) : TransactionEffect
}
