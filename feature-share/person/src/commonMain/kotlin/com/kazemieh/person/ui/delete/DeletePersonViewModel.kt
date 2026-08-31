package com.kazemieh.person.ui.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import com.kazemieh.domain.usecase.DeletePersonUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeletePersonViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val deletePersonUseCase: DeletePersonUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DeletePersonState())
    val state: StateFlow<DeletePersonState> = _state.asStateFlow()

    private val _effect = Channel<DeletePersonEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeletePersonIntent) {
        when (intent) {
            is DeletePersonIntent.SetData -> _state.update { it.copy(person = intent.person) }

            DeletePersonIntent.Submit -> deletePerson()
            DeletePersonIntent.DeleteAllTransaction -> _state.update {
                it.copy(
                    isDeleteAllData = true,
                    movePerson = null,
                    isPersonError = false
                )
            }

            DeletePersonIntent.MoveAllTransaction -> _state.update { it.copy(isDeleteAllData = false) }
            DeletePersonIntent.Dismiss -> viewModelScope.launch {
                _state.update { DeletePersonState() }
                _effect.send(DeletePersonEffect.OnDismiss)
            }

            DeletePersonIntent.ShowAllPersonList -> _state.update {
                it.copy(
                    isPersonListShow = !_state.value.isPersonListShow,
                    movePerson = if (!_state.value.isPersonListShow) _state.value.movePerson else null
                )
            }

            is DeletePersonIntent.SetMovePerson -> _state.update {
                it.copy(
                    movePerson = intent.person,
                    isPersonListShow = false
                )
            }
        }
    }

    private fun deletePerson() {
        viewModelScope.launch {
            val isError = !_state.value.isDeleteAllData && _state.value.movePerson == null
            if (isError) {
                _state.update { it.copy(isPersonError = true) }
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.person_choose))
                return@launch
            }
            _state.value.person?.let { deletePerson ->
                deletePersonUseCase(deletePerson, _state.value.movePerson)
                analytics.track(com.kazemieh.common.analytics.ProductEvent.PersonDeleted)
                _effect.send(DeletePersonEffect.DeletedTransaction)
            }
        }
    }

}

sealed interface DeletePersonIntent {
    data object Submit : DeletePersonIntent
    data object DeleteAllTransaction : DeletePersonIntent
    data object ShowAllPersonList : DeletePersonIntent
    data object Dismiss : DeletePersonIntent
    data object MoveAllTransaction : DeletePersonIntent
    data class SetData(val person: Person? = null) : DeletePersonIntent
    data class SetMovePerson(val person: Person? = null) : DeletePersonIntent
}

data class DeletePersonState(
    val person: Person? = null,
    val movePerson: Person? = null,
    val isPersonError: Boolean = false,
    val isPersonListShow: Boolean = false,
    val isDeleteAllData: Boolean = true
)

sealed interface DeletePersonEffect {
    object DeletedTransaction : DeletePersonEffect
    data object OnDismiss : DeletePersonEffect
}
