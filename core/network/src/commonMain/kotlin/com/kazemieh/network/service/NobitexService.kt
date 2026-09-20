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

class NobitexService(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getLatestRates(): List<AssetRate> {
        return try {
            // Nobitex returns all Rial markets when only dstCurrency is supplied. This keeps
            // every displayed quote in one consistent response and avoids per-symbol requests.
            val response = fetchRialMarkets()
            if (response.status != "ok") return emptyList()
            val stats = response.stats

            val now = Clock.System.now()
            val rates = mutableListOf<AssetRate>()

            NOBITEX_ASSETS.forEach { (src, meta) ->
                val key = "$src-rls"
                val stat = stats[key]
                if (stat != null && stat.latest.isNotEmpty()) {
                    val priceRls = stat.latest.toDoubleOrNull()?.toLong() ?: 0L
                    if (priceRls > 0) {
                        // `latest` is returned by Nobitex in Rial; FinTrack displays Toman.
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

    private suspend fun fetchRialMarkets(): NobitexStatsResponse {
        val url = "https://api.nobitex.ir/market/stats?dstCurrency=rls"
        val text = client.get(url) {
            header("Accept", "application/json")
            header("User-Agent", "FinTrack/1.0")
        }.bodyAsText()
        return json.decodeFromString(text)
    }

    private data class RateMeta(val type: AssetType, val code: String, val name: String)

    private companion object {
        val NOBITEX_ASSETS: Map<String, RateMeta> = mapOf(
            "btc" to RateMeta(AssetType.CRYPTO, "btc", "بیت‌کوین"),
            "eth" to RateMeta(AssetType.CRYPTO, "eth", "اتریوم"),
            "usdt" to RateMeta(AssetType.CRYPTO, "usdt", "تتر"),
            "trx" to RateMeta(AssetType.CRYPTO, "trx", "ترون"),
            "doge" to RateMeta(AssetType.CRYPTO, "doge", "دوج‌کوین"),
            "shib" to RateMeta(AssetType.CRYPTO, "shib", "شیبا"),
            "ada" to RateMeta(AssetType.CRYPTO, "ada", "کاردانو"),
            "xrp" to RateMeta(AssetType.CRYPTO, "xrp", "ریپل"),
            "ton" to RateMeta(AssetType.CRYPTO, "ton", "تون‌کوین"),
            "sol" to RateMeta(AssetType.CRYPTO, "sol", "سولانا")
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
