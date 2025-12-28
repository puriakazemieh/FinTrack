package com.kazemieh.transaction.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kazemieh.common.formatted
import com.kazemieh.common.ld
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPositive
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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

    val uiTransactionWithRelations: Flow<PagingData<TransactionWithRelations>> =
        transactionUseCases.getAllTransactionsFiltered(TransactionFilterParams())
            .cachedIn(viewModelScope)

    fun onIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.LoadTransactions -> loadTransactions()

            is TransactionIntent.OnDeleteClicked ->
                _state.update {
                    it.copy(
                        transaction = intent.transaction,
                        isDeleteDialogShow = true
                    )
                }

            is TransactionIntent.OnEditClicked ->
                _state.update {
                    it.copy(
                        transaction = intent.transaction,
                        isEditBottomSheetShow = true
                    )
                }

            TransactionIntent.LoadEditCanceled -> _state.update {
                it.copy(
                    transaction = null,
                    isEditBottomSheetShow = false
                )
            }

            TransactionIntent.OnDeleteCanceled -> _state.update {
                it.copy(
                    transaction = null,
                    isDeleteDialogShow = false
                )
            }

            TransactionIntent.OnDeleteConfirmed -> deleteTransaction()
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCases.getCategorySum(TransactionFilterParams()).collect { categorySums ->

                val totalIncome = categorySums.filter { it.type == TransactionType.INCOME }
                    .sumOf { it.totalAmount }.ld("totalIncome")
                val totalExpense = categorySums.filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.totalAmount }.ld("totalExpense")
                val totalTransfer = categorySums.filter { it.type == TransactionType.TRANSFER }
                    .sumOf { it.totalAmount }.ld("totalTransfer")
                val balance = categorySums.sumOf { it.totalAmount }


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

    private fun deleteTransaction() {
        viewModelScope.launch {
            _state.value.transaction?.let { transactionUseCases.deleteTransaction(it) }
            _effect.send(TransactionEffect.ShowMessage(R.string.transaction_deleted))
        }
    }

}

sealed interface TransactionIntent {
    data class OnDeleteClicked(val transaction: Transaction) : TransactionIntent
    data class OnEditClicked(val transaction: Transaction) : TransactionIntent
    data object OnDeleteConfirmed : TransactionIntent
    data object OnDeleteCanceled : TransactionIntent
    data object LoadEditCanceled : TransactionIntent
    data object LoadTransactions : TransactionIntent
}

data class TransactionState(

    val transaction: Transaction? = null,
    val isDeleteDialogShow: Boolean = false,
    val isEditBottomSheetShow: Boolean = false,

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
