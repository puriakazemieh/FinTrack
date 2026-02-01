package com.kazemieh.person.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.domain.usecase.GetAllPerson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonViewModel(
    private val getAllPerson: GetAllPerson
) : ViewModel() {

    private val _state = MutableStateFlow(PersonState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PersonEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: PersonIntent) {
        when (intent) {
            PersonIntent.GetAllPerson -> loadAllPersons()

            is PersonIntent.SetSelectedPerson -> {
                val current = _state.value.initialSelectionIds
                val person = intent.person
                val selectedPersons =
                    if (current.contains(person))
                        current - person
                    else
                        current + person

                _state.update {
                    it.copy(initialSelectionIds = selectedPersons, showAddPerson = false)
                }
            }

            is PersonIntent.SetAllSelectedPersons -> _state.update {
                it.copy(
                    initialSelectionIds = intent.persons?.toSet() ?: emptySet()
                )
            }


            is PersonIntent.ShowAddPerson -> _state.update {
                it.copy(showAddPerson = !state.value.showAddPerson, selectedPerson = null)
            }

            is PersonIntent.OnDeleteClick -> _state.update {
                it.copy(
                    isDeleteShow = !_state.value.isDeleteShow,
                    selectedPerson = intent.person
                )
            }

            is PersonIntent.OnEditClick -> _state.update {
                it.copy(
                    showAddPerson = true,
                    selectedPerson = intent.person
                )
            }

            PersonIntent.OnDismiss -> {
                viewModelScope.launch {
                    _effect.send(PersonEffect.OnDismiss)
                    _state.update { PersonState() }
                }
            }

            is PersonIntent.SelectedPerson -> {
                viewModelScope.launch {
                    if (intent.person != null) {
                        _effect.send(PersonEffect.OnPersonSelected(intent.person))
                        _state.update { PersonState() }
                    }
                }
            }

        }
    }

    private fun loadAllPersons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllPerson().collect { persons ->
                _state.update {
                    it.copy(
                        persons = persons,
                        items = persons.map { it.toItemUi() }.toSet(),
                        isLoading = false
                    )
                }
            }
        }
    }

}

data class PersonState(
    val persons: List<Person> = emptyList(),
    val showAddPerson: Boolean = false,
    val isLoading: Boolean = false,
    val initialSelectionIds: Set<Person> = emptySet(),
    val items: Set<ItemUi> = emptySet(),
    val isDeleteShow: Boolean = false,
    val selectedPerson: Person? = null,
)

sealed interface PersonIntent {
    data class SetSelectedPerson(val person: Person) : PersonIntent
    data class SetAllSelectedPersons(val persons: Set<Person>? = null) : PersonIntent
    data object GetAllPerson : PersonIntent
    data object ShowAddPerson : PersonIntent
    data object OnDismiss : PersonIntent
    data class SelectedPerson(val person: Person? = null) : PersonIntent
    data class OnEditClick(val person: Person? = null) : PersonIntent
    data class OnDeleteClick(val person: Person? = null) : PersonIntent
}

sealed interface PersonEffect {
    data object OnDismiss : PersonEffect
    data class OnPersonSelected(val person: Person) : PersonEffect
}