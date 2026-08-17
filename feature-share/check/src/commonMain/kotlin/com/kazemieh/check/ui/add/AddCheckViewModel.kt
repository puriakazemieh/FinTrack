package com.kazemieh.check.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.domain.usecase.CheckUseCaseGroup
import com.kazemieh.domain.usecase.GetCategoryUseCase
import com.kazemieh.domain.usecase.GetPersonByIdUseCase
import com.kazemieh.domain.usecase.GetSourceByIdUseCase
import com.kazemieh.domain.usecase.GetTagByIdUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedPersonsUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedSourcesUseCase
import com.kazemieh.domain.usecase.ObserveMostUsedTagsUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_notif_check_paid
import fintrack.core.designsystem.generated.resources.msg_notif_check_received
import fintrack.core.designsystem.generated.resources.notif_cheque_label
import kotlinx.coroutines.Job
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
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

class AddCheckViewModel(
    private val checkUseCases: CheckUseCaseGroup,
    private val getPersonByIdUseCase: GetPersonByIdUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val getSourceByIdUseCase: GetSourceByIdUseCase,
    private val getTagByIdUseCase: GetTagByIdUseCase,
    private val observeMostUsedCategoriesUseCase: ObserveMostUsedCategoriesUseCase,
    private val observeMostUsedSourcesUseCase: ObserveMostUsedSourcesUseCase,
    private val observeMostUsedTagsUseCase: ObserveMostUsedTagsUseCase,
    private val observeMostUsedPersonsUseCase: ObserveMostUsedPersonsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddCheckState())
    val state: StateFlow<AddCheckState> = _state.asStateFlow()

    private val _effect = Channel<AddCheckEffect>()
    val effect: Flow<AddCheckEffect> = _effect.receiveAsFlow()

    private var mostUsedCategoriesJob: Job? = null

    init {
        observeMostUsed()
    }

    private fun observeMostUsed() {
        updateMostUsedCategories(_state.value.isIncoming)
        observeMostUsedSourcesUseCase(limit = 3).onEach { sources ->
            _state.update { it.copy(mostUsedSources = sources) }
        }.launchIn(viewModelScope)
        observeMostUsedTagsUseCase(limit = 3).onEach { tags ->
            _state.update { it.copy(mostUsedTags = tags) }
        }.launchIn(viewModelScope)
        observeMostUsedPersonsUseCase(limit = 3).onEach { persons ->
            _state.update { it.copy(mostUsedPersons = persons) }
        }.launchIn(viewModelScope)
    }

    private fun updateMostUsedCategories(isIncoming: Boolean) {
        mostUsedCategoriesJob?.cancel()
        val type = if (isIncoming) TransactionType.INCOME else TransactionType.EXPENSE
        mostUsedCategoriesJob = observeMostUsedCategoriesUseCase(type, limit = 3)
            .onEach { categories -> _state.update { it.copy(mostUsedCategories = categories) } }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: AddCheckIntent) {
        when (intent) {
            is AddCheckIntent.SetAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddCheckIntent.SetDate -> _state.update { it.copy(date = intent.date) }
            is AddCheckIntent.SetDueDate -> _state.update { it.copy(dueDate = intent.dueDate) }
            is AddCheckIntent.SetReminderTime -> setReminderTime(intent.hour, intent.minute)
            is AddCheckIntent.SetReminderEnabled -> _state.update { it.copy(reminderEnabled = intent.enabled) }
            is AddCheckIntent.SetPerson -> _state.update { it.copy(person = intent.person) }
            is AddCheckIntent.SetCategory -> _state.update { it.copy(category = intent.category) }
            is AddCheckIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddCheckIntent.SetTags -> _state.update { it.copy(tags = intent.tags) }
            is AddCheckIntent.SetStatus -> _state.update { it.copy(status = intent.status) }
            is AddCheckIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddCheckIntent.SetIsIncoming -> {
                _state.update { it.copy(isIncoming = intent.isIncoming) }
                updateMostUsedCategories(intent.isIncoming)
            }
            is AddCheckIntent.ToggleSheet -> _state.update { it.copy(topSheet = intent.sheet) }
            is AddCheckIntent.LoadCheck -> loadCheck(intent.checkId)
            AddCheckIntent.Reset -> _state.value = AddCheckState(
                mostUsedCategories = _state.value.mostUsedCategories,
                mostUsedSources = _state.value.mostUsedSources,
                mostUsedTags = _state.value.mostUsedTags,
                mostUsedPersons = _state.value.mostUsedPersons
            )
            AddCheckIntent.Submit -> submit()
        }
    }

    private fun setReminderTime(hour: Int, minute: Int) {
        val timeZone = TimeZone.currentSystemDefault()
        val adjustedDueDate = _state.value.dueDate
            .let { kotlinx.datetime.Instant.fromEpochMilliseconds(it).toPersianDateTime(timeZone) }
            .copy(hour = hour, minute = minute, second = 0)
            .toEpochMilliseconds(timeZone)
        _state.update { it.copy(dueDate = adjustedDueDate, reminderEnabled = true) }
    }

    private fun loadCheck(checkId: Long) {
        viewModelScope.launch {
            val check = checkUseCases.getCheckByIdUseCase(checkId) ?: return@launch
            val person = getPersonByIdUseCase(check.personId)
            val category = check.categoryId?.let { getCategoryUseCase(it) }
            val source = check.sourceId?.let { getSourceByIdUseCase(it) }
            val tags = check.tagIds.orEmpty().mapNotNull { getTagByIdUseCase(it) }.toSet()
            _state.update {
                it.copy(
                    checkId = check.id,
                    amount = check.amount.toString(),
                    date = check.date,
                    dueDate = check.dueDate,
                    reminderEnabled = check.reminderEnabled,
                    status = check.status,
                    person = person,
                    category = category,
                    source = source,
                    tags = tags,
                    legacyPhotoPath = check.photoPath,
                    description = check.description.orEmpty(),
                    isIncoming = check.isIncoming
                )
            }
            updateMostUsedCategories(check.isIncoming)
        }
    }

    private fun submit() {
        val current = _state.value
        val amount = current.amount.toLongOrNull() ?: return
        val person = current.person ?: return

        viewModelScope.launch {
            val check = Check(
                id = current.checkId ?: 0L,
                amount = amount,
                date = current.date,
                dueDate = current.dueDate,
                status = current.status,
                personId = person.id ?: return@launch,
                categoryId = current.category?.id,
                sourceId = current.source?.id,
                tagIds = current.tags.mapNotNull { it.id },
                reminderEnabled = current.reminderEnabled,
                photoPath = current.legacyPhotoPath,
                description = current.description,
                isIncoming = current.isIncoming
            )
            val reminderTitle = getString(Res.string.notif_cheque_label)
            val reminderMessage = if (current.isIncoming) {
                getString(Res.string.msg_notif_check_received, person.name)
            } else {
                getString(Res.string.msg_notif_check_paid, person.name)
            }
            if (current.checkId == null) {
                checkUseCases.addCheckUseCase(check, reminderTitle, reminderMessage)
            } else {
                checkUseCases.updateCheckUseCase(check, reminderTitle, reminderMessage)
            }
            _effect.send(AddCheckEffect.Saved)
        }
    }
}

data class AddCheckState(
    val checkId: Long? = null,
    val amount: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val dueDate: Long = Clock.System.now().toEpochMilliseconds(),
    val reminderEnabled: Boolean = true,
    val person: Person? = null,
    val category: Category? = null,
    val source: Source? = null,
    val tags: Set<Tag> = emptySet(),
    val status: CheckStatus = CheckStatus.PENDING,
    val legacyPhotoPath: String? = null,
    val description: String = "",
    val isIncoming: Boolean = false,
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val mostUsedTags: List<Tag> = emptyList(),
    val mostUsedPersons: List<Person> = emptyList(),
    val topSheet: AddCheckSheet? = null
)

sealed interface AddCheckSheet {
    data object Calculator : AddCheckSheet
    data object CategoryPicker : AddCheckSheet
    data object SourcePicker : AddCheckSheet
    data object PersonPicker : AddCheckSheet
    data object TagPicker : AddCheckSheet
    data object IssueDatePicker : AddCheckSheet
    data object DueDatePicker : AddCheckSheet
    data object ReminderTimePicker : AddCheckSheet
}

sealed interface AddCheckIntent {
    data class SetAmount(val amount: String) : AddCheckIntent
    data class SetDate(val date: Long) : AddCheckIntent
    data class SetDueDate(val dueDate: Long) : AddCheckIntent
    data class SetReminderTime(val hour: Int, val minute: Int) : AddCheckIntent
    data class SetReminderEnabled(val enabled: Boolean) : AddCheckIntent
    data class SetPerson(val person: Person?) : AddCheckIntent
    data class SetCategory(val category: Category?) : AddCheckIntent
    data class SetSource(val source: Source?) : AddCheckIntent
    data class SetTags(val tags: Set<Tag>) : AddCheckIntent
    data class SetStatus(val status: CheckStatus) : AddCheckIntent
    data class SetDescription(val description: String) : AddCheckIntent
    data class SetIsIncoming(val isIncoming: Boolean) : AddCheckIntent
    data class ToggleSheet(val sheet: AddCheckSheet?) : AddCheckIntent
    data class LoadCheck(val checkId: Long) : AddCheckIntent
    data object Reset : AddCheckIntent
    data object Submit : AddCheckIntent
}

sealed interface AddCheckEffect {
    data object Saved : AddCheckEffect
}
