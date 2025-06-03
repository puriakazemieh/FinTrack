package com.kazemieh.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.Transaction
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
            transactionUseCases.getAllTransactions().collect { result ->
                _state.update {
                    it.copy(
                        transactions = result,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionUseCases.deleteTransaction(transaction)
            _effect.send(TransactionEffect.ShowMessage(R.string.delete_transaction))
            loadTransactions()
        }
    }
}
