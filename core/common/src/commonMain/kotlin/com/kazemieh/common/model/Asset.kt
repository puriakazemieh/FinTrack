package com.kazemieh.common.model

import com.kazemieh.common.toSignedPersianPrice
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class AssetType {
    GOLD,
    FX,
    STOCK,
    CRYPTO,
    CUSTOM
}

@Serializable
data class Asset(
    val id: Long? = null,
    val name: String,
    val type: AssetType,
    val quantity: Double,
    val purchasePrice: Long, // Price per unit at purchase
    val currentPrice: Long? = null, // Latest known price per unit
    val currency: String = "IRR",
    /** Stable market identifier (for example `usd`, `gold_18k`, or `btc`). */
    val marketCode: String? = null,
    val description: String? = null,
    val colorId: Int,
    val iconId: Int,
    val lastUpdate: Instant? = null,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity {
    val totalPurchaseValue: Long = (quantity * purchasePrice).toLong()
    val totalCurrentValue: Long = (quantity * (currentPrice ?: purchasePrice)).toLong()
    
    val formattedCurrentValue: String = totalCurrentValue.toSignedPersianPrice()
    val profitOrLoss: Long = totalCurrentValue - totalPurchaseValue
    val profitOrLossPercentage: Double = if (totalPurchaseValue != 0L) (profitOrLoss.toDouble() / totalPurchaseValue) * 100 else 0.0
}

/**
 * Combines purchase and sale lots of the same market into one current position.
 * A negative quantity represents a sale, so summing quantities produces the user's
 * remaining holding while the summed cost basis keeps profit/loss meaningful.
 */
fun List<Asset>.aggregateByMarket(): List<Asset> =
    groupBy { asset ->
        asset.type to (asset.marketCode?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
            ?: asset.name.trim().lowercase())
    }.mapNotNull { (_, lots) ->
        val quantity = lots.sumOf { it.quantity }
        // A fully sold position should not continue to appear as an active asset.
        if (kotlin.math.abs(quantity) < 0.0000001) return@mapNotNull null

        val totalCostBasis = lots.sumOf { it.totalPurchaseValue }
        val latestLot = lots.maxByOrNull { it.lastUpdate?.toEpochMilliseconds() ?: Long.MIN_VALUE }
            ?: return@mapNotNull null
        val latestPrice = latestLot.currentPrice ?: latestLot.purchasePrice
        latestLot.copy(
            quantity = quantity,
            purchasePrice = (totalCostBasis / quantity).toLong(),
            currentPrice = latestPrice
        )
    }.sortedBy { it.name }

@Serializable
data class AssetRate(
    val type: AssetType,
    val code: String, // e.g. "usd", "gold_18k"
    val name: String,
    val price: Long,
    val lastUpdate: Instant
)

/** A locally recorded market quote, used to render real rate movement over time. */
@Serializable
data class MarketRateHistory(
    val code: String,
    val price: Long,
    val date: Instant
)

@Serializable
data class AssetHistory(
    val id: Long? = null,
    val assetId: Long,
    val price: Long,
    val date: Instant,
    override val updatedAt: Long = 0,
    override val syncStatus: SyncStatus = SyncStatus.SYNCED
) : SyncableEntity
