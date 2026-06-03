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
import com.kazemieh.common.SnackbarController
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.resolveString
import fintrack.core.designsystem.generated.resources.*
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

    private val _effect = Channel<AddTransactionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddTransactionIntent) {
        when (intent) {
            is AddTransactionIntent.SetAmount -> _state.update { it.copy(amount = intent.amount, isAmountError = false) }
            is AddTransactionIntent.SetAmountTransfer -> _state.update { it.copy(amountTransfer = intent.amount) }
            is AddTransactionIntent.SetCategory -> _state.update {
                it.copy(
                    category = intent.category,
                    isCategoryError = false,
                    sheetStack = it.sheetStack.dropLast(1)
                )
            }

            is AddTransactionIntent.SetSource -> _state.update {
                it.copy(
                    source = intent.source,
                    isSourceError = false,
                    sheetStack = it.sheetStack.dropLast(1)
                )
            }

            is AddTransactionIntent.SetSourceEnd -> _state.update {
                it.copy(
                    sourceEnd = intent.source,
                    isSourceEndError = false,
                    sheetStack = it.sheetStack.dropLast(1)
                )
            }

            is AddTransactionIntent.SetTags -> _state.update {
                it.copy(
                    tags = intent.tags,
                    sheetStack = it.sheetStack.dropLast(1)
                )
            }

            is AddTransactionIntent.SetPerson -> _state.update {
                it.copy(
                    persons = intent.persons,
                    sheetStack = it.sheetStack.dropLast(1)
                )
            }
            is AddTransactionIntent.SetDate -> _state.update {
                it.copy(
                    date = intent.date,
                    timeStamp = intent.timeStamp,
                    sheetStack = it.sheetStack.dropLast(1)
                )
            }
            is AddTransactionIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            AddTransactionIntent.Submit -> submitTransaction()
            is AddTransactionIntent.FetchDefaultData -> fetchDefaultData(intent.transactionWithRelations)
            AddTransactionIntent.OnDismiss -> viewModelScope.launch { _effect.send(AddTransactionEffect.OnDismiss) }
            is AddTransactionIntent.SelectedType -> onTypeChanged(intent.selectedTransactionType)
            is AddTransactionIntent.ToggleSheet -> toggleSheet(intent.sheet)
            AddTransactionIntent.PopSheet -> popSheet()
            AddTransactionIntent.ClearSheets -> clearSheets()
        }
    }

    private fun fetchDefaultData(transactionWithRelations: TransactionWithRelations?) {
        if (transactionWithRelations == null) {
            val today = com.kazemieh.designsystem.component.jalali.JalaliCalendar()
            _state.value = AddTransactionState(
                date = "${today.day.toFa()} / ${today.monthString} / ${today.year.toFa()}",
                timeStamp = today.toTimestamp()
            ) // Full reset first with today's date
            viewModelScope.launch {
                val defaultSource = transactionUseCaseGroup.getDefaultFinancialSourceUseCase()
                val defaultCategory = transactionUseCaseGroup.getDefaultCategoryUseCase(_state.value.transactionType)
                _state.update {
                    it.copy(
                        source = defaultSource,
                        category = defaultCategory
                    )
                }
            }
            return
        }
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
                persons = transactionWithRelations.persons.toSet()
            )
        }
    }

    private fun submitTransaction() {
        if (!validateAndUpdateErrors()) return

        val current = _state.value
        val amount = current.amount.toIntOrNull() ?: 0
        val amountTransfer = current.amountTransfer?.toIntOrNull() ?: 0

        val transaction = Transaction(
            id = current.oldTransaction?.id ?: 0L,
            amount = amount,
            amountTransfer = amountTransfer,
            description = current.description,
            date = current.date ?: "",
            timeStamp = current.timeStamp,
            type = current.transactionType,
            categoryId = current.category?.id ?: 0,
            sourceId = current.source?.id ?: 0,
            sourceEndId = current.sourceEnd?.id
        )

        val tagIds = current.tags?.mapNotNull { it.id } ?: emptyList()
        val personIds = current.persons?.mapNotNull { it.id } ?: emptyList()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val id = if (current.oldTransaction == null) {
                transactionUseCaseGroup.addTransactionUseCase(transaction, tagIds, personIds)
            } else {
                transactionUseCaseGroup.updateTransactionUseCase(current.oldTransaction, transaction, tagIds, personIds)
            }

            if (id > 0) {
                _effect.send(AddTransactionEffect.AddedTransaction)
            } else {
                _state.update { it.copy(isLoading = false) }
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.transaction_failed).resolveString())
            }
        }
    }

    private fun toggleSheet(sheet: AddTransactionSheet) {
        _state.update { it.copy(sheetStack = it.sheetStack + sheet) }
    }

    private fun popSheet() {
        _state.update { it.copy(sheetStack = it.sheetStack.dropLast(1)) }
    }

    private fun clearSheets() {
        _state.update { it.copy(sheetStack = emptyList()) }
    }

    private fun validateAndUpdateErrors(): Boolean {
        val current = _state.value
        val amount = current.amount.toLongOrNull() ?: 0L
        val sourceOk = current.source?.id != null
        val sourceEndOk = current.transactionType != TransactionType.TRANSFER || current.sourceEnd?.id != null
        val categoryOk = current.transactionType == TransactionType.TRANSFER || current.category?.id != null
        val amountOk = amount > 0

        _state.update {
            it.copy(
                isAmountError = !amountOk,
                isSourceError = !sourceOk,
                isSourceEndError = !sourceEndOk,
                isCategoryError = !categoryOk
            )
        }

        if (!amountOk || !sourceOk || !sourceEndOk || !categoryOk) {
            viewModelScope.launch {
                val msg = if (!amountOk) Res.string.fill_all_field
                else if (!categoryOk) Res.string.category_choose
                else Res.string.source_choose
                SnackbarController.showMessage(UiText.StringResourceText(msg).resolveString())
            }
            return false
        }
        return true
    }

    private fun onTypeChanged(type: TransactionType) {
        viewModelScope.launch {
            val defaultCategory = if (type == TransactionType.TRANSFER) null else transactionUseCaseGroup.getDefaultCategoryUseCase(type)
            _state.update {
                it.copy(
                    transactionType = type,
                    category = defaultCategory,
                    isCategoryError = false
                )
            }
        }
    }
}

data class AddTransactionState(
    val amount: String = "",
    val amountTransfer: String? = null,
    val description: String = "",
    val date: String? = null,
    val timeStamp: Long = 0,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val category: Category? = null,
    val source: Source? = null,
    val sourceEnd: Source? = null,
    val tags: Set<Tag>? = emptySet(),
    val persons: Set<Person>? = emptySet(),
    val isAmountError: Boolean = false,
    val isCategoryError: Boolean = false,
    val isSourceError: Boolean = false,
    val isSourceEndError: Boolean = false,
    val isLoading: Boolean = false,
    val sheetStack: List<AddTransactionSheet> = emptyList(),
    val oldTransaction: Transaction? = null,
    val listTransactionType: List<TransactionType> = TransactionType.entries
)

val AddTransactionState.topSheet: AddTransactionSheet?
    get() = sheetStack.lastOrNull()

sealed interface AddTransactionIntent {
    data class SetAmount(val amount: String) : AddTransactionIntent
    data class SetAmountTransfer(val amount: String) : AddTransactionIntent
    data class SetCategory(val category: Category?) : AddTransactionIntent
    data class SetSource(val source: Source?) : AddTransactionIntent
    data class SetSourceEnd(val source: Source?) : AddTransactionIntent
    data class SetTags(val tags: Set<Tag>?) : AddTransactionIntent
    data class SetPerson(val persons: Set<Person>?) : AddTransactionIntent
    data class SetDate(val date: String, val timeStamp: Long) : AddTransactionIntent
    data class SetDescription(val description: String) : AddTransactionIntent
    data object Submit : AddTransactionIntent
    data class FetchDefaultData(val transactionWithRelations: TransactionWithRelations?) : AddTransactionIntent
    data object OnDismiss : AddTransactionIntent
    data class SelectedType(val selectedTransactionType: TransactionType) : AddTransactionIntent
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
    data object DatePicker : AddTransactionSheet
}

sealed interface AddTransactionEffect {
    data object AddedTransaction : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}
