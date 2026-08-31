package com.kazemieh.financialsource.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.domain.usecase.UpdateSourcePositionsUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourceViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val observeSourcesUseCase: ObserveSourcesUseCase,
    private val updateSourcePositionsUseCase: UpdateSourcePositionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SourceEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    fun onIntent(intent: SourceIntent) {
        when (intent) {
            SourceIntent.LoadAllSource -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("source_list"))
                loadAllFinancialSource()
            }

            is SourceIntent.UpdateSearchQuery -> _searchQuery.value = intent.query

            SourceIntent.OnAddSourceClick -> _state.update {
                it.copy(isAddShow = !_state.value.isAddShow, selectedSources = null)
            }

            is SourceIntent.SelectedSource -> {
                viewModelScope.launch {
                    _effect.send(SourceEffect.AddedSource(intent.selectedSources))
                    _searchQuery.value = ""
                    _state.update { SourceState() }
                }
            }

            SourceIntent.OnDismiss -> {
                viewModelScope.launch {
                    _effect.send(SourceEffect.OnDismiss)
                }
            }

            SourceIntent.ResetFlags -> {
                _state.update {
                    it.copy(
                        isAddShow = false,
                        isDeleteShow = false,
                        selectedSources = null,
                        isReorderShow = false
                    )
                }
            }

            SourceIntent.OnToggleReorder -> {
                _state.update { it.copy(isReorderShow = !it.isReorderShow) }
            }

            is SourceIntent.UpdatePositions -> {
                viewModelScope.launch {
                    updateSourcePositionsUseCase(intent.positions)
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
    val totalBalance: Long = 0,
    val isReorderShow: Boolean = false
)


sealed interface SourceIntent {
    data object LoadAllSource : SourceIntent
    data class UpdateSearchQuery(val query: String) : SourceIntent
    data object OnAddSourceClick : SourceIntent
    data object OnDismiss : SourceIntent
    data class SelectedSource(val selectedSources: Source) : SourceIntent
    data class OnEditClick(val source: Source) : SourceIntent
    data class OnDeleteClick(val source: Source? = null) : SourceIntent
    data object ResetFlags : SourceIntent
    data object OnToggleReorder : SourceIntent
    data class UpdatePositions(val positions: Map<Long, Int>) : SourceIntent
}

sealed interface SourceEffect {
    data class AddedSource(val source: Source) : SourceEffect
    data object OnDismiss : SourceEffect
}