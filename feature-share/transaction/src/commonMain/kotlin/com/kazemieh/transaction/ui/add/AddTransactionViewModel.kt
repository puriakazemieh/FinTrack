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
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_mandatory_fields_error
import fintrack.core.designsystem.generated.resources.transaction_failed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModel(
    private val transactionUseCaseGroup: TransactionUseCaseGroup,
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    private val _effect = Channel<AddTransactionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _typeFlow = MutableStateFlow<TransactionType?>(TransactionType.EXPENSE)

    init {
        _typeFlow
            .flatMapLatest { type ->
                transactionUseCaseGroup.observeMostUsedCategoriesUseCase(type)
            }
            .onEach { categories ->
                _state.update { it.copy(mostUsedCategories = categories) }
            }
            .launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedSourcesUseCase()
            .onEach { sources ->
                _state.update { it.copy(mostUsedSources = sources) }
            }
            .launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedTagsUseCase()
            .onEach { tags ->
                _state.update { it.copy(mostUsedTags = tags) }
            }
            .launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedPersonsUseCase()
            .onEach { persons ->
                _state.update { it.copy(mostUsedPersons = persons) }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: AddTransactionIntent) {
        when (intent) {
            is AddTransactionIntent.SetAmount -> _state.update {
                it.copy(
                    amount = intent.amount,
                    isAmountError = false
                )
            }

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
            AddTransactionIntent.Submit -> {
                if (validateAndUpdateErrors()) {
                    submitTransaction()
                } else {
                    viewModelScope.launch {
                        SnackbarController.showMessage(UiText.StringResourceText(Res.string.msg_mandatory_fields_error))
                    }
                }
            }

            is AddTransactionIntent.FetchDefaultData -> fetchDefaultData(intent.transactionWithRelations)
            AddTransactionIntent.OnDismiss -> viewModelScope.launch {
                _effect.send(
                    AddTransactionEffect.OnDismiss
                )
            }

            is AddTransactionIntent.SelectedType -> onTypeChanged(intent.selectedTransactionType)
            is AddTransactionIntent.ToggleSheet -> toggleSheet(intent.sheet)
            AddTransactionIntent.PopSheet -> popSheet()
            AddTransactionIntent.ClearSheets -> clearSheets()
            AddTransactionIntent.Delete -> deleteTransaction()
        }
    }

    private fun deleteTransaction() {
        val transaction = _state.value.oldTransaction ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transactionUseCaseGroup.deleteTransactionUseCase(transaction)
            _state.update { it.copy(isLoading = false) }
            _effect.send(AddTransactionEffect.AddedTransaction)
        }
    }

    private fun fetchDefaultData(transactionWithRelations: TransactionWithRelations?) {
        if (transactionWithRelations == null) {
            val today = JalaliCalendar()
            _state.value = AddTransactionState(
                date = "${today.day.toPersianDigits()} / ${today.monthString} / ${today.year.toPersianDigits()}",
                timeStamp = today.toTimestamp(),
                mostUsedCategories = _state.value.mostUsedCategories,
                mostUsedSources = _state.value.mostUsedSources,
                mostUsedTags = _state.value.mostUsedTags,
                mostUsedPersons = _state.value.mostUsedPersons
            ) // Full reset first with today's date
            viewModelScope.launch {
                val defaultSource = transactionUseCaseGroup.getDefaultFinancialSourceUseCase()
                val defaultCategory =
                    transactionUseCaseGroup.getDefaultCategoryUseCase(_state.value.transactionType)
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
        _typeFlow.value = transactionWithRelations.transaction.type
    }

    private fun submitTransaction() {
        val current = _state.value
        val transaction = Transaction(
            id = current.oldTransaction?.id ?: 0,
            amount = current.amount.toIntOrNull() ?: 0,
            amountTransfer = current.amountTransfer?.toIntOrNull() ?: 0,
            categoryId = current.category?.id ?: 0,
            sourceId = current.source?.id ?: 0,
            sourceEndId = current.sourceEnd?.id,
            description = current.description,
            timeStamp = current.timeStamp,
            type = current.transactionType,
            date = current.date ?: ""
        )

        val tagIds = current.tags?.mapNotNull { it.id } ?: emptyList()
        val personIds = current.persons?.mapNotNull { it.id } ?: emptyList()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val id = if (current.oldTransaction == null) {
                transactionUseCaseGroup.addTransactionUseCase(transaction, tagIds, personIds)
            } else {
                transactionUseCaseGroup.updateTransactionUseCase(
                    current.oldTransaction,
                    transaction,
                    tagIds,
                    personIds
                )
            }
            _state.update { it.copy(isLoading = false) }
            if (id > 0) {
                _effect.send(AddTransactionEffect.AddedTransaction)
            } else {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.transaction_failed))
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
        val isAmountError = current.amount.toLongOrNull() == null || current.amount.toLong() <= 0
        val isCategoryError =
            current.transactionType != TransactionType.TRANSFER && current.category == null
        val isSourceError = current.source == null
        val isSourceEndError =
            current.transactionType == TransactionType.TRANSFER && current.sourceEnd == null

        _state.update {
            it.copy(
                isAmountError = isAmountError,
                isCategoryError = isCategoryError,
                isSourceError = isSourceError,
                isSourceEndError = isSourceEndError
            )
        }

        return !isAmountError && !isCategoryError && !isSourceError && !isSourceEndError
    }

    private fun onTypeChanged(type: TransactionType) {
        _typeFlow.value = type
        viewModelScope.launch {
            val defaultCategory = transactionUseCaseGroup.getDefaultCategoryUseCase(type)
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
    val listTransactionType: List<TransactionType> = TransactionType.entries,
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val mostUsedTags: List<Tag> = emptyList(),
    val mostUsedPersons: List<Person> = emptyList()
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
    data class FetchDefaultData(val transactionWithRelations: TransactionWithRelations?) :
        AddTransactionIntent

    data object OnDismiss : AddTransactionIntent
    data class SelectedType(val selectedTransactionType: TransactionType) : AddTransactionIntent
    data class ToggleSheet(val sheet: AddTransactionSheet) : AddTransactionIntent
    data object PopSheet : AddTransactionIntent
    data object ClearSheets : AddTransactionIntent
    data object Delete : AddTransactionIntent
}

sealed interface AddTransactionSheet {
    data object SourcePicker : AddTransactionSheet
    data object SourceEndPicker : AddTransactionSheet
    data object CategoryPicker : AddTransactionSheet
    data object TagPicker : AddTransactionSheet
    data object PersonPicker : AddTransactionSheet
    data object DatePicker : AddTransactionSheet
    data object DeleteConfirmation : AddTransactionSheet
}

sealed interface AddTransactionEffect {
    data object AddedTransaction : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}
