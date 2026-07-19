package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetRate
import com.kazemieh.data_contract.datasource.AssetLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toAsset
import com.kazemieh.database.mapper.toAssetHistory
import com.kazemieh.database.mapper.toAssetRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class AssetLocalDataSourceImpl(
    private val db: FinTrackDatabase
) : AssetLocalDataSource {

    private val assetQueries = db.assetQueries
    private val rateCacheQueries = db.rateCacheQueries

    override fun observeAssets(): Flow<List<Asset>> {
        return assetQueries.observeAssets()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toAsset() } }
    }

    override suspend fun addAsset(asset: Asset): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
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
            lastUpdate = asset.lastUpdate?.toEpochMilliseconds(),
            updatedAt = now,
            syncStatus = 1
        )
        assetQueries.lastInsertRowId().awaitAsOne()
    }

    override suspend fun updateAsset(asset: Asset) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
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
                updatedAt = now,
                syncStatus = 1,
                id = asset.id ?: 0
            )
        }
    }

    override suspend fun deleteAsset(assetId: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            assetQueries.deleteAsset(now, assetId)
        }
    }

    override suspend fun updateAssetPrice(assetId: Long, currentPrice: Long) {
        withContext(Dispatchers.Default) {
            db.transaction {
                val now = Clock.System.now().toEpochMilliseconds()
                assetQueries.updateAssetPrice(currentPrice, now, now, 1, assetId)
                assetQueries.addAssetHistory(assetId, currentPrice, now, now, 1)
            }
        }
    }

    override fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>> {
        return assetQueries.observeAssetHistory(assetId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toAssetHistory() } }
    }

    override fun observeCachedRates(): Flow<List<AssetRate>> {
        return rateCacheQueries.observeRates()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toAssetRate() } }
    }

    override suspend fun getCachedRates(): List<AssetRate> = withContext(Dispatchers.Default) {
        rateCacheQueries.getAllRates().awaitAsList().map { it.toAssetRate() }
    }

    override suspend fun cacheRates(rates: List<AssetRate>) {
        if (rates.isEmpty()) return
        withContext(Dispatchers.Default) {
            db.transaction {
                rates.forEach { rate ->
                    rateCacheQueries.upsertRate(
                        code = rate.code,
                        type = rate.type,
                        name = rate.name,
                        price = rate.price,
                        lastUpdate = rate.lastUpdate.toEpochMilliseconds()
                    )
                }
            }
        }
    }

    override suspend fun getAllAssets(): List<Asset> = withContext(Dispatchers.Default) {
        assetQueries.observeAssets().awaitAsList().map { it.toAsset() }
    }

    override suspend fun insertFullAsset(asset: Asset) {
        withContext(Dispatchers.Default) {
            assetQueries.insertFullAsset(
                id = asset.id ?: 0,
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
                updatedAt = asset.updatedAt,
                syncStatus = asset.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getAllAssetHistory(): List<AssetHistory> = withContext(Dispatchers.Default) {
        assetQueries.getAllAssetHistory().awaitAsList().map { it.toAssetHistory() }
    }

    override suspend fun insertFullAssetHistory(history: AssetHistory) {
        withContext(Dispatchers.Default) {
            assetQueries.insertFullAssetHistory(
                id = history.id ?: 0,
                assetId = history.assetId,
                price = history.price,
                date = history.date.toEpochMilliseconds(),
                updatedAt = history.updatedAt,
                syncStatus = history.syncStatus.value.toLong()
            )
        }
    }

    override suspend fun getModifiedAssets(): List<Asset> = withContext(Dispatchers.Default) {
        assetQueries.getModifiedAssets().awaitAsList().map { it.toAsset() }
    }

    override suspend fun markAssetAsSynced(id: Long) {
        assetQueries.markAssetAsSynced(id)
    }

    override suspend fun physicallyDeleteAsset(id: Long) {
        assetQueries.physicallyDeleteAsset(id)
    }
}
