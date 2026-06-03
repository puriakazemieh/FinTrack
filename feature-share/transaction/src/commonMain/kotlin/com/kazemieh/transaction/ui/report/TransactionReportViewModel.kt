package com.kazemieh.transaction.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.formatted
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.PieChartItem
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionReportViewModel(
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val pageSize = 20

    private val _state = MutableStateFlow(TransactionReportState(currentLimit = pageSize))
    val state: StateFlow<TransactionReportState> = _state.asStateFlow()

    private val requestFlow = MutableStateFlow(PageRequest(limit = pageSize, offset = 0))

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private val filterParamsFlow: Flow<TransactionFilterParams> = state
        .map { s ->
            val type = if (s.filterParams.type == 0) null else s.filterParams.type
            TransactionFilterParams(
                type = type,
                categories = s.filterParams.categories,
                // If type is TRANSFER, we ignore category filtering because transfers don't have user categories
                isAllCategories = s.filterParams.isAllCategories || type == TransactionType.TRANSFER.count,
                sources = s.filterParams.sources,
                isAllSources = s.filterParams.isAllSources,
                tags = s.filterParams.tags,
                isAllTags = s.filterParams.isAllTags,
                persons = s.filterParams.persons,
                isAllPersons = s.filterParams.isAllPersons,
                fromTimestamp = s.filterParams.fromTimestamp,
                toTimestamp = s.filterParams.toTimestamp?.let { 
                    it + 24 * 60 * 60 * 1000L
                }
            )
        }
        .distinctUntilChanged()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            replay = 1
        )

    init {
        observeTransactions()
        observeCategorySums()
        refresh()
    }

    fun onIntent(intent: TransactionReportIntent) {
        when (intent) {
            is TransactionReportIntent.SelectedType -> {
                _state.update { s ->
                    s.copy(filterParams = s.filterParams.copy(type = intent.selectedTransactionType.count))
                }
                resetPaging()
            }

            is TransactionReportIntent.SelectedSource -> {
                _state.update { s ->
                    s.copy(filterParams = s.filterParams.copy(sources = intent.selectedSource))
                }
                resetPaging()
            }

            is TransactionReportIntent.SelectedCategory -> {
                _state.update { s ->
                    s.copy(filterParams = s.filterParams.copy(categories = intent.selectedCategories))
                }
                resetPaging()
            }

            is TransactionReportIntent.SelectedTag -> {
                _state.update { s ->
                    s.copy(filterParams = s.filterParams.copy(tags = intent.selectedTag))
                }
                resetPaging()
            }

            is TransactionReportIntent.SelectedPerson -> {
                _state.update { s ->
                    s.copy(filterParams = s.filterParams.copy(persons = intent.selectedPerson))
                }
                resetPaging()
            }

            is TransactionReportIntent.SelectedDate -> {
                _state.update { s ->
                    s.copy(
                        filterParams = s.filterParams.copy(
                            fromTimestamp = intent.fromTimestamp,
                            toTimestamp = intent.toTimestamp
                        )
                    )
                }
                resetPaging()
            }

            is TransactionReportIntent.SetFilters -> {
                _state.update { s ->
                    s.copy(
                        filterParams = s.filterParams.copy(
                            sources = intent.sources,
                            isAllSources = intent.isAllSources,
                            categories = intent.categories,
                            isAllCategories = intent.isAllCategories,
                            tags = intent.tags,
                            isAllTags = intent.isAllTags,
                            persons = intent.persons,
                            isAllPersons = intent.isAllPersons,
                            type = intent.type.count,
                            fromTimestamp = intent.fromTimestamp,
                            toTimestamp = intent.toTimestamp
                        )
                    )
                }
                resetPaging()
            }

            TransactionReportIntent.LoadNextPage -> loadNextPage()
            TransactionReportIntent.RetryRefresh -> refresh()
            TransactionReportIntent.RetryAppend -> loadNextPage()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                filterParamsFlow,
                requestFlow,
                refreshTrigger.onStart { emit(Unit) }
            ) { params, req, _ -> params to req }
                .flatMapLatest { (params, req) ->
                    transactionUseCaseGroup.observeTransactionsUseCase(
                        transactionFilterParams = params,
                        request = req
                    )
                }
                .catch { e ->
                    val msg = e.message ?: "Unknown error"
                    _state.update { s ->
                        if (s.isAppending) s.copy(isAppending = false, appendError = msg)
                        else s.copy(isRefreshing = false, refreshError = msg)
                    }
                }
                .collectLatest { page ->
                    val items = page.items
                    val limit = page.request.limit

                    _state.update { s ->
                        s.copy(
                            items = items,
                            endReached = items.size < limit,
                            isRefreshing = false,
                            isAppending = false,
                            refreshError = null,
                            appendError = null
                        )
                    }
                }
        }
    }

    private fun refresh() {
        val s = _state.value
        if (s.isRefreshing) return

        _state.update {
            it.copy(
                isRefreshing = true,
                isAppending = false,
                refreshError = null,
                appendError = null,
                items = emptyList(),
                currentLimit = pageSize,
                endReached = false
            )
        }

        requestFlow.value = PageRequest(limit = pageSize, offset = 0)
        refreshTrigger.tryEmit(Unit)
    }

    private fun loadNextPage() {
        val s = _state.value
        if (s.isRefreshing || s.isAppending || s.endReached) return

        val newLimit = s.currentLimit + pageSize

        _state.update {
            it.copy(
                isAppending = true,
                appendError = null,
                currentLimit = newLimit
            )
        }

        requestFlow.value = PageRequest(limit = newLimit, offset = 0)
    }

    private fun resetPaging() {
        _state.update {
            it.copy(
                isRefreshing = true,
                isAppending = false,
                refreshError = null,
                appendError = null,
                items = emptyList(),
                currentLimit = pageSize,
                endReached = false
            )
        }
        requestFlow.value = PageRequest(limit = pageSize, offset = 0)
        refreshTrigger.tryEmit(Unit)
    }

    private fun observeCategorySums() {
        viewModelScope.launch {
            filterParamsFlow
                .flatMapLatest { params -> transactionUseCaseGroup.observeCategorySumsUseCase(params) }
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collectLatest { updateCategorySums(it) }
        }
    }

    private fun updateCategorySums(categorySums: List<CategorySum>) {
        var totalIncome: Long = 0
        var totalExpense: Long = 0
        var totalTransfer: Long = 0
        val pieChartItems = categorySums.map { c ->
            when (c.type) {
                TransactionType.INCOME -> totalIncome += c.totalAmount
                TransactionType.EXPENSE -> totalExpense += c.totalAmount
                TransactionType.TRANSFER -> totalTransfer += c.totalAmount
                else -> Unit
            }
            PieChartItem(
                id = c.categoryId,
                label = c.name,
                value = c.totalAmount,
                colorId = c.colorId,
                iconId = c.iconId
            )
        }

        val balanceValue = totalIncome - totalExpense

        _state.update {
            it.copy(
                balance = balanceValue.formatted(),
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                totalTransfer = totalTransfer,
                isPositiveBalance = balanceValue >= 0,
                pieChartData = pieChartItems,
                categorySums = categorySums
            )
        }
    }
}

sealed interface TransactionReportIntent {
    data class SelectedType(val selectedTransactionType: TransactionType = TransactionType.ALL) :
        TransactionReportIntent

    data class SelectedSource(val selectedSource: Set<Source> = emptySet()) :
        TransactionReportIntent

    data class SelectedTag(val selectedTag: Set<Tag> = emptySet()) : TransactionReportIntent
    data class SelectedPerson(val selectedPerson: Set<Person> = emptySet()) :
        TransactionReportIntent

    data class SelectedCategory(val selectedCategories: Set<Category> = emptySet()) :
        TransactionReportIntent

    data class SelectedDate(val fromTimestamp: Long? = null, val toTimestamp: Long? = null) :
        TransactionReportIntent

    data class SetFilters(
        val sources: Set<Source>,
        val isAllSources: Boolean = true,
        val categories: Set<Category>,
        val isAllCategories: Boolean = true,
        val tags: Set<Tag>,
        val isAllTags: Boolean = true,
        val persons: Set<Person>,
        val isAllPersons: Boolean = true,
        val type: TransactionType,
        val fromTimestamp: Long?,
        val toTimestamp: Long?,
    ) : TransactionReportIntent

    data object LoadNextPage : TransactionReportIntent
    data object RetryRefresh : TransactionReportIntent
    data object RetryAppend : TransactionReportIntent
}

data class TransactionFilterParamsState(
    val type: Int? = null,
    val sources: Set<Source> = emptySet(),
    val isAllSources: Boolean = true,
    val categories: Set<Category> = emptySet(),
    val isAllCategories: Boolean = true,
    val tags: Set<Tag> = emptySet(),
    val isAllTags: Boolean = true,
    val persons: Set<Person> = emptySet(),
    val isAllPersons: Boolean = true,
    val fromTimestamp: Long? = null,
    val toTimestamp: Long? = null
)

data class TransactionReportState(
    val filterParams: TransactionFilterParamsState = TransactionFilterParamsState(),
    val balance: String = "۰",
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val totalTransfer: Long = 0,
    val isPositiveBalance: Boolean = true,
    val pieChartData: List<PieChartItem> = emptyList(),
    val categorySums: List<CategorySum> = emptyList(),

    // list/reactive paging
    val items: List<TransactionWithRelations> = emptyList(),
    val currentLimit: Int = 20,
    val endReached: Boolean = false,
    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    val refreshError: String? = null,
    val appendError: String? = null,

    val error: String? = null,
)