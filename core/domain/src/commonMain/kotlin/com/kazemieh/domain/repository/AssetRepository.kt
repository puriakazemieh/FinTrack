package com.kazemieh.domain.repository

import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetRate
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun observeAssets(): Flow<List<Asset>>
    suspend fun addAsset(asset: Asset): Long
    suspend fun updateAsset(asset: Asset)
    suspend fun deleteAsset(assetId: Long)
    suspend fun syncRates(): List<AssetRate>
    fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>>
}
