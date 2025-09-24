package com.kazemieh.transaction.ui.add


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.Transaction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    private val _effect = Channel<AddTransactionEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.SetAmount -> _state.update { it.copy(amount = event.amount) }
            is AddTransactionEvent.SetCategory -> _state.update { it.copy(category = event.category) }
            is AddTransactionEvent.SetSource -> _state.update { it.copy(source = event.setSource) }
            is AddTransactionEvent.SetDate -> _state.update { it.copy(selectedDate = event.date) }
            is AddTransactionEvent.SetDescription -> _state.update { it.copy(description = event.description) }
            is AddTransactionEvent.ToggleTag -> {
                val current = _state.value.selectedTags
                val updated = if (event.tag in current) current - event.tag else current + event.tag
                _state.update { it.copy(selectedTags = updated) }
            }

            AddTransactionEvent.Submit -> submitTransaction()
            is AddTransactionEvent.SetIsIncome -> _state.update { it.copy(isIncome = event.isIncome) }
            AddTransactionEvent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddTransactionState() }
                    _effect.send(AddTransactionEffect.OnDismiss)
                }
            }
        }
    }


    private fun submitTransaction() {
        val current = _state.value
        val amount = current.amount.toIntOrNull()?.times(if (current.isIncome) 1 else -1)
        val categoryId = current.category?.first
        val sourceId = current.source?.first

        if (amount == null || categoryId == null || sourceId == null) {
            viewModelScope.launch {
                _effect.send(AddTransactionEffect.Error("لطفاً تمام فیلدها را پر کنید."))
            }
            return
        }

        val transaction = Transaction(
            id = 0L,
            amount = amount,
            categoryId = categoryId.toLong(),
            financialSourceId = sourceId.toLong(),
            description = current.description,
            date = current.selectedDate
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                transactionUseCases.addTransaction(
                    transaction,
                    current.selectedTags.filter { it.id != null }.map { it.id!! })
                _state.update { it.copy(isLoading = false, isSuccess = true) }
                _effect.send(AddTransactionEffect.Success)
                _state.update { AddTransactionState() }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AddTransactionEffect.Error(e.message ?: "خطا در ثبت تراکنش"))
            }
        }
    }
}
