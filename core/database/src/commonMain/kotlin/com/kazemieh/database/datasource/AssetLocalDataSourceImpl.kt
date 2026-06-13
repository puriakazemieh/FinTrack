package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.data_contract.datasource.AssetLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toAsset
import com.kazemieh.database.mapper.toAssetHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class AssetLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : AssetLocalDataSource {

    private val assetQueries = db.assetQueries

    override fun observeAssets(): Flow<List<Asset>> {
        return assetQueries.observeAssets()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toAsset() } }
    }

    override suspend fun addAsset(asset: Asset): Long = withContext(Dispatchers.Default) {
        assetQueries.addAsset(
            name = asset.name,
            type = asset.type,
            quantity = asset.quantity,
            purchasePrice = asset.purchasePrice,
            currentPrice = asset.currentPrice,
            currency = asset.currency,
            description = asset.description,
            colorId = asset.colorId.toLong(),
            iconId = asset.iconId.toLong(),
            lastUpdate = asset.lastUpdate?.toEpochMilliseconds()
        )
        assetQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun updateAsset(asset: Asset) = withContext(Dispatchers.Default) {
        assetQueries.updateAsset(
            name = asset.name,
            type = asset.type,
            quantity = asset.quantity,
            purchasePrice = asset.purchasePrice,
            currentPrice = asset.currentPrice,
            currency = asset.currency,
            description = asset.description,
            colorId = asset.colorId.toLong(),
            iconId = asset.iconId.toLong(),
            lastUpdate = asset.lastUpdate?.toEpochMilliseconds(),
            id = asset.id ?: 0
        )
        Unit
    }

    override suspend fun deleteAsset(assetId: Long) = withContext(Dispatchers.Default) {
        assetQueries.deleteAsset(assetId)
        Unit
    }

    override suspend fun updateAssetPrice(assetId: Long, currentPrice: Long) = withContext(Dispatchers.Default) {
        db.transaction {
            val now = Clock.System.now().toEpochMilliseconds()
            assetQueries.updateAssetPrice(currentPrice, now, assetId)
            assetQueries.addAssetHistory(assetId, currentPrice, now)
        }
    }

    override fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>> {
        return assetQueries.observeAssetHistory(assetId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toAssetHistory() } }
    }
}
