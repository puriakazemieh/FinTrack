package com.kazemieh.network.service

import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Public Bitpin market feed used when Nobitex is not reachable from the user's network.
 * Its IRT pairs are already quoted in Toman, unlike Nobitex's RLS response.
 */
class BitpinService(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getLatestRates(): List<AssetRate> = try {
        val response = client.get("https://api.bitpin.ir/v1/mkt/markets/?page_size=1000") {
            header("Accept", "application/json")
            header("User-Agent", "FinTrack/1.0")
        }.bodyAsText()
        val markets = json.decodeFromString<BitpinMarketsResponse>(response).results
        val now = Clock.System.now()

        markets.asSequence()
            .filter { it.tradable && it.currency2?.code.equals("IRT", ignoreCase = true) }
            .mapNotNull { market ->
                val code = market.currency1?.code?.lowercase() ?: return@mapNotNull null
                val meta = SUPPORTED_ASSETS[code] ?: return@mapNotNull null
                // Bitpin's IRT market represents Toman, so do not divide the price by ten.
                val price = market.price?.toDoubleOrNull()?.toLong() ?: return@mapNotNull null
                price.takeIf { it > 0L }?.let {
                    AssetRate(AssetType.CRYPTO, meta.code, meta.name, it, now)
                }
            }
            .distinctBy { it.code }
            .toList()
    } catch (_: Exception) {
        emptyList()
    }

    private data class RateMeta(val code: String, val name: String)

    private companion object {
        val SUPPORTED_ASSETS = mapOf(
            "btc" to RateMeta("btc", "بیت‌کوین"),
            "eth" to RateMeta("eth", "اتریوم"),
            "usdt" to RateMeta("usdt", "تتر"),
            "trx" to RateMeta("trx", "ترون"),
            "doge" to RateMeta("doge", "دوج‌کوین"),
            "shib" to RateMeta("shib", "شیبا"),
            "ada" to RateMeta("ada", "کاردانو"),
            "xrp" to RateMeta("xrp", "ریپل"),
            "ton" to RateMeta("ton", "تون‌کوین"),
            "sol" to RateMeta("sol", "سولانا")
        )
    }
}

@Serializable
private data class BitpinMarketsResponse(
    val results: List<BitpinMarket> = emptyList()
)

@Serializable
private data class BitpinMarket(
    val currency1: BitpinCurrency? = null,
    val currency2: BitpinCurrency? = null,
    val tradable: Boolean = false,
    val price: String? = null
)

@Serializable
private data class BitpinCurrency(
    val code: String = ""
)
