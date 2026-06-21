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

    suspend fun getAllAssets(): List<Asset>
    suspend fun insertFullAsset(asset: Asset)
    suspend fun getAllAssetHistory(): List<AssetHistory>
    suspend fun insertFullAssetHistory(history: AssetHistory)
    suspend fun getModifiedAssets(): List<Asset>
    suspend fun markAssetAsSynced(id: Long)
    suspend fun physicallyDeleteAsset(id: Long)
}
