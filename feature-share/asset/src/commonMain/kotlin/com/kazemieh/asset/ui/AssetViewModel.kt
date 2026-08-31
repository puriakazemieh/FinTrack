package com.kazemieh.asset.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
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
    data object SyncRates : AssetIntent
}

sealed interface AssetEffect {
    data class ShowMessage(val message: UiText) : AssetEffect
}

class AssetViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val assetUseCases: AssetUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AssetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AssetEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetListViewed)
        onIntent(AssetIntent.LoadAssets)
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
            AssetIntent.SyncRates -> syncRates()
        }
    }

    private fun loadAsset(id: Long) {
        val asset = _state.value.assets.find { it.id == id }
        _state.update { it.copy(selectedAsset = asset) }
    }

    private fun loadHistory(id: Long) {
        viewModelScope.launch {
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

    private fun addAsset(asset: Asset) {
        viewModelScope.launch {
            assetUseCases.addAsset(asset)
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetCreated(asset.type.name))
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_added)))
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
            assetUseCases.syncAssetRates()
            analytics.track(com.kazemieh.common.analytics.ProductEvent.FxRatesViewed)
            _state.update { it.copy(isLoading = false) }
        }
    }
}

private data class AssetData(
    val all: List<Asset>,
    val filtered: List<Asset>,
    val total: Long,
    val comp: Map<AssetType, Double>
)
