package com.kazemieh.network.service

import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class NobitexService(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getLatestRates(): List<AssetRate> {
        return try {
            val srcCurrencies = NOBITEX_ASSETS.keys.joinToString(",")
            val url = "https://api.nobitex.ir/market/stats?srcCurrency=$srcCurrencies&dstCurrency=rls"
            
            val text = client.get(url).bodyAsText()
            val response = json.decodeFromString<NobitexStatsResponse>(text)
            
            if (response.status != "ok") return emptyList()

            val now = Clock.System.now()
            val rates = mutableListOf<AssetRate>()

            NOBITEX_ASSETS.forEach { (src, meta) ->
                val key = "$src-rls"
                val stat = response.stats[key]
                if (stat != null && stat.latest.isNotEmpty()) {
                    // Sometimes the latest value could have decimal points (like 0.5) but for Rials it's an integer mostly.
                    val priceRls = stat.latest.toDoubleOrNull()?.toLong() ?: 0L
                    if (priceRls > 0) {
                        // Nobitex returns Rials, but we store Tomans in FinTrack
                        val priceIrt = priceRls / 10 
                        rates.add(AssetRate(meta.type, meta.code, meta.name, priceIrt, now))
                    }
                }
            }
            rates
        } catch (e: Exception) {
            emptyList()
        }
    }

    private data class RateMeta(val type: AssetType, val code: String, val name: String)

    private companion object {
        val NOBITEX_ASSETS: Map<String, RateMeta> = mapOf(
            "btc" to RateMeta(AssetType.FX, "btc", "بیت‌کوین"),
            "eth" to RateMeta(AssetType.FX, "eth", "اتریوم"),
            "usdt" to RateMeta(AssetType.FX, "usdt", "تتر"),
            "trx" to RateMeta(AssetType.FX, "trx", "ترون"),
            "doge" to RateMeta(AssetType.FX, "doge", "دوج‌کوین"),
            "shib" to RateMeta(AssetType.FX, "shib", "شیبا"),
            "ada" to RateMeta(AssetType.FX, "ada", "کاردانو"),
            "xrp" to RateMeta(AssetType.FX, "xrp", "ریپل"),
            "ton" to RateMeta(AssetType.FX, "ton", "تون‌کوین"),
            "sol" to RateMeta(AssetType.FX, "sol", "سولانا")
        )
    }
}

@Serializable
private data class NobitexStatsResponse(
    val status: String,
    val stats: Map<String, NobitexStatItem> = emptyMap()
)

@Serializable
private data class NobitexStatItem(
    val latest: String = ""
)
