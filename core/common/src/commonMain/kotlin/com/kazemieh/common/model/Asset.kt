package com.kazemieh.common.model

import com.kazemieh.common.toSignedPersianPrice
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class AssetType {
    GOLD,
    FX,
    STOCK,
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

@Serializable
data class AssetRate(
    val type: AssetType,
    val code: String, // e.g. "usd", "gold_18k"
    val name: String,
    val price: Long,
    val lastUpdate: Instant
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
