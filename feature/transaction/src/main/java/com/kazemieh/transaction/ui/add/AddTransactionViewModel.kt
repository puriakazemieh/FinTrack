package com.kazemieh.transaction.ui.add


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.Transaction
import com.kazemieh.model.TransactionType
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
            is AddTransactionEvent.SetAmount -> _state.update {
                it.copy(
                    amount = event.amount,
                    isAmountError = event.amount.isBlank()
                )
            }

            is AddTransactionEvent.SetAmountTransfer -> _state.update {
                it.copy(amountTransfer = event.amount)
            }

            is AddTransactionEvent.SetCategory -> _state.update {
                it.copy(
                    category = event.category,
                    isCategoryError = event.category?.second?.isBlank() == true
                )
            }

            is AddTransactionEvent.SetSource -> _state.update {
                it.copy(
                    source = event.source,
                    isSourceError = event.source?.second?.isBlank() == true
                )
            }

            is AddTransactionEvent.SetSourceEnd -> _state.update {
                it.copy(
                    sourceEnd = event.source,
                    isSourceEndError = event.source?.second?.isBlank() == true
                )
            }

            is AddTransactionEvent.SetDate -> _state.update {
                it.copy(selectedDate = event.date, timeStamp = event.timeStamp)
            }

            is AddTransactionEvent.SetDescription -> _state.update { it.copy(description = event.description) }
            is AddTransactionEvent.SetTags -> _state.update { it.copy(tags = event.tags) }
            is AddTransactionEvent.SetPerson -> _state.update { it.copy(persons = event.persons) }

            AddTransactionEvent.Submit -> submitTransaction()

            AddTransactionEvent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddTransactionState() }
                    _effect.send(AddTransactionEffect.OnDismiss)
                }
            }

            is AddTransactionEvent.SelectedType -> _state.update {
                it.copy(
                    selectedTransactionType = event.selectedTransactionType,
                    category = null,
                    sourceEnd = null,
                    amountTransfer = null
                )
            }

            AddTransactionEvent.FetchDefaultData -> fetchDefaultData()
        }
    }

    private fun fetchDefaultData() {
        viewModelScope.launch {
            val defaultCategory =
                transactionUseCases.getDefaultCategoryUseCase(TransactionType.INCOME.count)
            val defaultSource = transactionUseCases.getDefaultFinancialSourceUseCase()

            val category = if (defaultCategory.id != null) {
                defaultCategory.id!!.toInt() to defaultCategory.name
            } else null

            val source = if (defaultSource.id != null) {
                defaultSource.id!!.toInt() to defaultSource.name
            } else null

            _state.update {
                it.copy(
                    category = category,
                    source = source
                )
            }
        }
    }


    private fun submitTransaction() {
        viewModelScope.launch {
            val current = _state.value
            var categoryId = current.category?.first
            val sourceId = current.source?.first
            val sourceEndId = current.sourceEnd?.first

            if (current.selectedTransactionType == TransactionType.TRANSFER) {
                val category = transactionUseCases.getTransferCategoryUseCase()
                _state.update {
                    it.copy(category = (category.id?.toInt() ?: 1) to category.name)
                }
                categoryId = category.id?.toInt() ?: 1
            }

            val amount = current.amount.toIntOrNull()?.also { amount ->
                if (current.selectedTransactionType == TransactionType.EXPENSE)
                    amount.times(-1)
            }

            if (amount == null || categoryId == null || sourceId == null || (current.selectedTransactionType == TransactionType.TRANSFER && sourceEndId == null)) {
                _effect.send(AddTransactionEffect.Error("لطفاً تمام فیلدها را پر کنید."))
                _state.update {
                    it.copy(
                        isSourceError = sourceId == null,
                        isCategoryError = categoryId == null,
                        isAmountError = amount == null,
                        isSourceEndError = sourceEndId == null
                    )
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            val transaction = Transaction(
                id = 0L,
                amount = amount,
                amountTransfer = current.amountTransfer?.toIntOrNull() ?: 0,
                categoryId = categoryId.toLong(),
                financialSourceId = sourceId.toLong(),
                financialSourceEndId = sourceEndId?.toLong(),
                description = current.description,
                timeStamp = current.timeStamp,
                type = current.selectedTransactionType
            )

            try {
                val tagsId = current.tags?.map { it.first.toLong() } ?: emptyList()
                val personIds = current.persons?.map { it.first.toLong() } ?: emptyList()
                val transactionId =
                    transactionUseCases.addTransaction(transaction, tagsId, personIds)
                if (transactionId >= 0) {
                    _effect.send(AddTransactionEffect.Success)
                    _state.update { AddTransactionState() }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AddTransactionEffect.Error(e.message ?: "خطا در ثبت تراکنش"))
            }
        }
    }
}

sealed interface AddTransactionEvent {
    data class SetAmount(val amount: String) : AddTransactionEvent
    data class SetAmountTransfer(val amount: String) : AddTransactionEvent

    data class SetCategory(val category: Pair<Int, String>? = null) : AddTransactionEvent
    data class SetSource(val source: Pair<Int, String>? = null) : AddTransactionEvent
    data class SetSourceEnd(val source: Pair<Int, String>? = null) : AddTransactionEvent
    data class SetTags(val tags: Set<Pair<Int, String>>? = null) : AddTransactionEvent
    data class SetPerson(val persons: Set<Pair<Int, String>>? = null) : AddTransactionEvent

    data class SetDate(val date: String, val timeStamp: Long) : AddTransactionEvent
    data class SetDescription(val description: String) : AddTransactionEvent

    object Submit : AddTransactionEvent
    object FetchDefaultData : AddTransactionEvent
    data object OnDismiss : AddTransactionEvent

    data class SelectedType(val selectedTransactionType: TransactionType = TransactionType.INCOME) :
        AddTransactionEvent
}


data class AddTransactionState(
    val amount: String = "",
    val amountTransfer: String? = "",
    val description: String = "",
    val selectedDate: String? = null,
    val timeStamp: Long = System.currentTimeMillis(),

    val category: Pair<Int, String>? = null,
    val source: Pair<Int, String>? = null,
    val sourceEnd: Pair<Int, String>? = null,
    val tags: Set<Pair<Int, String>>? = null,
    val persons: Set<Pair<Int, String>>? = null,

    val isAmountError: Boolean = false,
    val isCategoryError: Boolean = false,
    val isSourceError: Boolean = false,
    val isSourceEndError: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val selectedTransactionType: TransactionType = TransactionType.INCOME,

    val listTransactionType: List<TransactionType> =
        listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
)

sealed interface AddTransactionEffect {
    object Success : AddTransactionEffect
    data class Error(val message: String) : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}
