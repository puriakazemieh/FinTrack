package com.kazemieh.tag.ui.delete


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.DeleteTagUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.tag_choose
import fintrack.core.designsystem.generated.resources.transaction_delete_failed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeleteTagViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val deleteTagUseCase: DeleteTagUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteTagState())
    val state: StateFlow<DeleteTagState> = _state.asStateFlow()

    private val _effect = Channel<DeleteTagEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeleteTagIntent) {
        when (intent) {
            is DeleteTagIntent.SetData -> _state.update { it.copy(tag = intent.tag) }

            DeleteTagIntent.Submit -> deleteTag()
            DeleteTagIntent.DeleteAllTransaction -> _state.update {
                it.copy(
                    isDeleteAllData = true,
                    moveTag = null,
                    isTagError = false
                )
            }

            DeleteTagIntent.MoveAllTransaction -> _state.update { it.copy(isDeleteAllData = false) }
            DeleteTagIntent.Dismiss -> viewModelScope.launch {
                _state.update { DeleteTagState() }
                _effect.send(DeleteTagEffect.OnDismiss)
            }

            DeleteTagIntent.ShowAllTagList -> _state.update {
                it.copy(
                    isTagListShow = !_state.value.isTagListShow,
                    moveTag = if (!_state.value.isTagListShow) _state.value.moveTag else null
                )
            }

            is DeleteTagIntent.SetMoveTag -> _state.update {
                it.copy(
                    moveTag = intent.tag,
                    isTagListShow = false
                )
            }
        }
    }

    private fun deleteTag() {
        viewModelScope.launch {
            val isError = !_state.value.isDeleteAllData && _state.value.moveTag == null
            if (isError) {
                _state.update { it.copy(isTagError = true) }
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.tag_choose))
                return@launch
            }
            _state.value.tag?.let { deleteTag ->
                deleteTagUseCase(deleteTag, _state.value.moveTag)
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("tag_deleted"))
                _effect.send(DeleteTagEffect.DeletedTransaction)
            }
        }
    }

}

sealed interface DeleteTagIntent {
    data object Submit : DeleteTagIntent
    data object DeleteAllTransaction : DeleteTagIntent
    data object ShowAllTagList : DeleteTagIntent
    data object Dismiss : DeleteTagIntent
    data object MoveAllTransaction : DeleteTagIntent
    data class SetData(val tag: Tag? = null) : DeleteTagIntent
    data class SetMoveTag(val tag: Tag? = null) : DeleteTagIntent
}

data class DeleteTagState(
    val tag: Tag? = null,
    val moveTag: Tag? = null,
    val isTagError: Boolean = false,
    val isTagListShow: Boolean = false,
    val isDeleteAllData: Boolean = true
)

sealed interface DeleteTagEffect {
    object DeletedTransaction : DeleteTagEffect
    data object OnDismiss : DeleteTagEffect
}
