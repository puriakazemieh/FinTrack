package com.kazemieh.data.repository

import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetHistory
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.MarketRateHistory
import com.kazemieh.data_contract.datasource.AssetLocalDataSource
import com.kazemieh.domain.repository.AssetRepository
import com.kazemieh.network.service.TgjuService
import com.kazemieh.network.service.NobitexService
import com.kazemieh.network.service.BitpinService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AssetRepositoryImpl(
    private val localDataSource: AssetLocalDataSource,
    private val tgjuService: TgjuService,
    private val nobitexService: NobitexService,
    private val bitpinService: BitpinService
) : AssetRepository {

    override fun observeAssets(): Flow<List<Asset>> = localDataSource.observeAssets()

    override suspend fun addAsset(asset: Asset): Long = localDataSource.addAsset(asset)

    override suspend fun updateAsset(asset: Asset) = localDataSource.updateAsset(asset)

    override suspend fun deleteAsset(assetId: Long) = localDataSource.deleteAsset(assetId)

    override suspend fun syncRates(): List<AssetRate> {
        val (tgjuRates, nobitexRates, bitpinRates) = coroutineScope {
            val tgju = async { tgjuService.getLatestRates() }
            val nobitex = async { nobitexService.getLatestRates() }
            val bitpin = async { bitpinService.getLatestRates() }
            Triple(tgju.await(), nobitex.await(), bitpin.await())
        }
        
        // Prefer Nobitex for its supported coins, then fill unavailable coins from Bitpin's
        // public Toman market feed. This also handles networks where api.nobitex.ir cannot
        // be resolved at all without falling back to unsafe/incorrect USD crypto quotes.
        val nobitexCodes = nobitexRates.map { it.code }
        val cryptoRates = nobitexRates + bitpinRates.filter { it.code !in nobitexCodes }
        val cryptoCodes = cryptoRates.map { it.code }.toSet()
        val filteredTgju = tgjuRates.filter { it.code !in cryptoCodes }
        
        val fresh = filteredTgju + cryptoRates
        // TGJU's crypto rows are quoted in USD, not Iranian Rial. Never retain an old
        // non-Nobitex crypto quote: it would be displayed as Toman and look plausibly,
        // but catastrophically, wrong (for example BTC around 8,000 instead of billions).
        val unavailableCryptoCodes = CRYPTO_CODES - cryptoCodes
        if (unavailableCryptoCodes.isNotEmpty()) {
            localDataSource.deleteCachedRates(unavailableCryptoCodes.toList())
        }
        // Persist a successful fetch so the UI keeps showing the last known prices even when the
        // remote source is later unreachable; fall back to the cached snapshot otherwise.
        if (fresh.isNotEmpty()) {
            localDataSource.cacheRates(fresh)
        }
        val rates = fresh.ifEmpty { localDataSource.getCachedRates() }

        val assets = localDataSource.observeAssets().first()
        assets.forEach { asset ->
            val assetId = asset.id
            val code = asset.marketCode ?: asset.legacyMarketCode()
            val rate = code?.let { wantedCode -> rates.find { it.code.equals(wantedCode, ignoreCase = true) } }

            if (assetId != null && rate != null && asset.currentPrice != rate.price) {
                localDataSource.updateAssetPrice(assetId, rate.price)
            }
        }
        return rates
    }

    override fun observeRates(): Flow<List<AssetRate>> = localDataSource.observeCachedRates()

    override fun observeRateHistory(code: String): Flow<List<MarketRateHistory>> =
        localDataSource.observeRateHistory(code)

    override fun observeAssetHistory(assetId: Long): Flow<List<AssetHistory>> =
        localDataSource.observeAssetHistory(assetId)

    /** Best-effort compatibility for assets created before `marketCode` existed. */
    private fun Asset.legacyMarketCode(): String? {
        val normalized = name.lowercase()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace(" ", "")
        return when {
            type == AssetType.GOLD && ("18" in normalized || "۱۸" in normalized) -> "gold_18k"
            type == AssetType.GOLD && ("24" in normalized || "۲۴" in normalized) -> "gold_24k"
            type == AssetType.GOLD && "سکه" in normalized -> "coin_emami"
            type == AssetType.FX && (normalized == "usd" || "دلار" in normalized) -> "usd"
            type == AssetType.FX && (normalized == "eur" || "یورو" in normalized) -> "eur"
            type == AssetType.FX && (normalized == "gbp" || "پوند" in normalized) -> "gbp"
            type == AssetType.FX && (normalized == "aed" || "درهم" in normalized) -> "aed"
            type == AssetType.CRYPTO && (normalized == "btc" || "بیتکوین" in normalized) -> "btc"
            type == AssetType.CRYPTO && (normalized == "eth" || "اتریوم" in normalized) -> "eth"
            type == AssetType.CRYPTO && (normalized == "usdt" || "تتر" in normalized) -> "usdt"
            else -> null
        }
    }

    private companion object {
        val CRYPTO_CODES = setOf("btc", "eth", "usdt", "trx", "doge", "shib", "ada", "xrp", "ton", "sol")
    }
}
