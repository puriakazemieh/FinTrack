package com.kazemieh.transaction.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.ImageStorage
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.jalali.JalaliCalendar
import com.kazemieh.money.Currency
import com.kazemieh.preferences.FinTrackPreferences
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_mandatory_fields_error
import fintrack.core.designsystem.generated.resources.transaction_failed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val transactionUseCaseGroup: TransactionUseCaseGroup,
    private val imageStorage: ImageStorage,
    private val smsDraftRepository: SmsDraftRepository,
    private val goalRepository: com.kazemieh.domain.repository.GoalRepository,
    private val preferenceUseCases: com.kazemieh.domain.usecase.PreferenceUseCases
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

            is AddTransactionIntent.SetTime -> _state.update {
                val tz = TimeZone.currentSystemDefault()
                val pdt = Instant.fromEpochMilliseconds(it.timeStamp).toPersianDateTime(tz)
                val newTs = pdt.copy(hour = intent.hour, minute = intent.minute, second = 0).toEpochMilliseconds(tz)
                it.copy(timeStamp = newTs)
            }

            is AddTransactionIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddTransactionIntent.SetPhoto -> _state.update { it.copy(photoBytes = intent.bytes) }
            AddTransactionIntent.Submit -> {
                if (validateAndUpdateErrors()) {
                    submitTransaction()
                } else {
                    viewModelScope.launch {
                        SnackbarController.showMessage(UiText.StringResourceText(Res.string.msg_mandatory_fields_error))
                    }
                }
            }

            is AddTransactionIntent.FetchDefaultData -> fetchDefaultData(
                intent.transactionWithRelations,
                intent.smsDraft
            )
            is AddTransactionIntent.PrefillFromTemplate -> prefillFromTemplate(intent.template)
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
            transaction.photoPath?.let { imageStorage.deleteImage(it) }
            transactionUseCaseGroup.deleteTransactionUseCase(transaction)
            
            analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionDeleted)
            
            _state.update { it.copy(isLoading = false) }
            _effect.send(AddTransactionEffect.AddedTransaction)
        }
    }

    private fun fetchDefaultData(
        transactionWithRelations: TransactionWithRelations?,
        smsDraft: SmsDraft? = null
    ) {
        if (transactionWithRelations == null) {
            val today = JalaliCalendar()
            val currencyValue = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_CURRENCY, "")
            val currency = Currency.valueOf(currencyValue)
            val prefilledAmount = smsDraft?.amount?.let { 
                if (currency.code == "IRT") (it / 10).toString() else it.toString()
            } ?: ""
            
            _state.update { 
                it.copy(
                    date = "${today.day.toPersianDigits()} / ${today.monthString} / ${today.year.toPersianDigits()}",
                    timeStamp = today.toTimestamp(),
                    smsDraft = smsDraft,
                    amount = prefilledAmount,
                    transactionType = smsDraft?.type ?: TransactionType.EXPENSE
                )
            }
            viewModelScope.launch {
                val defaultSource = transactionUseCaseGroup.getDefaultFinancialSourceUseCase()
                val detectedSource = if (smsDraft?.sourceId != null) {
                    transactionUseCaseGroup.observeSourceUseCase(smsDraft.sourceId!!).firstOrNull()
                } else {
                    smsDraft?.sourceIdentifier?.let { 
                        transactionUseCaseGroup.getSourceByIdentifierUseCase(it)
                    }
                }
                
                val defaultCategory =
                    transactionUseCaseGroup.getDefaultCategoryUseCase(_state.value.transactionType)
                
                val detectedCategory = smsDraft?.categoryId?.let { 
                    transactionUseCaseGroup.getCategoryUseCase(it)
                }
                
                _state.update {
                    it.copy(
                        source = detectedSource ?: defaultSource,
                        category = detectedCategory ?: defaultCategory
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
                persons = transactionWithRelations.persons.toSet(),
                photoBytes = null // We will load it below
            )
        }
        _typeFlow.value = transactionWithRelations.transaction.type

        transactionWithRelations.transaction.photoPath?.let { path ->
            viewModelScope.launch {
                val bytes = imageStorage.loadImage(path)
                _state.update { it.copy(photoBytes = bytes) }
            }
        }
    }

    /** Prefills the form from a past transaction as a starting point for a *new* entry
     *  (today's date, no [AddTransactionState.oldTransaction]) rather than editing it. */
    private fun prefillFromTemplate(template: TransactionWithRelations) {
        val today = JalaliCalendar()
        _state.value = AddTransactionState(
            date = "${today.day.toPersianDigits()} / ${today.monthString} / ${today.year.toPersianDigits()}",
            timeStamp = today.toTimestamp(),
            mostUsedCategories = _state.value.mostUsedCategories,
            mostUsedSources = _state.value.mostUsedSources,
            mostUsedTags = _state.value.mostUsedTags,
            mostUsedPersons = _state.value.mostUsedPersons,
            amount = template.transaction.amount.toString(),
            amountTransfer = template.transaction.amountTransfer.toString(),
            description = template.transaction.description ?: "",
            transactionType = template.transaction.type,
            category = template.category,
            source = template.source,
            sourceEnd = template.sourceEnd,
            tags = template.tags.toSet(),
            persons = template.persons.toSet()
        )
        analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionDuplicateClicked)
        _typeFlow.value = template.transaction.type
    }

    private fun submitTransaction() {
        val current = _state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val photoPath = current.photoBytes?.let { bytes ->
                // Simplified: always save if present. 
                // In a real app, you'd check if it changed.
                val path = imageStorage.saveImage(bytes)
                // Delete old photo if it exists and we are saving a new one
                current.oldTransaction?.photoPath?.let { oldPath ->
                    imageStorage.deleteImage(oldPath)
                }
                path
            } ?: run {
                // If photoBytes is null, and we had an old photo, it means it was removed
                current.oldTransaction?.photoPath?.let { oldPath ->
                    imageStorage.deleteImage(oldPath)
                }
                null
            }

            val amountValue = current.amount.toLongOrNull() ?: 0L
            // For a transfer, the destination amount mirrors the source amount unless the
            // user explicitly entered a different one; otherwise it would stay 0 and the
            // per-day net / detail math would treat the transfer as having no value.
            val amountTransferValue = if (current.transactionType == TransactionType.TRANSFER) {
                current.amountTransfer?.toLongOrNull()?.takeIf { it > 0L } ?: amountValue
            } else {
                current.amountTransfer?.toLongOrNull() ?: 0L
            }

            val currencyValue = preferenceUseCases.getStringPreference(com.kazemieh.preferences.FinTrackPreferences.PREF_CURRENCY, "IRT")

            val transaction = Transaction(
                id = current.oldTransaction?.id ?: 0,
                currencyCode = current.oldTransaction?.currencyCode ?: currencyValue,
                amount = amountValue,
                amountTransfer = amountTransferValue,
                categoryId = current.category?.id ?: 0,
                sourceId = current.source?.id ?: 0,
                sourceEndId = current.sourceEnd?.id,
                relatedDebtId = current.oldTransaction?.relatedDebtId,
                description = current.description,
                photoPath = photoPath,
                timeStamp = current.timeStamp,
                type = current.transactionType,
                date = current.date ?: ""
            )

            val tagIds = current.tags?.mapNotNull { it.id } ?: emptyList()
            val personIds = current.persons?.mapNotNull { it.id } ?: emptyList()

            val id = if (current.oldTransaction == null) {
                val newId = transactionUseCaseGroup.addTransactionUseCase(transaction, tagIds, personIds)
                if (newId > 0) analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionCreated(transaction.type.name))
                newId
            } else {
                val updatedId = transactionUseCaseGroup.updateTransactionUseCase(
                    current.oldTransaction,
                    transaction,
                    tagIds,
                    personIds
                )
                if (updatedId > 0) analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionUpdated)
                updatedId
            }
            _state.update { it.copy(isLoading = false) }
            if (id > 0) {
                current.smsDraft?.let { draft ->
                    smsDraftRepository.markSmsDraftAsUsed(draft.id)
                }
                if (current.oldTransaction == null && current.transactionType == TransactionType.EXPENSE) {
                    applyRoundUp(amountValue.toLong())
                }
                _effect.send(AddTransactionEffect.AddedTransaction)
            } else {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.transaction_failed))
            }
        }
    }

    /** Rounds a new expense up to the configured unit and deposits the difference
     *  into the user's chosen savings goal, if round-up is enabled. */
    private suspend fun applyRoundUp(expenseAmount: Long) {
        val enabled = preferenceUseCases.getBooleanPreference(FinTrackPreferences.PREF_ROUNDUP_ENABLED, false)
        if (!enabled) return
        val goalId = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_ROUNDUP_GOAL_ID, "").toLongOrNull() ?: return
        val unit = preferenceUseCases.getStringPreference(FinTrackPreferences.PREF_ROUNDUP_UNIT, "5000").toLongOrNull() ?: 5000L
        if (unit <= 0 || expenseAmount <= 0) return

        val remainder = expenseAmount % unit
        if (remainder == 0L) return
        val roundUpAmount = unit - remainder

        val goal = goalRepository.getGoalById(goalId) ?: return
        goalRepository.updateGoal(goal.copy(savedAmount = goal.savedAmount + roundUpAmount))
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
        // Amount is stored as Int; reject values that don't fit so they aren't silently
        // truncated to 0 on save (toIntOrNull returns null for out-of-range values).
        val amountLong = current.amount.toLongOrNull()
        val isAmountError = amountLong == null || amountLong <= 0L
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
        if (type == TransactionType.TRANSFER && _typeFlow.value != TransactionType.TRANSFER) {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.SourceTransferInitiated)
        }
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
    val photoBytes: ByteArray? = null,
    val isAmountError: Boolean = false,
    val isCategoryError: Boolean = false,
    val isSourceError: Boolean = false,
    val isSourceEndError: Boolean = false,
    val isLoading: Boolean = false,
    val sheetStack: List<AddTransactionSheet> = emptyList(),
    val oldTransaction: Transaction? = null,
    val smsDraft: SmsDraft? = null,
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
    data class SetTime(val hour: Int, val minute: Int) : AddTransactionIntent
    data class SetDescription(val description: String) : AddTransactionIntent
    data class SetPhoto(val bytes: ByteArray?) : AddTransactionIntent
    data object Submit : AddTransactionIntent
    data class FetchDefaultData(
        val transactionWithRelations: TransactionWithRelations?,
        val smsDraft: SmsDraft? = null
    ) : AddTransactionIntent
    data class PrefillFromTemplate(val template: TransactionWithRelations) : AddTransactionIntent

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
    data object TimePicker : AddTransactionSheet
    data object DeleteConfirmation : AddTransactionSheet
    data object Calculator : AddTransactionSheet
}

sealed interface AddTransactionEffect {
    data object AddedTransaction : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}
