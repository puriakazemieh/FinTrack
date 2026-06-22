package com.kazemieh.asset.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Asset
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
    val isLoading: Boolean = false,
    val totalValue: Long = 0,
    val composition: Map<AssetType, Double> = emptyMap(),
    val searchQuery: String = ""
)

sealed interface AssetIntent {
    data object LoadAssets : AssetIntent
    data class UpdateSearchQuery(val query: String) : AssetIntent
    data class AddAsset(val asset: Asset) : AssetIntent
    data class DeleteAsset(val id: Long) : AssetIntent
    data class UpdateAsset(val asset: Asset) : AssetIntent
    data object SyncRates : AssetIntent
}

sealed interface AssetEffect {
    data class ShowMessage(val message: UiText) : AssetEffect
}

class AssetViewModel(
    private val assetUseCases: AssetUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AssetState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AssetEffect>()
    val effect = _effect.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        onIntent(AssetIntent.LoadAssets)
    }

    fun onIntent(intent: AssetIntent) {
        when (intent) {
            AssetIntent.LoadAssets -> observeAssets()
            is AssetIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is AssetIntent.AddAsset -> addAsset(intent.asset)
            is AssetIntent.DeleteAsset -> deleteAsset(intent.id)
            is AssetIntent.UpdateAsset -> updateAsset(intent.asset)
            AssetIntent.SyncRates -> syncRates()
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
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_added)))
        }
    }

    private fun deleteAsset(id: Long) {
        viewModelScope.launch {
            assetUseCases.deleteAsset(id)
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_deleted)))
        }
    }

    private fun updateAsset(asset: Asset) {
        viewModelScope.launch {
            assetUseCases.updateAsset(asset)
            _effect.send(AssetEffect.ShowMessage(UiText.StringResourceText(Res.string.msg_asset_updated)))
        }
    }

    private fun syncRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            assetUseCases.syncAssetRates()
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
