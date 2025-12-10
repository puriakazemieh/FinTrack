package com.kazemieh.financialsource.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.component.list.ItemUi
import com.kazemieh.domain.usecase.GetAllFinancialSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FinancialSourceViewModel(
    private val getAllFinancialSource: GetAllFinancialSource
) : ViewModel() {

    private val _state = MutableStateFlow(FinancialSourceState())
    val state = _state.asStateFlow()

    fun onIntent(intent: FinancialSourceIntent) {
        when (intent) {
            FinancialSourceIntent.LoadAllFinancialSource -> loadAllFinancialSource()
            is FinancialSourceIntent.SelectedSources -> {
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
            getAllFinancialSource().collect { financialSource ->
                _state.update {
                    it.copy(
                        sources = financialSource.map { it.toUi() },
                        items = financialSource.map { ItemUi(it.id?.toInt() ?: 0, it.name) },
                        isLoading = false
                    )
                }
            }
        }
    }


}

sealed interface FinancialSourceIntent {
    data object LoadAllFinancialSource : FinancialSourceIntent
    data class SelectedSources(val selectedSources: Pair<Int, String>) :
        FinancialSourceIntent
}

data class FinancialSourceState(
    val sources: List<FinancialSourceUi> = emptyList(),
    val items: List<ItemUi> = emptyList(),
    val selectedSources: Set<Pair<Int, String>>? = null,
    val isLoading: Boolean = false
)
