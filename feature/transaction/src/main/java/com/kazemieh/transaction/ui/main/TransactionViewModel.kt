package com.kazemieh.transaction.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.formatted
import com.kazemieh.common.toPositive
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.transaction.ui.component.TransactionUi
import com.kazemieh.transaction.ui.component.TransactionWithRelationsUi
import com.kazemieh.transaction.ui.component.toDomain
import com.kazemieh.transaction.ui.component.toUi
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

    fun onIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.LoadTransactions -> loadTransactions()

            is TransactionIntent.DeleteTransaction -> deleteTransaction(intent.transaction)

        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCases.getAllTransactions().collect { transactions ->

                var totalIncome = 0
                var totalExpense = 0
                var totalTransfer = 0
                var balance = 0

                val uiTransactionWithRelations = transactions.map { transactionWithRelations ->

                    when(transactionWithRelations.transaction.type){
                        TransactionType.INCOME -> { totalIncome += transactionWithRelations.transaction.amount}
                        TransactionType.EXPENSE -> {totalExpense += transactionWithRelations.transaction.amount}
                        TransactionType.TRANSFER -> {totalTransfer += transactionWithRelations.transaction.amount}
                        else -> {}
                    }
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
                        formatedTotalExpense = totalExpense.formatted(),
                        totalExpense = totalExpense.toPositive().toLong(),
                        formatedTotalTransfer = totalTransfer.formatted(),
                        totalTransfer = totalTransfer.toPositive().toLong(),
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

sealed interface TransactionIntent {
    data class DeleteTransaction(val transaction: TransactionUi) : TransactionIntent
    data object LoadTransactions : TransactionIntent
}

data class TransactionState(
    val uiTransactionWithRelations: List<TransactionWithRelationsUi> = emptyList(),

    val isLoading: Boolean = false,

    val balance: String = "0",
    val isPositiveBalance: Boolean = true,

    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,

    val formatedTotalTransfer: String = "0",
    val totalTransfer: Long = 0,

    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,

    val selectedTransactionType: TransactionType = TransactionType.INCOME,
    val pieChartData: List<PieChartItem> = listOf(),
    val error: String? = null
)

sealed interface TransactionEffect {
    data class ShowMessage(val message: Int) : TransactionEffect
}
