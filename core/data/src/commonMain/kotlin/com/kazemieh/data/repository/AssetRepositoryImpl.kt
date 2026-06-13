package com.kazemieh.data.repository

import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.data_contract.datasource.AssetLocalDataSource
import com.kazemieh.domain.repository.AssetRepository
import com.kazemieh.network.service.TgjuService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AssetRepositoryImpl(
    private val localDataSource: AssetLocalDataSource,
    private val tgjuService: TgjuService
) : AssetRepository {

    override fun observeAssets(): Flow<List<Asset>> = localDataSource.observeAssets()

    override suspend fun addAsset(asset: Asset): Long = localDataSource.addAsset(asset)

    override suspend fun updateAsset(asset: Asset) = localDataSource.updateAsset(asset)

    override suspend fun deleteAsset(assetId: Long) = localDataSource.deleteAsset(assetId)

    override suspend fun syncRates(): List<AssetRate> {
        val rates = tgjuService.getLatestRates()
        val assets = localDataSource.observeAssets().first()

        assets.forEach { asset ->
            val rate = when (asset.type) {
                AssetType.GOLD -> rates.find { it.type == AssetType.GOLD }
                AssetType.FX -> rates.find { it.code.lowercase() == asset.name.lowercase() || it.type == AssetType.FX } // Simplified
                else -> null
            }

            rate?.let { foundRate ->
                localDataSource.updateAssetPrice(asset.id ?: 0, foundRate.price)
            }
        }
        return@syncRates rates
    }

    override fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>> = 
        localDataSource.observeAssetHistory(assetId)
}
