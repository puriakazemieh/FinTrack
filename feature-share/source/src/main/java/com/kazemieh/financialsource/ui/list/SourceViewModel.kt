package com.kazemieh.financialsource.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.ItemUi
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.toItemUi
import com.kazemieh.domain.usecase.GetAllSource
import com.kazemieh.financialsource.ui.list.SourceEffect.AddedSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourceViewModel(
    private val getAllSource: GetAllSource
) : ViewModel() {

    private val _state = MutableStateFlow(SourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SourceEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SourceIntent) {
        when (intent) {
            SourceIntent.LoadAllSource -> loadAllFinancialSource()

            SourceIntent.OnAddSourceClick -> _state.update {
                it.copy(isAddShow = !_state.value.isAddShow, selectedSources = null)
            }

            is SourceIntent.SelectedSource -> {
                viewModelScope.launch {

                    _effect.send(AddedSource(intent.selectedSources))
                    _state.update { SourceState() }
                }
            }

            SourceIntent.OnDismiss -> {
                viewModelScope.launch {
                    _effect.send(SourceEffect.OnDismiss)
                    _state.update { SourceState() }
                }
            }

            is SourceIntent.OnDeleteClick -> _state.update {
                it.copy(
                    isDeleteShow = !_state.value.isDeleteShow,
                    selectedSources = intent.source
                )
            }

            is SourceIntent.OnEditClick -> _state.update {
                it.copy(
                    isAddShow = true,
                    selectedSources = intent.source
                )
            }
        }
    }

    private fun loadAllFinancialSource() {
        viewModelScope.launch {
            getAllSource().collect { financialSource ->
                _state.update {
                    it.copy(
                        sources = financialSource,
                        items = financialSource.map { it.toItemUi() }.toSet()
                    )
                }
            }
        }
    }


}

data class SourceState(
    val sources: List<Source> = emptyList(),
    val selectedSources: Source? = null,
    val items: Set<ItemUi> = emptySet(),
    val isDeleteShow: Boolean = false,
    val isAddShow: Boolean = false
)


sealed interface SourceIntent {
    data object LoadAllSource : SourceIntent
    data object OnAddSourceClick : SourceIntent
    data object OnDismiss : SourceIntent
    data class SelectedSource(val selectedSources: Source) : SourceIntent
    data class OnEditClick(val source: Source) : SourceIntent
    data class OnDeleteClick(val source: Source? = null) : SourceIntent
}

sealed interface SourceEffect {
    data class AddedSource(val source: Source) : SourceEffect
    data object OnDismiss : SourceEffect
}