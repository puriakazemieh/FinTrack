package com.kazemieh.person.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.AddPerson
import com.kazemieh.model.Person
import com.kazemieh.person.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPersonViewModel(
    private val addPersonUseCase: AddPerson
) : ViewModel() {

    private val _state = MutableStateFlow(AddPersonState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddPersonEffect>()
    val effect = _effect.receiveAsFlow()


    fun onIntent(intent: AddPersonIntent) {
        when (intent) {
            AddPersonIntent.AddPerson -> addPerson()
            AddPersonIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddPersonState() }
                    _effect.send(AddPersonEffect.OnDismiss)
                }
            }

            is AddPersonIntent.SetPersonName -> _state.update { it.copy(personName = intent.personName) }
            is AddPersonIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
        }
    }

    private fun addPerson() = with(_state.value) {
        viewModelScope.launch {
            if (personName?.isNotBlank() == true) {
                val person = Person(
                    name = personName,
                    description = description
                )
                val personId = addPersonUseCase(person)
                if (personId >= 0) {
                    _effect.send(
                        AddPersonEffect.AddedPerson(
                            personId.toInt(),
                            _state.value.personName ?: "انتخاب کنید"
                        )
                    )
                    _state.update { AddPersonState() }
                }

            } else {
                _effect.send(AddPersonEffect.ShowMessage(R.string.check_name_person_source))
            }
        }
    }
}


data class AddPersonState(
    val personName: String? = null,
    val description: String? = null,
)

sealed interface AddPersonIntent {
    data object AddPerson : AddPersonIntent
    data class SetPersonName(val personName: String? = null) : AddPersonIntent
    data class SetDescription(val description: String? = null) : AddPersonIntent
    data object OnDismiss : AddPersonIntent
}

sealed interface AddPersonEffect {
    data class ShowMessage(val message: Int) : AddPersonEffect
    data class AddedPerson(val id: Int, val name: String) : AddPersonEffect
    data object OnDismiss : AddPersonEffect
}