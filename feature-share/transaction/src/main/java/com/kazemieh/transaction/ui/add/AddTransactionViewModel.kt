package com.kazemieh.transaction.ui.add


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    private val _effect = Channel<AddTransactionEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddTransactionIntent) {
        when (intent) {
            is AddTransactionIntent.ToggleSheet -> toggleSheet(intent.sheet)
            AddTransactionIntent.PopSheet -> popSheet()
            AddTransactionIntent.ClearSheets -> clearSheets()

            is AddTransactionIntent.SetCategory -> _state.update {
                it.copy(
                    category = intent.category,
                    isCategoryError = intent.category == null,
                )
            }.also { popSheet() }

            is AddTransactionIntent.SetSource -> _state.update {
                it.copy(
                    source = intent.source,
                    isSourceError = intent.source == null,
                )
            }.also { popSheet() }

            is AddTransactionIntent.SetSourceEnd -> _state.update {
                it.copy(
                    sourceEnd = intent.source,
                    isSourceEndError = intent.source == null,
                )
            }.also { popSheet() }

            is AddTransactionIntent.SetTags -> _state.update { it.copy(tags = intent.tags) }
                .also { popSheet() }

            is AddTransactionIntent.SetPerson -> _state.update { it.copy(persons = intent.persons) }
                .also { popSheet() }

            is AddTransactionIntent.SelectedType -> onTypeChanged(intent.selectedTransactionType)

            AddTransactionIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddTransactionState() }
                    _effect.send(AddTransactionEffect.OnDismiss)
                }
            }


            is AddTransactionIntent.SetAmount -> _state.update {
                it.copy(
                    amount = intent.amount,
                    isAmountError = intent.amount.isBlank()
                )
            }

            is AddTransactionIntent.SetAmountTransfer -> _state.update {
                it.copy(amountTransfer = intent.amount)
            }


            is AddTransactionIntent.SetDate -> _state.update {
                it.copy(date = intent.date, timeStamp = intent.timeStamp)
            }

            is AddTransactionIntent.SetDescription -> _state.update { it.copy(description = intent.description) }

            AddTransactionIntent.Submit -> submitTransaction()

            is AddTransactionIntent.FetchDefaultData -> fetchDefaultData(intent.transactionWithRelations)


        }
    }

    private fun fetchDefaultData(transactionWithRelations: TransactionWithRelations?) {
        viewModelScope.launch {
            if (transactionWithRelations == null) {
                val defaultCategory =
                    transactionUseCaseGroup.getDefaultCategoryUseCase(_state.value.transactionType)
                val defaultSource = transactionUseCaseGroup.getDefaultFinancialSourceUseCase()

                _state.update {
                    it.copy(category = defaultCategory, source = defaultSource)
                }
            } else {
                _state.update {
                    it.copy(
                        oldTransaction = transactionWithRelations.transaction,
                        amount = transactionWithRelations.transaction.amount.toString(),
                        amountTransfer = transactionWithRelations.transaction.amountTransfer.toString(),
                        description = transactionWithRelations.transaction.description ?: "",
                        date = transactionWithRelations.transaction.date,
                        timeStamp = transactionWithRelations.transaction.timeStamp,
                        transactionType = transactionWithRelations.transaction.type,
                        category = transactionWithRelations.category,
                        source = transactionWithRelations.source,
                        sourceEnd = transactionWithRelations.sourceEnd,
                        tags = transactionWithRelations.tags.toSet(),
                        persons = transactionWithRelations.persons.toSet(),
                    )
                }
            }
        }
    }

    private fun submitTransaction() {
        if (_state.value.isLoading) return

        viewModelScope.launch {

            if (!validateAndUpdateErrors()) {
                _effect.send(AddTransactionEffect.ShowMessage(UiText.StringResource(R.string.fill_all_field)))
                return@launch
            }


            _state.update { it.copy(isLoading = true, sheetStack = emptyList()) }

            val current = _state.value
            val amount = current.amount.toIntOrNull()!!
            val sourceId = current.source!!.id!!
            val sourceEndId =
                if (current.transactionType == TransactionType.TRANSFER) current.sourceEnd!!.id!! else null


            if (current.transactionType == TransactionType.TRANSFER && sourceEndId == sourceId) {
                _state.update { it.copy(isLoading = false, isSourceEndError = true) }
                _effect.send(AddTransactionEffect.ShowMessage(UiText.StringResource(R.string.fill_all_field)))
                return@launch
            }

            val categoryId = if (current.transactionType == TransactionType.TRANSFER) {
                val transferCategory = transactionUseCaseGroup.getTransferCategoryUseCase()
                _state.update { it.copy(category = transferCategory) }
                transferCategory.id!!
            } else {
                current.category!!.id!!
            }

            val transaction = Transaction(
                id = current.oldTransaction?.id ?: 0L,
                amount = amount,
                amountTransfer = if (current.transactionType == TransactionType.TRANSFER)
                    (current.amountTransfer?.toIntOrNull() ?: 0)
                else 0,
                categoryId = categoryId,
                sourceId = sourceId,
                sourceEndId = sourceEndId,
                description = current.description,
                timeStamp = current.timeStamp,
                type = current.transactionType
            )

            val tagIds = current.tags.orEmpty().mapNotNull { it.id }
            val personIds = current.persons.orEmpty().mapNotNull { it.id }

            val transactionId = runCatching {
                if (current.oldTransaction != null) {
                    transactionUseCaseGroup.updateTransactionUseCase(
                        oldTransaction = current.oldTransaction,
                        newTransaction = transaction,
                        tagIds = tagIds,
                        personIds = personIds
                    )
                } else {
                    transactionUseCaseGroup.addTransactionUseCase(transaction, tagIds, personIds)
                }
            }.getOrElse {
                -1L
            }

            if (transactionId >= 0) {
                _effect.send(AddTransactionEffect.AddedTransaction)
                _state.update { AddTransactionState() }
            } else {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AddTransactionEffect.ShowMessage(UiText.StringResource(R.string.transaction_failed)))
            }
        }
    }


    private fun toggleSheet(sheet: AddTransactionSheet) {
        _state.update { s ->
            val stack = s.sheetStack
            val next = when {
                stack.lastOrNull() == sheet -> stack.dropLast(1)
                stack.contains(sheet) -> stack.filterNot { it == sheet } + sheet
                else -> stack + sheet
            }
            s.copy(sheetStack = next)
        }
    }

    private fun popSheet() {
        _state.update { s ->
            if (s.sheetStack.isEmpty()) s else s.copy(sheetStack = s.sheetStack.dropLast(1))
        }
    }

    private fun clearSheets() {
        _state.update { it.copy(sheetStack = emptyList()) }
    }

    private fun validateAndUpdateErrors(): Boolean {
        val current = _state.value

        val amount = current.amount.toIntOrNull()
        val sourceOk = current.source?.id != null
        val sourceEndOk =
            current.transactionType != TransactionType.TRANSFER || current.sourceEnd?.id != null
        val categoryOk =
            current.transactionType == TransactionType.TRANSFER || current.category?.id != null
        val amountOk = amount != null && amount > 0

        _state.update {
            it.copy(
                isAmountError = !amountOk,
                isSourceError = !sourceOk,
                isSourceEndError = !sourceEndOk,
                isCategoryError = !categoryOk,
            )
        }
        return amountOk && sourceOk && sourceEndOk && categoryOk
    }

    private fun onTypeChanged(newType: TransactionType) {
        _state.update { s ->
            val isTransfer = newType == TransactionType.TRANSFER

            s.copy(
                transactionType = newType,

                sheetStack = emptyList(),

                category = if (isTransfer) null else s.category,
                sourceEnd = if (isTransfer) s.sourceEnd else null,
                amountTransfer = if (isTransfer) s.amountTransfer else null,

                isCategoryError = false,
                isSourceError = false,
                isSourceEndError = false,
                isAmountError = false,
            )
        }
    }


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

    val sheetStack: List<AddTransactionSheet> = emptyList(),

    val oldTransaction: Transaction? = null,

    val listTransactionType: List<TransactionType> =
        listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
)


val AddTransactionState.topSheet: AddTransactionSheet?
    get() = sheetStack.lastOrNull()


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
    data object Submit : AddTransactionIntent
    data class FetchDefaultData(val transactionWithRelations: TransactionWithRelations? = null) :
        AddTransactionIntent

    data object OnDismiss : AddTransactionIntent
    data class SelectedType(val selectedTransactionType: TransactionType = TransactionType.INCOME) :
        AddTransactionIntent

    data class ToggleSheet(val sheet: AddTransactionSheet) : AddTransactionIntent
    data object PopSheet : AddTransactionIntent
    data object ClearSheets : AddTransactionIntent
}


sealed interface AddTransactionSheet {
    data object SourcePicker : AddTransactionSheet
    data object SourceEndPicker : AddTransactionSheet
    data object CategoryPicker : AddTransactionSheet
    data object TagPicker : AddTransactionSheet
    data object PersonPicker : AddTransactionSheet
}


sealed interface AddTransactionEffect {
    object AddedTransaction : AddTransactionEffect
    data class ShowMessage(val message: UiText) : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}

