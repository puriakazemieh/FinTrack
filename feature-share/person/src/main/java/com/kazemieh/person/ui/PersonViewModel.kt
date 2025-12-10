package com.kazemieh.person.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.domain.usecase.GetAllPerson
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
                val current = _state.value.initialSelectionIds
                val person = intent.person.first
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
                    initialSelectionIds = intent.persons?.map { it.first }?.toSet() ?: emptySet()
                )
            }


            is PersonIntent.ShowAddPerson -> _state.update {
                it.copy(showAddPerson = !state.value.showAddPerson)
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
                        items = persons.map { ItemUi(it.id?.toInt() ?: 0, it.name) },
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
    val initialSelectionIds: Set<Int> = emptySet(),
    val items: List<ItemUi> = emptyList(),
)

sealed interface PersonIntent {
    data class SetSelectedPerson(val person: Pair<Int, String>) : PersonIntent
    data class SetAllSelectedPersons(val persons: Set<Pair<Int, String>>? = null) : PersonIntent
    data object GetAllPerson : PersonIntent
    data object ShowAddPerson : PersonIntent
}