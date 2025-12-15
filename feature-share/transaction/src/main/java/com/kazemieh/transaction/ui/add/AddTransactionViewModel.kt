package com.kazemieh.transaction.ui.add


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.TransactionUseCases
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

    fun onIntent(event: AddTransactionIntent) {
        when (event) {
            is AddTransactionIntent.SetAmount -> _state.update {
                it.copy(
                    amount = event.amount,
                    isAmountError = event.amount.isBlank()
                )
            }

            is AddTransactionIntent.SetAmountTransfer -> _state.update {
                it.copy(amountTransfer = event.amount)
            }

            is AddTransactionIntent.SetCategory -> _state.update {
                it.copy(
                    category = event.category,
                    isCategoryError = event.category?.name?.isBlank() == true,
                    isCategoryShow = false
                )
            }

            is AddTransactionIntent.SetSource -> _state.update {
                it.copy(
                    source = event.source,
                    isSourceError = event.source?.name?.isBlank() == true,
                    isSourceShow = false
                )
            }

            is AddTransactionIntent.SetSourceEnd -> _state.update {
                it.copy(
                    sourceEnd = event.source,
                    isSourceEndError = event.source?.name?.isBlank() == true,
                    isSourceShow = false
                )
            }

            is AddTransactionIntent.SetDate -> _state.update {
                it.copy(date = event.date, timeStamp = event.timeStamp)
            }

            is AddTransactionIntent.SetDescription -> _state.update { it.copy(description = event.description) }
            is AddTransactionIntent.SetTags -> _state.update {
                it.copy(tags = event.tags, isTagShow = false)
            }

            is AddTransactionIntent.SetPerson -> _state.update {
                it.copy(persons = event.persons, isPersonShow = false)
            }

            AddTransactionIntent.Submit -> submitTransaction()

            AddTransactionIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddTransactionState() }
                    _effect.send(AddTransactionEffect.OnDismiss)
                }
            }

            is AddTransactionIntent.SelectedType -> _state.update {
                it.copy(
                    transactionType = event.selectedTransactionType,
                    category = null,
                    sourceEnd = null,
                    amountTransfer = null
                )
            }

            AddTransactionIntent.FetchDefaultData -> fetchDefaultData()

            AddTransactionIntent.OnSourceClicked -> _state.update { it.copy(isSourceShow = !_state.value.isSourceShow) }
            AddTransactionIntent.OnSourceEndClicked -> _state.update { it.copy(isSourceEndShow = !_state.value.isSourceEndShow) }
            AddTransactionIntent.OnCategoryClicked -> _state.update { it.copy(isCategoryShow = !_state.value.isCategoryShow) }
            AddTransactionIntent.OnTagClicked -> _state.update { it.copy(isTagShow = !_state.value.isTagShow) }
            AddTransactionIntent.OnPersonClicked -> _state.update { it.copy(isPersonShow = !_state.value.isPersonShow) }
        }
    }

    private fun fetchDefaultData() {
        viewModelScope.launch {
            val defaultCategory =
                transactionUseCases.getDefaultCategoryUseCase(_state.value.transactionType)
            val defaultSource = transactionUseCases.getDefaultFinancialSourceUseCase()

            _state.update {
                it.copy(category = defaultCategory, source = defaultSource)
            }
        }
    }


    private fun submitTransaction() {
        viewModelScope.launch {
            val current = _state.value
            var categoryId = current.category?.id
            val sourceId = current.source?.id
            val sourceEndId = current.sourceEnd?.id

            if (current.transactionType == TransactionType.TRANSFER) {
                val category = transactionUseCases.getTransferCategoryUseCase()
                _state.update {
                    it.copy(category = category)
                }
                categoryId = category.id
            }

            val amount = current.amount.toIntOrNull()?.also { amount ->
                if (current.transactionType == TransactionType.EXPENSE)
                    amount.times(-1)
            }

            if (amount == null || categoryId == null || sourceId == null || (current.transactionType == TransactionType.TRANSFER && sourceEndId == null)) {
                _effect.send(AddTransactionEffect.ShowMessage(R.string.fill_all_field))
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
                categoryId = categoryId,
                financialSourceId = sourceId,
                financialSourceEndId = sourceEndId,
                description = current.description,
                timeStamp = current.timeStamp,
                type = current.transactionType
            )

            val tagsId = current.tags?.map { it.id ?: 0 } ?: emptyList()
            val personIds = current.persons?.map { it.id ?: 0 } ?: emptyList()
            val transactionId =
                transactionUseCases.addTransaction(transaction, tagsId, personIds)
            if (transactionId >= 0) {
                _effect.send(AddTransactionEffect.AddedTransaction)
                _state.update { AddTransactionState() }
            } else _effect.send(AddTransactionEffect.ShowMessage(R.string.transaction_failed))

        }
    }
}

sealed interface AddTransactionIntent {
    data class SetAmount(val amount: String) : AddTransactionIntent
    data class SetAmountTransfer(val amount: String) : AddTransactionIntent

    data class SetCategory(val category: Category? = null) : AddTransactionIntent
    data class SetSource(val source: Source? = null) : AddTransactionIntent
    data class SetSourceEnd(val source: Source? = null) : AddTransactionIntent
    data class SetTags(val tags: Set<Tag>? = null) : AddTransactionIntent
    data class SetPerson(val persons: Set<Person>? = null) : AddTransactionIntent

    data class SetDate(val date: String, val timeStamp: Long) : AddTransactionIntent
    data class SetDescription(val description: String) : AddTransactionIntent

    object Submit : AddTransactionIntent
    object FetchDefaultData : AddTransactionIntent
    data object OnDismiss : AddTransactionIntent
    data object OnSourceClicked : AddTransactionIntent
    data object OnSourceEndClicked : AddTransactionIntent
    data object OnCategoryClicked : AddTransactionIntent
    data object OnTagClicked : AddTransactionIntent
    data object OnPersonClicked : AddTransactionIntent

    data class SelectedType(val selectedTransactionType: TransactionType = TransactionType.INCOME) :
        AddTransactionIntent
}


data class AddTransactionState(
    val amount: String = "",
    val amountTransfer: String? = "",
    val description: String = "",
    val date: String? = null,
    val timeStamp: Long = System.currentTimeMillis(),
    val transactionType: TransactionType = TransactionType.INCOME,

    val category: Category? = null,
    val source: Source? = null,
    val sourceEnd: Source? = null,
    val tags: Set<Tag>? = null,
    val persons: Set<Person>? = null,

    val isAmountError: Boolean = false,
    val isCategoryError: Boolean = false,
    val isSourceError: Boolean = false,
    val isSourceEndError: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val isSourceShow: Boolean = false,
    val isSourceEndShow: Boolean = false,
    val isCategoryShow: Boolean = false,
    val isTagShow: Boolean = false,
    val isPersonShow: Boolean = false,


    val listTransactionType: List<TransactionType> =
        listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
)

sealed interface AddTransactionEffect {
    object AddedTransaction : AddTransactionEffect
    data class ShowMessage(val message: Int) : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}
