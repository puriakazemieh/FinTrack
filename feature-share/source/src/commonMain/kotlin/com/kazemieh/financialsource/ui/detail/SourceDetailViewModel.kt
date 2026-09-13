package com.kazemieh.financialsource.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.analytics.AnalyticsService
import com.kazemieh.common.analytics.ProductEvent
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.ObserveSourceUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SourceDetailState(
    val source: Source? = null,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val isLoading: Boolean = false
)

class SourceDetailViewModel(
    private val analytics: AnalyticsService,
    private val sourceId: Long,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val observeSourceUseCase: ObserveSourceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SourceDetailState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        analytics.track(ProductEvent.FeatureOpened("source_detail"))
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observeSourceUseCase(sourceId).collect { source ->
                _state.update { it.copy(source = source) }
            }
        }

        viewModelScope.launch {
            observeTransactionsUseCase(
                TransactionFilterParams(
                    sources = setOf(Source(id = sourceId, name = "", colorId = 1, iconId = 1)),
                    isAllSources = false
                ),
                PageRequest(limit = Int.MAX_VALUE, offset = 0)
            ).collect { page ->
                _state.update { it.copy(transactions = page.items, isLoading = false) }
            }
        }
    }
}
