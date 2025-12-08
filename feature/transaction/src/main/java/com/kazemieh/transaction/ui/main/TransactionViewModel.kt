package com.kazemieh.transaction.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.kazemieh.common.formatted
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPositive
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.transaction.ui.component.TransactionUi
import com.kazemieh.transaction.ui.component.TransactionWithRelationsUi
import com.kazemieh.transaction.ui.component.toDomain
import com.kazemieh.transaction.ui.component.toUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    val uiTransactionWithRelations: Flow<PagingData<TransactionWithRelationsUi>> =
        transactionUseCases.getAllTransactionsFiltered().cachedIn(viewModelScope)
            .map { it.map { it.toUi() } }

    fun onIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.LoadTransactions -> loadTransactions()

            is TransactionIntent.DeleteTransaction -> deleteTransaction(intent.transaction)

        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCases.getCategorySum().collect { categorySums ->

                var totalIncome: Long = 0
                var totalExpense: Long = 0
                var totalTransfer: Long = 0
                var balance: Long = 0


                categorySums.map { category ->

                    balance += category.totalAmount
                    when (category.type) {
                        TransactionType.INCOME.count -> {
                            totalIncome += category.totalAmount
                        }

                        TransactionType.EXPENSE.count -> {
                            totalExpense += category.totalAmount
                        }

                        TransactionType.TRANSFER.count -> {
                            totalTransfer += category.totalAmount
                        }

                        else -> {}
                    }
                }

                _state.update {
                    it.copy(
                        formatedTotalIncome = totalIncome.toInt().formatted(),
                        totalIncome = totalIncome.toInt().toPositive().toLong(),
                        formatedTotalExpense = totalExpense.toInt().formatted(),
                        totalExpense = totalExpense.toInt().toPositive().toLong(),
                        formatedTotalTransfer = totalTransfer.toInt().formatted(),
                        totalTransfer = totalTransfer.toInt().toPositive().toLong(),
                        balance = balance.toInt().formatted(),
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
