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

class TgjuService(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Fetches live gold / currency / crypto prices. The primary source is tgju.org's public
     * JSON endpoint (structured, keyless, stable); if that fails for any reason we fall back to
     * scraping the HTML homepage. Either way an empty list means "couldn't reach a source", which
     * the UI surfaces as an honest "unavailable + retry" state rather than fake data.
     */
    suspend fun getLatestRates(): List<AssetRate> {
        fetchFromJsonApi().takeIf { it.isNotEmpty() }?.let { return it }
        return fetchFromHtml()
    }

    // --- Primary: structured JSON API ---------------------------------------------------------

    private suspend fun fetchFromJsonApi(): List<AssetRate> {
        return try {
            val text = client.get("https://call.tgju.org/ajax.json") {
                header("User-Agent", "Mozilla/5.0 (Android) FinTrack")
                header("Accept", "application/json")
            }.bodyAsText()

            val response = json.decodeFromString<TgjuAjaxResponse>(text)
            val now = Clock.System.now()

            JSON_KEYS.mapNotNull { (key, meta) ->
                response.current[key]?.p?.let { priceStr ->
                    val price = cleanPrice(priceStr)
                    if (price > 0) AssetRate(meta.type, meta.code, meta.name, price, now) else null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Fallback: HTML scrape ----------------------------------------------------------------

    private suspend fun fetchFromHtml(): List<AssetRate> {
        return try {
            val html = client.get("https://www.tgju.org/") {
                header("User-Agent", "Mozilla/5.0 (Android) FinTrack")
            }.bodyAsText()
            parseHtml(html)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseHtml(html: String): List<AssetRate> {
        val rates = mutableListOf<AssetRate>()
        val now = Clock.System.now()

        fun extract(row: String): String? =
            """<tr data-market-row="$row">.*?<td class="info-price">.*?<span class="value">(.*?)</span>"""
                .toRegex(RegexOption.DOT_MATCHES_ALL)
                .find(html)?.groupValues?.get(1)

        extract("gold_18k")?.let { rates.add(AssetRate(AssetType.GOLD, "gold_18k", "طلای ۱۸ عیار", cleanPrice(it), now)) }
        extract("price_dollar_rl")?.let { rates.add(AssetRate(AssetType.FX, "usd", "دلار آمریکا", cleanPrice(it), now)) }
        extract("price_eur")?.let { rates.add(AssetRate(AssetType.FX, "eur", "یورو", cleanPrice(it), now)) }
        extract("price_aed")?.let { rates.add(AssetRate(AssetType.FX, "aed", "درهم امارات", cleanPrice(it), now)) }

        return rates
    }

    private fun cleanPrice(priceStr: String): Long {
        // Values arrive like "58,900" or "۵۸٬۹۰۰" — normalize Persian digits, strip separators.
        val normalized = priceStr
            .map { ch -> if (ch in '۰'..'۹') ('0' + (ch - '۰')) else ch }
            .joinToString("")
            .replace(",", "")
            .replace("٬", "")
            .replace(" ", "")
            .substringBefore('.')
        return normalized.toLongOrNull() ?: 0L
    }

    private data class RateMeta(val type: AssetType, val code: String, val name: String)

    private companion object {
        val JSON_KEYS: Map<String, RateMeta> = mapOf(
            "geram18" to RateMeta(AssetType.GOLD, "gold_18k", "طلای ۱۸ عیار"),
            "geram24" to RateMeta(AssetType.GOLD, "gold_24k", "طلای ۲۴ عیار"),
            "sekee_emami" to RateMeta(AssetType.GOLD, "coin_emami", "سکه امامی"),
            "price_dollar_rl" to RateMeta(AssetType.FX, "usd", "دلار آمریکا"),
            "price_eur" to RateMeta(AssetType.FX, "eur", "یورو"),
            "price_gbp" to RateMeta(AssetType.FX, "gbp", "پوند انگلیس"),
            "price_aed" to RateMeta(AssetType.FX, "aed", "درهم امارات"),
            "crypto-bitcoin" to RateMeta(AssetType.FX, "btc", "بیت‌کوین"),
            "crypto-ethereum" to RateMeta(AssetType.FX, "eth", "اتریوم")
        )
    }
}

@Serializable
private data class TgjuAjaxResponse(
    val current: Map<String, TgjuItem> = emptyMap()
)

@Serializable
private data class TgjuItem(
    val p: String = ""
)
