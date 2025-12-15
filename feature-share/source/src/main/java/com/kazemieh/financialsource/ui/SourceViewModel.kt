package com.kazemieh.financialsource.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.ItemUi
import com.kazemieh.common.model.toItemUi
import com.kazemieh.domain.usecase.GetAllSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourceViewModel(
    private val getAllSource: GetAllSource
) : ViewModel() {

    private val _state = MutableStateFlow(SourceState())
    val state = _state.asStateFlow()

    fun onIntent(intent: SourceIntent) {
        when (intent) {
            SourceIntent.LoadAllSource -> loadAllFinancialSource()
            SourceIntent.OnAddSourceClick -> _state.update {
                it.copy(isAddShow = !_state.value.isAddShow)
            }

            is SourceIntent.SelectedSources -> {
                val current = _state.value.selectedSources ?: emptySet()
                val selectedSources =
                    if (current.contains(intent.selectedSources))
                        current - intent.selectedSources
                    else
                        current + intent.selectedSources

                _state.update {
                    it.copy(selectedSources = selectedSources)
                }
            }
        }
    }

    private fun loadAllFinancialSource() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllSource().collect { financialSource ->
                _state.update {
                    it.copy(
                        sources = financialSource,
                        items = financialSource.map { it.toItemUi() }.toSet(),
                        isLoading = false
                    )
                }
            }
        }
    }


}

sealed interface SourceIntent {
    data object LoadAllSource : SourceIntent
    data object OnAddSourceClick : SourceIntent
    data class SelectedSources(val selectedSources: Pair<Int, String>) :
        SourceIntent
}

data class SourceState(
    val sources: List<Source> = emptyList(),
    val items: Set<ItemUi> = emptySet(),
    val selectedSources: Set<Pair<Int, String>>? = null,
    val isLoading: Boolean = false,
    val isAddShow: Boolean = false
)
