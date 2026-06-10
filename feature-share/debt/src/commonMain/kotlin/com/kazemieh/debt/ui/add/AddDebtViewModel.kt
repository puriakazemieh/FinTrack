package com.kazemieh.debt.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Debt
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock


class AddDebtViewModel(
    private val debtUseCases: DebtUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddDebtState())
    val state: StateFlow<AddDebtState> = _state.asStateFlow()

    private val _effect = Channel<AddDebtEffect>()
    val effect: Flow<AddDebtEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: AddDebtIntent) {
        when (intent) {
            is AddDebtIntent.SetPerson -> _state.update { it.copy(person = intent.person) }
            is AddDebtIntent.SetAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddDebtIntent.SetType -> _state.update { it.copy(type = intent.type) }
            is AddDebtIntent.SetDate -> _state.update { it.copy(date = intent.date) }
            is AddDebtIntent.SetDueDate -> _state.update { it.copy(dueDate = intent.dueDate) }
            is AddDebtIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddDebtIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            AddDebtIntent.Submit -> submit()
            AddDebtIntent.Dismiss -> viewModelScope.launch { _effect.send(AddDebtEffect.OnDismiss) }
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
                id = 0,
                personId = currentState.person.id ?: 0L,
                amount = amountValue,
                date = currentState.date,
                dueDate = currentState.dueDate,
                sourceId = currentState.source?.id,
                description = currentState.description,
                type = currentState.type,
                isSettled = false
            )
            debtUseCases.addDebtUseCase(debt)
            _effect.send(AddDebtEffect.Saved)
        }
    }
}

data class AddDebtState(
    val person: Person? = null,
    val amount: String = "",
    val type: DebtType = DebtType.OWED_BY_ME,
    val date: Long = Clock.System.now().toEpochMilliseconds(),
    val dueDate: Long? = null,
    val source: Source? = null,
    val description: String = "",
    val isLoading: Boolean = false
)

sealed interface AddDebtIntent {
    data class SetPerson(val person: Person?) : AddDebtIntent
    data class SetAmount(val amount: String) : AddDebtIntent
    data class SetType(val type: DebtType) : AddDebtIntent
    data class SetDate(val date: Long) : AddDebtIntent
    data class SetDueDate(val dueDate: Long?) : AddDebtIntent
    data class SetSource(val source: Source?) : AddDebtIntent
    data class SetDescription(val description: String) : AddDebtIntent
    data object Submit : AddDebtIntent
    data object Dismiss : AddDebtIntent
}

sealed interface AddDebtEffect {
    data object Saved : AddDebtEffect
    data object OnDismiss : AddDebtEffect
    data class ShowMessage(val message: UiText) : AddDebtEffect
}
