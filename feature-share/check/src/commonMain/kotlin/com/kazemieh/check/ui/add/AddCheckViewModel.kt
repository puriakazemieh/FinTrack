package com.kazemieh.check.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.Person
import com.kazemieh.domain.usecase.CheckUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class AddCheckViewModel(
    private val checkUseCases: CheckUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddCheckState())
    val state: StateFlow<AddCheckState> = _state.asStateFlow()

    private val _effect = Channel<AddCheckEffect>()
    val effect: Flow<AddCheckEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: AddCheckIntent) {
        when (intent) {
            is AddCheckIntent.SetAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddCheckIntent.SetDate -> _state.update { it.copy(date = intent.date) }
            is AddCheckIntent.SetDueDate -> _state.update { it.copy(dueDate = intent.dueDate) }
            is AddCheckIntent.SetPerson -> _state.update { it.copy(person = intent.person) }
            is AddCheckIntent.SetStatus -> _state.update { it.copy(status = intent.status) }
            is AddCheckIntent.SetPhoto -> _state.update { it.copy(photoPath = intent.photoPath) }
            is AddCheckIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddCheckIntent.SetIsIncoming -> _state.update { it.copy(isIncoming = intent.isIncoming) }
            AddCheckIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val currentState = _state.value
        val amountValue = currentState.amount.toLongOrNull()
        if (amountValue == null || currentState.person == null) return

        viewModelScope.launch {
            val check = Check(
                amount = amountValue,
                date = currentState.date,
                dueDate = currentState.dueDate,
                status = currentState.status,
                personId = currentState.person.id ?: 0L,
                photoPath = currentState.photoPath,
                description = currentState.description,
                isIncoming = currentState.isIncoming
            )
            checkUseCases.addCheckUseCase(check)
            _effect.send(AddCheckEffect.Saved)
        }
    }
}

data class AddCheckState(
    val amount: String = "",
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val dueDate: Long = Clock.System.now().toEpochMilliseconds(),
    val person: Person? = null,
    val status: CheckStatus = CheckStatus.PENDING,
    val photoPath: String? = null,
    val description: String = "",
    val isIncoming: Boolean = false
)

sealed interface AddCheckIntent {
    data class SetAmount(val amount: String) : AddCheckIntent
    data class SetDate(val date: Long) : AddCheckIntent
    data class SetDueDate(val dueDate: Long) : AddCheckIntent
    data class SetPerson(val person: Person?) : AddCheckIntent
    data class SetStatus(val status: CheckStatus) : AddCheckIntent
    data class SetPhoto(val photoPath: String?) : AddCheckIntent
    data class SetDescription(val description: String) : AddCheckIntent
    data class SetIsIncoming(val isIncoming: Boolean) : AddCheckIntent
    data object Submit : AddCheckIntent
}

sealed interface AddCheckEffect {
    data object Saved : AddCheckEffect
}
