package com.kazemieh.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.DeleteRecentSearchUseCase
import com.kazemieh.domain.usecase.GetRecentSearchesUseCase
import com.kazemieh.domain.usecase.ObserveTransactionsUseCase
import com.kazemieh.domain.usecase.SaveRecentSearchUseCase
import com.kazemieh.domain.usecase.SearchCategoriesUseCase
import com.kazemieh.domain.usecase.SearchPersonsUseCase
import com.kazemieh.domain.usecase.SearchSourcesUseCase
import com.kazemieh.domain.usecase.SearchTagsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import com.kazemieh.common.model.TransactionType
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val searchCategoriesUseCase: SearchCategoriesUseCase,
    private val searchSourcesUseCase: SearchSourcesUseCase,
    private val searchPersonsUseCase: SearchPersonsUseCase,
    private val searchTagsUseCase: SearchTagsUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val deleteRecentSearchUseCase: DeleteRecentSearchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SearchEffect>()
    val effect = _effect.receiveAsFlow()

    private val _query = MutableStateFlow("")
    private val _filterParams = MutableStateFlow(TransactionFilterParams())

    init {
        getRecentSearchesUseCase()
            .onEach { recents ->
                _state.update { it.copy(recentSearches = recents) }
            }
            .launchIn(viewModelScope)

        combine(_query, _filterParams) { q, params ->
            params.copy(query = q)
        }
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { params ->
                val q = params.query ?: ""
                if (q.isNotBlank() || params.type != null) {
                    _state.update { it.copy(isLoading = true) }
                } else {
                    _state.update {
                        it.copy(
                            transactions = emptyList(),
                            categories = emptyList(),
                            sources = emptyList(),
                            persons = emptyList(),
                            tags = emptyList(),
                            isLoading = false
                        )
                    }
                }
            }
            .flatMapLatest { params ->
                val q = params.query ?: ""
                combine(
                    observeTransactionsUseCase(
                        params,
                        PageRequest(limit = 20, offset = 0)
                    ),
                    searchCategoriesUseCase(q),
                    searchSourcesUseCase(q),
                    searchPersonsUseCase(q),
                    searchTagsUseCase(q)
                ) { txPage, cats, sources, persons, tags ->
                    SearchData(txPage.items, cats, sources, persons, tags)
                }
            }
            .onEach { data ->
                _state.update {
                    it.copy(
                        transactions = data.transactions,
                        categories = data.categories,
                        sources = data.sources,
                        persons = data.persons,
                        tags = data.tags,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                _query.value = intent.query
                _state.update { it.copy(query = intent.query) }
            }

            is SearchIntent.ClearQuery -> {
                _query.value = ""
                _filterParams.value = TransactionFilterParams()
                _state.update { it.copy(query = "") }
            }

            is SearchIntent.SelectRecentSearch -> {
                saveRecentSearch(intent.query)
                _query.value = intent.query
                _state.update { it.copy(query = intent.query) }
            }

            is SearchIntent.SelectQuickFilter -> {
                val type = when (intent.filter) {
                    QuickFilter.INCOME -> TransactionType.INCOME
                    QuickFilter.EXPENSE -> TransactionType.EXPENSE
                    QuickFilter.TRANSFER -> TransactionType.TRANSFER
                    QuickFilter.RECENT_TRANSACTIONS -> TransactionType.ALL
                }

                val query = when (intent.filter) {
                    QuickFilter.INCOME -> "درآمد"
                    QuickFilter.EXPENSE -> "هزینه"
                    QuickFilter.TRANSFER -> "انتقال"
                    QuickFilter.RECENT_TRANSACTIONS -> ""
                }

                _filterParams.update { it.copy(type = type.count) }
                _query.value = query
                _state.update { it.copy(query = query) }
            }

            is SearchIntent.DeleteRecentSearch -> {
                viewModelScope.launch {
                    deleteRecentSearchUseCase(intent.query)
                }
            }
        }
    }

    // This would typically be called when a user selects a result
    private fun saveRecentSearch(query: String) {
        viewModelScope.launch {
            saveRecentSearchUseCase(query)
        }
    }

    private data class SearchData(
        val transactions: List<TransactionWithRelations>,
        val categories: List<Category>,
        val sources: List<Source>,
        val persons: List<Person>,
        val tags: List<Tag>
    )
}
