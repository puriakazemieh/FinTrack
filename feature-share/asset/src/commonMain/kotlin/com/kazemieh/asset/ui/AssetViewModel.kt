package com.kazemieh.asset.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.aggregateByMarket
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.AssetUseCases
import com.kazemieh.domain.usecase.AddTransactionUseCase
import com.kazemieh.common.analytics.RefreshTrigger
import com.kazemieh.domain.repository.TransactionRepository
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class AssetState(
    val assets: List<Asset> = emptyList(),
    val filteredAssets: List<Asset> = emptyList(),
    val selectedAsset: Asset? = null,
    val history: List<AssetHistory> = emptyList(),
    val marketRates: List<AssetRate> = emptyList(),
    val isLoading: Boolean = false,
    val totalValue: Long = 0,
    val composition: Map<AssetType, Double> = emptyMap(),
    val searchQuery: String = "",
    val filter: AssetFilter = AssetFilter(),
    val defaultIncomeCategory: Category? = null,
    val defaultExpenseCategory: Category? = null,
    val defaultSource: Source? = null,
    val mostUsedIncomeCategories: List<Category> = emptyList(),
    val mostUsedExpenseCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val mostUsedPersons: List<Person> = emptyList(),
    val mostUsedTags: List<Tag> = emptyList()
)

/**
 * Transaction-related filters are applied only to assets that have a linked
 * purchase/sale transaction. An empty selection means that dimension is not
 * constrained, matching the transaction screen's filter behaviour.
 */
data class AssetFilter(
    val types: Set<AssetType> = emptySet(),
    val categories: Set<Category> = emptySet(),
    val sources: Set<Source> = emptySet(),
    val tags: Set<Tag> = emptySet(),
    val persons: Set<Person> = emptySet()
) {
    val hasTransactionCriteria: Boolean
        get() = categories.isNotEmpty() || sources.isNotEmpty() || tags.isNotEmpty() || persons.isNotEmpty()

    fun toTransactionFilterParams() = TransactionFilterParams(
        categories = categories,
        isAllCategories = categories.isEmpty(),
        sources = sources,
        isAllSources = sources.isEmpty(),
        tags = tags,
        isAllTags = tags.isEmpty(),
        persons = persons,
        isAllPersons = persons.isEmpty()
    )
}

sealed interface AssetIntent {
    /** Sent only by the full asset-list screen, not dashboard widgets or forms sharing this VM. */
    data object AssetListOpened : AssetIntent
    data object LoadAssets : AssetIntent
    data class LoadAsset(val id: Long) : AssetIntent
    data class AssetFormOpened(val isEdit: Boolean) : AssetIntent
    data class AssetFormDismissed(val isEdit: Boolean) : AssetIntent
    data class AssetTypeSelected(val type: AssetType) : AssetIntent
    data class MarketPickerOpened(val type: AssetType) : AssetIntent
    data class UpdateSearchQuery(val query: String) : AssetIntent
    data object FilterOpened : AssetIntent
    data class UpdateFilter(val filter: AssetFilter) : AssetIntent
    data class AddAsset(
        val asset: Asset,
        val registerTransaction: Boolean = false,
        val category: Category? = null,
        val source: Source? = null,
        val persons: Set<Person>? = null,
        val tags: Set<Tag>? = null,
        val isBuy: Boolean = true
    ) : AssetIntent
    data class DeleteAsset(val id: Long, val deleteTransaction: Boolean = false) : AssetIntent
    data class UpdateAsset(val asset: Asset) : AssetIntent
    data class LoadHistory(val id: Long, val type: AssetType) : AssetIntent
    data class ActionsOpened(val type: AssetType) : AssetIntent
    data class ActionSelected(val action: String) : AssetIntent
    data class MarketRateSelected(val rate: AssetRate) : AssetIntent
    data class AssetTransactionPromptAnswered(val accepted: Boolean) : AssetIntent
    data class SyncRates(val trigger: RefreshTrigger) : AssetIntent
}

sealed interface AssetEffect {
    data class ShowMessage(val message: UiText) : AssetEffect
    data class AssetAdded(val asset: Asset) : AssetEffect
}

class AssetViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val assetUseCases: AssetUseCases,
    private val transactionRepository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AssetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AssetEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(AssetFilter())

    init {
        onIntent(AssetIntent.LoadAssets)
        observeMarketRates()
        loadTransactionDefaults()
    }

    fun onIntent(intent: AssetIntent) {
        when (intent) {
            AssetIntent.AssetListOpened -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetListViewed
            )
            AssetIntent.LoadAssets -> observeAssets()
            is AssetIntent.LoadAsset -> loadAsset(intent.id)
            is AssetIntent.AssetFormOpened -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetFormOpened(intent.isEdit)
            )
            is AssetIntent.AssetFormDismissed -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetFormDismissed(intent.isEdit)
            )
            is AssetIntent.AssetTypeSelected -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetTypeSelected(intent.type.name)
            )
            is AssetIntent.MarketPickerOpened -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetMarketPickerOpened(intent.type.name)
            )
            is AssetIntent.UpdateSearchQuery -> {
                if (_searchQuery.value.isBlank() && intent.query.isNotBlank()) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetSearchStarted)
                }
                _searchQuery.value = intent.query
            }
            AssetIntent.FilterOpened -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetFilterOpened
            )
            is AssetIntent.UpdateFilter -> {
                val previous = _filter.value
                _filter.value = intent.filter
                _state.update { it.copy(filter = intent.filter) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FilterApplied("asset"))
                if (intent.filter == AssetFilter() && previous != AssetFilter()) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetFilterCleared)
                } else if (intent.filter != AssetFilter()) {
                    analytics.track(
                        com.kazemieh.common.analytics.ProductEvent.AssetFilterApplied(
                            typeCount = intent.filter.types.size,
                            categoryCount = intent.filter.categories.size,
                            sourceCount = intent.filter.sources.size,
                            tagCount = intent.filter.tags.size,
                            personCount = intent.filter.persons.size
                        )
                    )
                }
            }
            is AssetIntent.AddAsset -> addAsset(intent.asset, intent.registerTransaction, intent.category, intent.source, intent.persons, intent.tags, intent.isBuy)
            is AssetIntent.DeleteAsset -> deleteAsset(intent.id, intent.deleteTransaction)
            is AssetIntent.UpdateAsset -> updateAsset(intent.asset)
            is AssetIntent.LoadHistory -> loadHistory(intent.id, intent.type)
            is AssetIntent.ActionsOpened -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetActionsOpened(intent.type.name)
            )
            is AssetIntent.ActionSelected -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetActionSelected(intent.action)
            )
            is AssetIntent.MarketRateSelected -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetMarketSelected(
                    intent.rate.type.name,
                    intent.rate.code
                )
            )
            is AssetIntent.AssetTransactionPromptAnswered -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetTransactionPromptAnswered(intent.accepted)
            )
            is AssetIntent.SyncRates -> syncRates(intent.trigger)
        }
    }

    private fun loadTransactionDefaults() {
        viewModelScope.launch {
            val defInc = transactionRepository.getDefaultCategory(TransactionType.INCOME)
            val defExp = transactionRepository.getDefaultCategory(TransactionType.EXPENSE)
            val defSrc = transactionRepository.getDefaultSource()
            _state.update {
                it.copy(
                    defaultIncomeCategory = defInc,
                    defaultExpenseCategory = defExp,
                    defaultSource = defSrc
                )
            }
            launch {
                transactionRepository.observeMostUsedCategories(TransactionType.INCOME, 5).collect { list ->
                    _state.update { it.copy(mostUsedIncomeCategories = list) }
                }
            }
            launch {
                transactionRepository.observeMostUsedCategories(TransactionType.EXPENSE, 5).collect { list ->
                    _state.update { it.copy(mostUsedExpenseCategories = list) }
                }
            }
            launch {
                transactionRepository.observeMostUsedSources(5).collect { list ->
                    _state.update { it.copy(mostUsedSources = list) }
                }
            }
        }
    }

    private fun loadAsset(id: Long) {
        viewModelScope.launch {
            val assets = assetUseCases.observeAssets().first()
            val asset = assets.find { it.id == id }
            _state.update { it.copy(selectedAsset = asset) }
        }
    }

    private fun loadHistory(id: Long, type: AssetType) {
        viewModelScope.launch {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetHistoryViewed(type.name))
            assetUseCases.observeAssetHistory(id).collect { history ->
                _state.update { it.copy(history = history) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAssets() {
        viewModelScope.launch {
            val matchedAssetIds = _filter.flatMapLatest { filter ->
                if (!filter.hasTransactionCriteria) {
                    flowOf(AssetTransactionMatches())
                } else {
                    transactionRepository.observeTransactions(
                        transactionFilterParams = filter.toTransactionFilterParams(),
                        request = PageRequest(limit = ASSET_FILTER_TRANSACTION_LIMIT, offset = 0)
                    ).map { page ->
                        AssetTransactionMatches(
                            linkedIds = page.items.mapNotNull { transactionWithRelations ->
                                transactionWithRelations.transaction.description.extractLinkedAssetId()
                            }.toSet(),
                            // Assets created before a stable link was added can still be filtered
                            // through the previous, human-readable transaction description.
                            legacyNames = page.items.mapNotNull { transactionWithRelations ->
                                transactionWithRelations.transaction.description.extractLegacyAssetName()
                            }.toSet()
                        )
                    }
                }
            }

            combine(
                assetUseCases.observeAssets(),
                _searchQuery,
                _filter,
                matchedAssetIds
            ) { assets, query, filter, transactionMatches ->
                    val filtered = assets.filter { asset ->
                        asset.name.contains(query, ignoreCase = true) &&
                            (filter.types.isEmpty() || asset.type in filter.types) &&
                            (!filter.hasTransactionCriteria ||
                                asset.id in transactionMatches.linkedIds ||
                                asset.name in transactionMatches.legacyNames)
                    }
                    val total = assets.sumOf { it.totalCurrentValue }
                    val comp = assets.groupBy { it.type }
                        .mapValues { (_, list) ->
                            if (total != 0L) (list.sumOf { it.totalCurrentValue }.toDouble() / total) else 0.0
                        }
                    AssetData(assets, filtered.aggregateByMarket(), total, comp)
                }
                .collect { data ->
                    _state.update {
                        it.copy(
                            assets = data.all,
                            filteredAssets = data.filtered,
                            totalValue = data.total,
                            composition = data.comp,
                            isLoading = false,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }
    }

    private fun observeMarketRates() {
        viewModelScope.launch {
            assetUseCases.observeAssetRates().collect { rates ->
                _state.update { it.copy(marketRates = rates) }
            }
        }
    }

    private fun addAsset(asset: Asset, registerTransaction: Boolean, category: Category?, source: Source?, persons: Set<Person>?, tags: Set<Tag>?, isBuy: Boolean) {
        viewModelScope.launch {
            val existingAssetId = asset.id
            val isUpdate = existingAssetId != null && existingAssetId != 0L
            val id = if (isUpdate) {
                assetUseCases.updateAsset(asset)
                existingAssetId ?: return@launch
            } else {
                assetUseCases.addAsset(asset)
            }
            // A newly tracked market asset should receive a quote immediately when possible,
            // without holding the post-save confirmation hostage to an external request.
            viewModelScope.launch { assetUseCases.syncAssetRates() }
            if (isUpdate) {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetUpdated(asset.type.name))
            } else {
                analytics.track(
                    com.kazemieh.common.analytics.ProductEvent.AssetCreated(
                        type = asset.type.name,
                        operation = if (isBuy) "buy" else "sell",
                        linkedTransaction = registerTransaction
                    )
                )
            }
            
            if (!isUpdate && registerTransaction && category != null && source != null) {
                try {
                    val tx = Transaction(
                        id = 0L,
                        amount = asset.totalPurchaseValue,
                        categoryId = category.id ?: 0L,
                        sourceId = source.id ?: 0L,
                        description = assetTransactionDescription(asset.name, id, isBuy),
                        type = if (isBuy) TransactionType.EXPENSE else TransactionType.INCOME
                    )
                    addTransactionUseCase(tx, persons?.mapNotNull { it.id } ?: emptyList(), tags?.mapNotNull { it.id } ?: emptyList())
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionCreated("Asset Registration"))
                    analytics.track(
                        com.kazemieh.common.analytics.ProductEvent.AssetLinkedTransactionCreated(
                            if (isBuy) "buy" else "sell"
                        )
                    )
                } catch (e: Exception) {
                    analytics.track(
                        com.kazemieh.common.analytics.ProductEvent.AssetLinkedTransactionFailed(
                            if (isBuy) "buy" else "sell"
                        )
                    )
                }
            }
            
            _effect.send(
                AssetEffect.ShowMessage(
                    UiText.StringResourceText(
                        if (isUpdate) Res.string.msg_asset_updated else Res.string.msg_asset_added
                    )
                )
            )
            _effect.send(AssetEffect.AssetAdded(asset.copy(id = id)))
        }
    }

    private fun deleteAsset(id: Long, deleteTransaction: Boolean) {
        viewModelScope.launch {
            val asset = _state.value.assets.find { it.id == id }
            assetUseCases.deleteAsset(id)
            if (deleteTransaction && asset != null) {
                try {
                    val allTx = transactionRepository.getAllTransactions()
                    val targetTx = allTx.find { it.description.extractLinkedAssetId() == id }
                        ?: allTx.find {
                            it.description == "خرید دارایی ${asset.name}" ||
                                it.description == "فروش دارایی ${asset.name}"
                        }
                    if (targetTx != null) {
                        transactionRepository.deleteTransactionWithBalance(targetTx, emptyMap())
                    }
                } catch (e: Exception) {
                    // Ignore errors
                }
            }
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetDeleted(deleteTransaction))
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_deleted)))
        }
    }

    private fun updateAsset(asset: Asset) {
        viewModelScope.launch {
            assetUseCases.updateAsset(asset)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetUpdated(asset.type.name))
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_updated)))
        }
    }

    private fun syncRates(trigger: RefreshTrigger) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshRequested(trigger))
            try {
                val rates = assetUseCases.syncAssetRates()
                if (rates.isEmpty()) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshFailed(trigger))
                } else {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshCompleted(rates.size, trigger))
                }
            } catch (_: Exception) {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshFailed(trigger))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

private data class AssetData(
    val all: List<Asset>,
    val filtered: List<Asset>,
    val total: Long,
    val comp: Map<AssetType, Double>
)

private data class AssetTransactionMatches(
    val linkedIds: Set<Long> = emptySet(),
    val legacyNames: Set<String> = emptySet()
)

private const val ASSET_FILTER_TRANSACTION_LIMIT = 1_000
private const val ASSET_LINK_PREFIX = "\u2063asset:"
private const val ASSET_LINK_SUFFIX = "\u2063"

private fun assetTransactionDescription(name: String, assetId: Long, isBuy: Boolean): String {
    val title = if (isBuy) "خرید دارایی $name" else "فروش دارایی $name"
    // The marker keeps the relationship stable without exposing technical text in the UI.
    return "$title$ASSET_LINK_PREFIX$assetId$ASSET_LINK_SUFFIX"
}

private fun String?.extractLinkedAssetId(): Long? {
    val value = this ?: return null
    val start = value.indexOf(ASSET_LINK_PREFIX)
    if (start < 0) return null
    val numberStart = start + ASSET_LINK_PREFIX.length
    val end = value.indexOf(ASSET_LINK_SUFFIX, numberStart)
    if (end < 0) return null
    return value.substring(numberStart, end).toLongOrNull()
}

private fun String?.extractLegacyAssetName(): String? {
    val value = this ?: return null
    return when {
        value.startsWith("خرید دارایی ") -> value.removePrefix("خرید دارایی ").substringBefore(ASSET_LINK_PREFIX)
        value.startsWith("فروش دارایی ") -> value.removePrefix("فروش دارایی ").substringBefore(ASSET_LINK_PREFIX)
        else -> null
    }.takeIf { !it.isNullOrBlank() }
}
