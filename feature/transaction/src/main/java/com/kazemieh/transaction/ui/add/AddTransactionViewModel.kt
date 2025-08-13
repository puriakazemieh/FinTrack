package com.kazemieh.transaction.ui.add


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.Transaction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
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

    init {
        onEvent(AddTransactionEvent.LoadInitialData)
    }

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.SetAmount -> _state.update { it.copy(amount = event.amount) }
            is AddTransactionEvent.SetCategory -> _state.update { it.copy(selectedCategory = event.category) }
            is AddTransactionEvent.SetFinancialSource -> _state.update {
                it.copy(
                    selectedFinancialSource = event.financialSource
                )
            }

            is AddTransactionEvent.SetDate -> _state.update { it.copy(selectedDate = event.date) }
            is AddTransactionEvent.SetDescription -> _state.update { it.copy(description = event.description) }
            is AddTransactionEvent.ToggleTag -> {
                val current = _state.value.selectedTags
                val updated = if (event.tag in current) current - event.tag else current + event.tag
                _state.update { it.copy(selectedTags = updated) }
            }

            AddTransactionEvent.Submit -> submitTransaction()
            AddTransactionEvent.LoadInitialData -> loadInitialData()
            is AddTransactionEvent.SetIsIncome -> _state.update { it.copy(isIncome = event.isIncome) }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categories = transactionUseCases.getAllCategory()
            val sources = transactionUseCases.getAllFinancialSource()
            val tags = transactionUseCases.getAllTag()
            combine(categories, sources, tags) { categories, sources, tags ->
                _state.update { currentState ->
                    currentState.copy(
                        categories = categories,
                        financialSources = sources,
                        tags = tags
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun submitTransaction() {
        val current = _state.value
        val amount = current.amount.toDoubleOrNull()
        val categoryId = current.selectedCategory?.id
        val sourceId = current.selectedFinancialSource?.id

        if (amount == null || categoryId == null || sourceId == null) {
            viewModelScope.launch {
                _effect.send(AddTransactionEffect.Error("لطفاً تمام فیلدها را پر کنید."))
            }
            return
        }

        val transaction = Transaction(
            id = 0L,
            amount = amount,
            categoryId = categoryId,
            financialSourceId = sourceId,
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
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AddTransactionEffect.Error(e.message ?: "خطا در ثبت تراکنش"))
            }
        }
    }
}
