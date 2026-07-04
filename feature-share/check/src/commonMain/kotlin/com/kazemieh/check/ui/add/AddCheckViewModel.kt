package com.kazemieh.check.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.Person
import com.kazemieh.common.ImageStorage
import com.kazemieh.domain.usecase.CheckUseCaseGroup
import com.kazemieh.domain.usecase.GetPersonByIdUseCase
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

class AddCheckViewModel(
    private val checkUseCases: CheckUseCaseGroup,
    private val getPersonByIdUseCase: GetPersonByIdUseCase,
    private val imageStorage: ImageStorage
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
            is AddCheckIntent.SetPhoto -> _state.update { it.copy(photoBytes = intent.bytes) }
            is AddCheckIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddCheckIntent.SetIsIncoming -> _state.update { it.copy(isIncoming = intent.isIncoming) }
            is AddCheckIntent.LoadCheck -> loadCheck(intent.checkId)
            AddCheckIntent.Submit -> submit()
        }
    }

    private fun loadCheck(checkId: Long) {
        viewModelScope.launch {
            val check = checkUseCases.getCheckByIdUseCase(checkId) ?: return@launch
            val person = getPersonByIdUseCase(check.personId)
            val photoBytes = check.photoPath?.let { imageStorage.loadImage(it) }
            _state.update {
                it.copy(
                    checkId = check.id,
                    amount = check.amount.toString(),
                    date = check.date,
                    dueDate = check.dueDate,
                    status = check.status,
                    person = person,
                    photoPath = check.photoPath,
                    photoBytes = photoBytes,
                    description = check.description ?: "",
                    isIncoming = check.isIncoming
                )
            }
        }
    }

    private fun submit() {
        val currentState = _state.value
        val amountValue = currentState.amount.toLongOrNull()
        if (amountValue == null || currentState.person == null) return

        viewModelScope.launch {
            // Save a newly picked photo, otherwise keep the previously stored path (edit mode).
            val photoPath = currentState.photoBytes?.let { bytes ->
                imageStorage.saveImage(bytes)
            } ?: currentState.photoPath

            val check = Check(
                id = currentState.checkId ?: 0L,
                amount = amountValue,
                date = currentState.date,
                dueDate = currentState.dueDate,
                status = currentState.status,
                personId = currentState.person.id ?: 0L,
                photoPath = photoPath,
                description = currentState.description,
                isIncoming = currentState.isIncoming
            )
            if (currentState.checkId != null) {
                checkUseCases.updateCheckUseCase(check)
            } else {
                val reminderTitle = getString(Res.string.notif_cheque_label)
                val reminderMessage = if (currentState.isIncoming) {
                    getString(Res.string.msg_notif_check_received, currentState.person.name)
                } else {
                    getString(Res.string.msg_notif_check_paid, currentState.person.name)
                }
                checkUseCases.addCheckUseCase(check, reminderTitle, reminderMessage)
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
    val person: Person? = null,
    val status: CheckStatus = CheckStatus.PENDING,
    val photoBytes: ByteArray? = null,
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
    data class SetPhoto(val bytes: ByteArray?) : AddCheckIntent
    data class SetDescription(val description: String) : AddCheckIntent
    data class SetIsIncoming(val isIncoming: Boolean) : AddCheckIntent
    data class LoadCheck(val checkId: Long) : AddCheckIntent
    data object Submit : AddCheckIntent
}

sealed interface AddCheckEffect {
    data object Saved : AddCheckEffect
}
