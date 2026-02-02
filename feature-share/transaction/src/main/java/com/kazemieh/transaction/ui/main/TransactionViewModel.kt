package com.kazemieh.transaction.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kazemieh.common.formatted
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

    val uiTransactionWithRelations: Flow<PagingData<TransactionWithRelations>> =
        transactionUseCases.getAllTransactionsFiltered(TransactionFilterParams())
            .cachedIn(viewModelScope)

    init {
        observeSummary()
    }

    private fun observeSummary() {
        viewModelScope.launch {
            runCatching {
                transactionUseCases.getCategorySum(TransactionFilterParams())
                    .collect { categorySums ->
                        val totalIncome = categorySums.filter { it.type == TransactionType.INCOME }
                            .sumOf { it.totalAmount }
                        val totalExpense =
                            categorySums.filter { it.type == TransactionType.EXPENSE }
                                .sumOf { it.totalAmount }
                        val totalTransfer =
                            categorySums.filter { it.type == TransactionType.TRANSFER }
                                .sumOf { it.totalAmount }

                        val balance = totalIncome + totalExpense.times(-1) + totalTransfer

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
                            )
                        }
                    }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
    }


}


data class TransactionState(
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,
    val formatedTotalTransfer: String = "0",
    val totalTransfer: Long = 0,
    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,
    val error: String? = null
)

