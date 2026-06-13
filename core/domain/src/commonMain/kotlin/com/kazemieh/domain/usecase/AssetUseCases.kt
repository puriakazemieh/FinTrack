package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Asset
import com.kazemieh.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

data class AssetUseCases(
    val observeAssets: ObserveAssetsUseCase,
    val addAsset: AddAssetUseCase,
    val updateAsset: UpdateAssetUseCase,
    val deleteAsset: DeleteAssetUseCase,
    val syncAssetRates: SyncAssetRatesUseCase,
    val observeAssetHistory: ObserveAssetHistoryUseCase
)

class ObserveAssetsUseCase(private val repository: AssetRepository) {
    operator fun invoke(): Flow<List<Asset>> = repository.observeAssets()
}

class AddAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(asset: Asset) = repository.addAsset(asset)
}

class UpdateAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(asset: Asset) = repository.updateAsset(asset)
}

class DeleteAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(assetId: Long) = repository.deleteAsset(assetId)
}

class SyncAssetRatesUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke() = repository.syncRates()
}

class ObserveAssetHistoryUseCase(private val repository: AssetRepository) {
    operator fun invoke(assetId: Long) = repository.observeAssetHistory(assetId)
}
