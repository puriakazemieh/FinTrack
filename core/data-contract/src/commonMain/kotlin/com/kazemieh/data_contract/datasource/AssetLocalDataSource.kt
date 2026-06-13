package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import kotlinx.coroutines.flow.Flow

interface AssetLocalDataSource {
    fun observeAssets(): Flow<List<Asset>>
    suspend fun addAsset(asset: Asset): Long
    suspend fun updateAsset(asset: Asset)
    suspend fun deleteAsset(assetId: Long)
    suspend fun updateAssetPrice(assetId: Long, currentPrice: Long)
    fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>>
}
