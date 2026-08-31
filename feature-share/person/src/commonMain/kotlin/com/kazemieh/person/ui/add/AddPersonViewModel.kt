package com.kazemieh.person.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.AddPersonUseCase
import com.kazemieh.domain.usecase.UpdatePersonUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.check_name_person_source
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPersonViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val addPersonUseCase: AddPersonUseCase,
    private val updatePersonUseCase: UpdatePersonUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddPersonState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddPersonEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddPersonIntent) {
        when (intent) {
            AddPersonIntent.StartAdd -> startAdd()
            is AddPersonIntent.StartEdit -> startEdit(intent.person)

            is AddPersonIntent.UpdateName -> updateDraft { it.copy(name = intent.value) }
            is AddPersonIntent.UpdateDescription -> updateDraft { it.copy(description = intent.value) }

            AddPersonIntent.Save -> save()
            AddPersonIntent.OnDismiss -> dismiss()
        }
    }

    private fun updateDraft(transform: (PersonDraft) -> PersonDraft) {
        _state.update { it.copy(draft = transform(it.draft)) }
    }

    private fun startAdd() {
        _state.update {
            AddPersonState(
                mode = AddPersonMode.Add,
                draft = PersonDraft()
            )
        }
    }

    private fun startEdit(person: Person) {
        _state.update {
            AddPersonState(
                mode = AddPersonMode.Edit(person.id ?: 0L),
                draft = person.toDraft()
            )
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _state.update { AddPersonState() }
            _effect.send(AddPersonEffect.OnDismiss)
        }
    }

    private fun save() = with(_state.value) {
        viewModelScope.launch {
            val name = draft.name.trim()
            if (name.isBlank()) {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.check_name_person_source))
                return@launch
            }

            val person = draft.toPerson(id = mode.personIdOrNull)

            val personId = when (mode) {
                is AddPersonMode.Add -> {
                    val id = addPersonUseCase(person)
                    if (id > 0) analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("person_created"))
                    id
                }
                is AddPersonMode.Edit -> {
                    val id = updatePersonUseCase(person).toLong()
                    if (id > 0) analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("person_updated"))
                    id
                }
            }

            if (personId >= 0) {
                val saved = person.copy(id = personId)
                _effect.send(AddPersonEffect.SavedPerson(saved))
                _state.update { AddPersonState() }
            }
        }
    }
}

/** --- State / Mode / Draft --- **/

data class AddPersonState(
    val mode: AddPersonMode = AddPersonMode.Add,
    val draft: PersonDraft = PersonDraft()
)

sealed interface AddPersonMode {
    data object Add : AddPersonMode
    data class Edit(val personId: Long) : AddPersonMode
}

private val AddPersonMode.personIdOrNull: Long?
    get() = (this as? AddPersonMode.Edit)?.personId

data class PersonDraft(
    val name: String = "",
    val description: String? = null
)

private fun Person.toDraft(): PersonDraft = PersonDraft(
    name = this.name.orEmpty(),
    description = this.description
)

private fun PersonDraft.toPerson(id: Long?): Person = Person(
    id = id,
    name = name,
    description = description
)

/** --- Intent / Effect --- **/

sealed interface AddPersonIntent {
    data object StartAdd : AddPersonIntent
    data class StartEdit(val person: Person) : AddPersonIntent

    data class UpdateName(val value: String) : AddPersonIntent
    data class UpdateDescription(val value: String?) : AddPersonIntent

    data object Save : AddPersonIntent
    data object OnDismiss : AddPersonIntent
}

sealed interface AddPersonEffect {
    data class SavedPerson(val person: Person) : AddPersonEffect
    data object OnDismiss : AddPersonEffect
}
