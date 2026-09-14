package com.kazemieh.asset.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.domain.usecase.AssetUseCases
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
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
    val searchQuery: String = ""
)

sealed interface AssetIntent {
    data object LoadAssets : AssetIntent
    data class LoadAsset(val id: Long) : AssetIntent
    data class UpdateSearchQuery(val query: String) : AssetIntent
    data class AddAsset(val asset: Asset) : AssetIntent
    data class DeleteAsset(val id: Long) : AssetIntent
    data class UpdateAsset(val asset: Asset) : AssetIntent
    data class LoadHistory(val id: Long) : AssetIntent
    data class MarketRateSelected(val rate: AssetRate) : AssetIntent
    data class AssetTransactionPromptAnswered(val accepted: Boolean) : AssetIntent
    data object SyncRates : AssetIntent
}

sealed interface AssetEffect {
    data class ShowMessage(val message: UiText) : AssetEffect
    data class AssetAdded(val asset: Asset) : AssetEffect
}

class AssetViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val assetUseCases: AssetUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AssetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AssetEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetListViewed)
        onIntent(AssetIntent.LoadAssets)
        observeMarketRates()
    }

    fun onIntent(intent: AssetIntent) {
        when (intent) {
            AssetIntent.LoadAssets -> observeAssets()
            is AssetIntent.LoadAsset -> loadAsset(intent.id)
            is AssetIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is AssetIntent.AddAsset -> addAsset(intent.asset)
            is AssetIntent.DeleteAsset -> deleteAsset(intent.id)
            is AssetIntent.UpdateAsset -> updateAsset(intent.asset)
            is AssetIntent.LoadHistory -> loadHistory(intent.id)
            is AssetIntent.MarketRateSelected -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetMarketSelected(
                    intent.rate.type.name,
                    intent.rate.code
                )
            )
            is AssetIntent.AssetTransactionPromptAnswered -> analytics.track(
                com.kazemieh.common.analytics.ProductEvent.AssetTransactionPromptAnswered(intent.accepted)
            )
            AssetIntent.SyncRates -> syncRates()
        }
    }

    private fun loadAsset(id: Long) {
        val asset = _state.value.assets.find { it.id == id }
        _state.update { it.copy(selectedAsset = asset) }
    }

    private fun loadHistory(id: Long) {
        viewModelScope.launch {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetHistoryViewed)
            assetUseCases.observeAssetHistory(id).collect { history ->
                _state.update { it.copy(history = history) }
            }
        }
    }

    private fun observeAssets() {
        viewModelScope.launch {
            assetUseCases.observeAssets()
                .combine(_searchQuery) { assets, query ->
                    val filtered = assets.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                    val total = assets.sumOf { it.totalCurrentValue }
                    val comp = assets.groupBy { it.type }
                        .mapValues { (_, list) ->
                            if (total != 0L) (list.sumOf { it.totalCurrentValue }.toDouble() / total) else 0.0
                        }
                    AssetData(assets, filtered, total, comp)
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

    private fun addAsset(asset: Asset) {
        viewModelScope.launch {
            val id = assetUseCases.addAsset(asset)
            // A newly tracked market asset should receive a quote immediately when possible,
            // without holding the post-save confirmation hostage to an external request.
            viewModelScope.launch { assetUseCases.syncAssetRates() }
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetCreated(asset.type.name))
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_added)))
            _effect.send(AssetEffect.AssetAdded(asset.copy(id = id)))
        }
    }

    private fun deleteAsset(id: Long) {
        viewModelScope.launch {
            assetUseCases.deleteAsset(id)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetDeleted)
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_deleted)))
        }
    }

    private fun updateAsset(asset: Asset) {
        viewModelScope.launch {
            assetUseCases.updateAsset(asset)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetUpdated)
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_updated)))
        }
    }

    private fun syncRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshRequested)
            try {
                val rates = assetUseCases.syncAssetRates()
                if (rates.isEmpty()) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshFailed)
                } else {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshCompleted(rates.size))
                }
            } catch (_: Exception) {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetRateRefreshFailed)
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
