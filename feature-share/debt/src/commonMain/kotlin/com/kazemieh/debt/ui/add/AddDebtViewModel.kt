package com.kazemieh.debt.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock


class AddDebtViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val debtUseCases: DebtUseCaseGroup,
    private val transactionUseCases: TransactionUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddDebtState())
    val state: StateFlow<AddDebtState> = _state.asStateFlow()

    private val _effect = Channel<AddDebtEffect>()
    val effect: Flow<AddDebtEffect> = _effect.receiveAsFlow()

    init {
        observeMostUsed()
    }

    private var mostUsedCategoriesJob: kotlinx.coroutines.Job? = null

    private fun observeMostUsed() {
        updateMostUsedCategories(_state.value.type)

        transactionUseCases.observeMostUsedSourcesUseCase()
            .onEach { list -> _state.update { it.copy(mostUsedSources = list) } }
            .launchIn(viewModelScope)

        transactionUseCases.observeMostUsedTagsUseCase()
            .onEach { list -> _state.update { it.copy(mostUsedTags = list) } }
            .launchIn(viewModelScope)

        transactionUseCases.observeMostUsedPersonsUseCase()
            .onEach { list -> _state.update { it.copy(mostUsedPersons = list) } }
            .launchIn(viewModelScope)
    }

    private fun updateMostUsedCategories(type: DebtType) {
        mostUsedCategoriesJob?.cancel()
        val transactionType = if (type == DebtType.OWED_TO_ME) com.kazemieh.common.model.TransactionType.INCOME else com.kazemieh.common.model.TransactionType.EXPENSE
        mostUsedCategoriesJob = transactionUseCases.observeMostUsedCategoriesUseCase(transactionType)
            .onEach { list -> _state.update { it.copy(mostUsedCategories = list) } }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: AddDebtIntent) {
        when (intent) {
            is AddDebtIntent.LoadDebt -> loadDebt(intent.id)
            is AddDebtIntent.SetPersonById -> loadPerson(intent.id)
            is AddDebtIntent.SetPerson -> _state.update { it.copy(person = intent.person) }
            is AddDebtIntent.SetAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddDebtIntent.SetType -> {
                _state.update { it.copy(type = intent.type) }
                updateMostUsedCategories(intent.type)
            }
            is AddDebtIntent.SetCategory -> _state.update { it.copy(category = intent.category) }
            is AddDebtIntent.SetTags -> _state.update { it.copy(tags = intent.tags) }
            is AddDebtIntent.SetDate -> _state.update { it.copy(date = intent.date) }
            is AddDebtIntent.SetDueDate -> _state.update { it.copy(dueDate = intent.dueDate) }
            is AddDebtIntent.SetReminderEnabled -> _state.update { it.copy(reminderEnabled = intent.enabled, dueDate = if (intent.enabled && it.dueDate == null) it.date else it.dueDate) }
            is AddDebtIntent.SetReminderTime -> setReminderTime(intent.hour, intent.minute)
            is AddDebtIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddDebtIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            AddDebtIntent.Submit -> submit()
            AddDebtIntent.Reset -> _state.value = AddDebtState()
            AddDebtIntent.Dismiss -> viewModelScope.launch { _effect.send(AddDebtEffect.OnDismiss) }
        }
    }

    private fun loadPerson(id: Long) {
        viewModelScope.launch {
            val person = debtUseCases.getPersonByIdUseCase(id)
            _state.update { it.copy(person = person) }
        }
    }

    private fun setReminderTime(hour: Int, minute: Int) {
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val currentTs = _state.value.dueDate ?: _state.value.date
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(currentTs)
        val pdt = instant.toPersianDateTime(tz)
        val newTs = pdt.copy(hour = hour, minute = minute, second = 0).toEpochMilliseconds(tz)
        _state.update { it.copy(dueDate = newTs, reminderEnabled = true) }
    }

    private fun loadDebt(id: Long) {
        viewModelScope.launch {
            val withRelations = debtUseCases.getDebtWithRelationsUseCase(id) ?: return@launch
            val debt = withRelations.debt
            
            _state.update {
                it.copy(
                    debtId = id,
                    person = withRelations.person,
                    category = withRelations.category,
                    tags = withRelations.tags.toSet(),
                    amount = debt.amount.toString(),
                    type = debt.type,
                    date = debt.date,
                    dueDate = debt.dueDate,
                    source = withRelations.source,
                    description = debt.description ?: ""
                )
            }
            updateMostUsedCategories(debt.type)
        }
    }

    private fun submit() {
        val currentState = _state.value
        val amountValue = currentState.amount.toLongOrNull()
        if (currentState.person == null || amountValue == null) {
            return
        }

        viewModelScope.launch {
            val debt = Debt(
                id = currentState.debtId ?: 0,
                personId = currentState.person.id ?: 0L,
                amount = amountValue,
                categoryId = currentState.category?.id,
                date = currentState.date,
                dueDate = currentState.dueDate,
                sourceId = currentState.source?.id,
                description = currentState.description,
                type = currentState.type,
                isSettled = false,
                reminderEnabled = currentState.reminderEnabled,
                personName = currentState.person.name
            )
            
            val tagIds = currentState.tags.mapNotNull { it.id }
            
            if (currentState.debtId == null) {
                debtUseCases.addDebtUseCase(debt, tagIds)
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("debt_created"))
            } else {
                debtUseCases.updateDebtUseCase(debt, tagIds)
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("debt_updated"))
            }
            _effect.send(AddDebtEffect.Saved)
        }
    }
}

data class AddDebtState(
    val debtId: Long? = null,
    val person: Person? = null,
    val category: com.kazemieh.common.model.Category? = null,
    val tags: Set<com.kazemieh.common.model.Tag> = emptySet(),
    val amount: String = "",
    val type: DebtType = DebtType.OWED_BY_ME,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val dueDate: Long? = null,
    val reminderEnabled: Boolean = false,
    val source: Source? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    
    // Most used
    val mostUsedCategories: List<com.kazemieh.common.model.Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val mostUsedTags: List<com.kazemieh.common.model.Tag> = emptyList(),
    val mostUsedPersons: List<Person> = emptyList()
)

sealed interface AddDebtIntent {
    data class LoadDebt(val id: Long) : AddDebtIntent
    data class SetPersonById(val id: Long) : AddDebtIntent
    data class SetPerson(val person: Person?) : AddDebtIntent
    data class SetAmount(val amount: String) : AddDebtIntent
    data class SetType(val type: DebtType) : AddDebtIntent
    data class SetCategory(val category: com.kazemieh.common.model.Category?) : AddDebtIntent
    data class SetTags(val tags: Set<com.kazemieh.common.model.Tag>) : AddDebtIntent
    data class SetDate(val date: Long) : AddDebtIntent
    data class SetDueDate(val dueDate: Long?) : AddDebtIntent
    data class SetReminderEnabled(val enabled: Boolean) : AddDebtIntent
    data class SetReminderTime(val hour: Int, val minute: Int) : AddDebtIntent
    data class SetSource(val source: Source?) : AddDebtIntent
    data class SetDescription(val description: String) : AddDebtIntent
    data object Submit : AddDebtIntent
    data object Reset : AddDebtIntent
    data object Dismiss : AddDebtIntent
}

sealed interface AddDebtEffect {
    data object Saved : AddDebtEffect
    data object OnDismiss : AddDebtEffect
    data class ShowMessage(val message: UiText) : AddDebtEffect
}
