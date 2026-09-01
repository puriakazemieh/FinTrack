package com.kazemieh.transaction.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TransactionIntent {
    data object Init : TransactionIntent
    data object Refresh : TransactionIntent
    data object LoadNextPage : TransactionIntent
    data class SetFilter(val params: TransactionFilterParams) : TransactionIntent
}

sealed interface TransactionEffect {
    data class ShowMessage(val message: String) : TransactionEffect
}

data class TransactionState(
    // ===== summary =====
    val balance: String = "0",
    val isPositiveBalance: Boolean = true,
    val formatedTotalIncome: String = "0",
    val totalIncome: Long = 0,
    val formatedTotalTransfer: String = "0",
    val totalTransfer: Long = 0,
    val formatedTotalExpense: String = "0",
    val totalExpense: Long = 0,

    // ===== list =====
    val filterParams: TransactionFilterParams = TransactionFilterParams(),
    val items: List<TransactionWithRelations> = emptyList(),

    val currentLimit: Int = 20,
    val endReached: Boolean = false,

    val isRefreshing: Boolean = false,
    val isAppending: Boolean = false,
    val refreshError: String? = null,
    val appendError: String? = null,
)

class TransactionViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val pageSize = 20

    private val _state = MutableStateFlow(TransactionState(currentLimit = pageSize))
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private val _effects = Channel<TransactionEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<TransactionEffect> = _effects.receiveAsFlow()

    private val requestFlow = MutableStateFlow(PageRequest(limit = pageSize, offset = 0))

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private var listJob: Job? = null

    fun onIntent(intent: TransactionIntent) {
        when (intent) {
            TransactionIntent.Init -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionListViewed)
                observeSummary()
                startObservingTransactionsIfNeeded()
                refresh()
            }

            TransactionIntent.Refresh -> refresh()

            is TransactionIntent.SetFilter -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionFilterApplied)
                
                
                _state.update {
                    it.copy(
                        filterParams = intent.params,
                        items = emptyList(),
                        currentLimit = pageSize,
                        endReached = false,
                        isRefreshing = true,
                        isAppending = false,
                        refreshError = null,
                        appendError = null
                    )
                }
                requestFlow.value = PageRequest(limit = pageSize, offset = 0)
            }

            TransactionIntent.LoadNextPage -> loadNextPage()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startObservingTransactionsIfNeeded() {
        if (listJob != null) return

        val filterFlow = state
            .map { it.filterParams }
            .distinctUntilChanged()

        listJob = combine(
            filterFlow,
            requestFlow,
            refreshTrigger.onStart { emit(Unit) }
        ) { params, req, _ -> params to req }
            .flatMapLatest { (params, req) ->
                transactionUseCaseGroup.observeTransactionsUseCase(
                    transactionFilterParams = params,
                    request = req
                )
            }
            .onEach { page ->
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
            .catch { e ->
                val msg = e.message ?: "Unknown error"
                _state.update { s ->
                    if (s.isAppending) s.copy(isAppending = false, appendError = msg)
                    else s.copy(isRefreshing = false, refreshError = msg)
                }
                _effects.trySend(TransactionEffect.ShowMessage(msg))
            }
            .launchIn(viewModelScope)
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

    private fun observeSummary() {
        viewModelScope.launch {
            combine(
                transactionUseCaseGroup.observeCategorySumsUseCase(TransactionFilterParams()),
                transactionUseCaseGroup.observeSourcesUseCase()
            ) { categorySums, sources ->
                val totalIncome = categorySums.filter { it.type == TransactionType.INCOME }
                    .sumOf { it.totalAmount }

                val totalExpense = categorySums.filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.totalAmount }

                val totalTransfer = categorySums.filter { it.type == TransactionType.TRANSFER }
                    .sumOf { it.totalAmount }

                val balance = sources.sumOf { it.balance.toLong() }

                _state.update {
                    it.copy(
                        formatedTotalIncome = totalIncome.toInt().toSignedPersianPrice(),
                        totalIncome = totalIncome,
                        formatedTotalExpense = totalExpense.toInt().toSignedPersianPrice(),
                        totalExpense = totalExpense,
                        formatedTotalTransfer = totalTransfer.toInt().toSignedPersianPrice(),
                        totalTransfer = totalTransfer,
                        balance = balance.toLong().toSignedPersianPrice(),
                        isPositiveBalance = balance >= 0L
                    )
                }
            }.catch { e ->
                _state.update { it.copy(refreshError = e.message) }
            }.collectLatest { }
        }
    }
}