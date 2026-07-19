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

    /**
     * Fetches live market rates, persists them to the local cache on success, and applies them to
     * the user's assets. Returns the freshest rates available — the live result when reachable,
     * otherwise the last cached snapshot so callers never fall back to an empty list.
     */
    suspend fun syncRates(): List<AssetRate>

    /** Last cached market rates, emitted immediately and on every successful [syncRates]. */
    fun observeRates(): Flow<List<AssetRate>>
    fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>>
}
