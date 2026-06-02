package com.kazemieh.financialsource.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.common.SnackbarController
import com.kazemieh.designsystem.component.model.resolveString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.check_name_financial_source
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourceViewModel(
    private val observeSourcesUseCase: ObserveSourcesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SourceEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    fun onIntent(intent: SourceIntent) {
        when (intent) {
            SourceIntent.LoadAllSource -> loadAllFinancialSource()

            is SourceIntent.UpdateSearchQuery -> _searchQuery.value = intent.query

            SourceIntent.OnAddSourceClick -> _state.update {
                it.copy(isAddShow = !_state.value.isAddShow, selectedSources = null)
            }

            is SourceIntent.SelectedSource -> {
                viewModelScope.launch {

                    _effect.send(SourceEffect.AddedSource(intent.selectedSources))
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
            observeSourcesUseCase()
                .combine(_searchQuery) { sources, query ->
                    val filtered = sources.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.description?.contains(query, ignoreCase = true) == true
                    }
                    val total = sources.sumOf { it.balance.toLong() }
                    Triple(sources, filtered, total)
                }
                .collect { (all, filtered, total) ->
                    _state.update {
                        it.copy(
                            sources = all,
                            filteredSources = filtered,
                            items = filtered.map { s -> s.toItemUi() }.toSet(),
                            totalBalance = total,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }
    }


}

data class SourceState(
    val sources: List<Source> = emptyList(),
    val filteredSources: List<Source> = emptyList(),
    val selectedSources: Source? = null,
    val items: Set<ItemUi> = emptySet(),
    val isDeleteShow: Boolean = false,
    val isAddShow: Boolean = false,
    val searchQuery: String = "",
    val totalBalance: Long = 0
)


sealed interface SourceIntent {
    data object LoadAllSource : SourceIntent
    data class UpdateSearchQuery(val query: String) : SourceIntent
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