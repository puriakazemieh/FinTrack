package com.kazemieh.person.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllPerson
import com.kazemieh.common.model.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonViewModel(
    private val getAllPerson: GetAllPerson
) : ViewModel() {

    private val _state = MutableStateFlow(PersonState())
    val state = _state.asStateFlow()

    fun onIntent(intent: PersonIntent) {
        when (intent) {
            PersonIntent.GetAllPerson -> loadAllPersons()

            is PersonIntent.SetSelectedPerson -> {
                val current = _state.value.selectedPersons ?: emptySet()
                val selectedPersons =
                    if (current.contains(intent.person))
                        current - intent.person
                    else
                        current + intent.person

                _state.update {
                    it.copy(selectedPersons = selectedPersons, showAddPerson = false)
                }
            }

            is PersonIntent.SetAllSelectedPersons -> _state.update {
                it.copy(selectedPersons = intent.persons)
            }

            is PersonIntent.ShowAddPerson -> _state.update {
                it.copy(showAddPerson = intent.showAddPerson)
            }

        }
    }

    private fun loadAllPersons() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllPerson().collect { persons ->
//                val personSet = persons.map { person -> (person.id?.toInt() ?: -1) to person.name }.toSet()
                _state.update { it.copy(persons = persons, isLoading = false) }
            }
        }
    }


}

data class PersonState(
    val persons: List<Person> = emptyList(),
    val selectedPersons: Set<Pair<Int, String>>? = null,
    val showAddPerson: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface PersonIntent {
    data class SetSelectedPerson(val person: Pair<Int, String>) : PersonIntent
    data class SetAllSelectedPersons(val persons: Set<Pair<Int, String>>? = null) : PersonIntent
    data object GetAllPerson : PersonIntent
    data class ShowAddPerson(val showAddPerson: Boolean = false) : PersonIntent
}