package com.kazemieh.financialsource.ui.delete


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.DeleteSourceUseCase
import com.kazemieh.domain.usecase.ObserveSourceUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.source_choose
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeleteSourceViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val deleteSourceUseCase: DeleteSourceUseCase,
    private val observeSourceUseCase: ObserveSourceUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteSourceState())
    val state: StateFlow<DeleteSourceState> = _state.asStateFlow()

    private val _effect = Channel<DeleteSourceEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeleteSourceIntent) {
        when (intent) {
            is DeleteSourceIntent.SetData -> getDefaultData(intent.source)

            DeleteSourceIntent.Submit -> deleteSource()
            DeleteSourceIntent.DeleteAllTransaction -> _state.update {
                it.copy(
                    isDeleteAllData = true,
                    moveSource = null,
                    isSourceError = false
                )
            }

            DeleteSourceIntent.MoveAllTransaction -> _state.update { it.copy(isDeleteAllData = false) }
            DeleteSourceIntent.Dismiss -> viewModelScope.launch {
                _state.update { DeleteSourceState() }
                _effect.send(DeleteSourceEffect.OnDismiss)
            }

            DeleteSourceIntent.ShowAllSourceList -> _state.update {
                it.copy(
                    isSourceListShow = !_state.value.isSourceListShow,
                    moveSource = if (!_state.value.isSourceListShow) _state.value.moveSource else null
                )
            }

            is DeleteSourceIntent.SetMoveSource -> _state.update {
                it.copy(
                    moveSource = intent.source,
                    isSourceListShow = false
                )
            }
        }
    }

    private fun getDefaultData(selectedSource: Source?) {
        if (selectedSource == null) return
        viewModelScope.launch {
            observeSourceUseCase(selectedSource.id ?: 0).collect { source ->
                _state.update {
                    it.copy(source = source)
                }
            }
        }
    }

    private fun deleteSource() {
        viewModelScope.launch {
            val isError = !_state.value.isDeleteAllData && _state.value.moveSource == null
            if (isError) {
                _state.update { it.copy(isSourceError = true) }
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.source_choose))
                return@launch
            }
            _state.value.source?.let { deleteSource ->
                deleteSourceUseCase(deleteSource, _state.value.moveSource)
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("source_deleted"))
                _effect.send(DeleteSourceEffect.DeletedTransaction)
            }
        }
    }

}

sealed interface DeleteSourceIntent {
    data object Submit : DeleteSourceIntent
    data object DeleteAllTransaction : DeleteSourceIntent
    data object ShowAllSourceList : DeleteSourceIntent
    data object Dismiss : DeleteSourceIntent
    data object MoveAllTransaction : DeleteSourceIntent
    data class SetData(val source: Source? = null) : DeleteSourceIntent
    data class SetMoveSource(val source: Source? = null) : DeleteSourceIntent
}


data class DeleteSourceState(
    val source: Source? = null,
    val moveSource: Source? = null,
    val isSourceError: Boolean = false,
    val isSourceListShow: Boolean = false,
    val isDeleteAllData: Boolean = true
)

sealed interface DeleteSourceEffect {
    object DeletedTransaction : DeleteSourceEffect
    data object OnDismiss : DeleteSourceEffect
}
