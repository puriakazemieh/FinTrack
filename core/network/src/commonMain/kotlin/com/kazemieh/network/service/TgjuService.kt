package com.kazemieh.network.service

import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Clock

class TgjuService(private val client: HttpClient) {

    suspend fun getLatestRates(): List<AssetRate> {
        return try {
            val response = client.get("https://www.tgju.org/")
            val html = response.bodyAsText()
            parseRates(html)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRates(html: String): List<AssetRate> {
        val rates = mutableListOf<AssetRate>()
        val now = Clock.System.now()

        // Simple regex-based parsing for demo purposes. 
        // In production, Ksoup would be better for reliability.
        
        val gold18kRegex = """<tr data-market-row="gold_18k">.*?<td class="info-price">.*?<span class="value">(.*?)</span>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val usdRegex = """<tr data-market-row="price_dollar_rl">.*?<td class="info-price">.*?<span class="value">(.*?)</span>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val eurRegex = """<tr data-market-row="price_eur">.*?<td class="info-price">.*?<span class="value">(.*?)</span>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val aedRegex = """<tr data-market-row="price_aed">.*?<td class="info-price">.*?<span class="value">(.*?)</span>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        
        gold18kRegex.find(html)?.groupValues?.get(1)?.let { priceStr ->
            rates.add(AssetRate(AssetType.GOLD, "gold_18k", "طلای ۱۸ عیار", cleanPrice(priceStr), now))
        }

        usdRegex.find(html)?.groupValues?.get(1)?.let { priceStr ->
            rates.add(AssetRate(AssetType.FX, "usd", "دلار آمریکا", cleanPrice(priceStr), now))
        }

        eurRegex.find(html)?.groupValues?.get(1)?.let { priceStr ->
            rates.add(AssetRate(AssetType.FX, "eur", "یورو", cleanPrice(priceStr), now))
        }

        aedRegex.find(html)?.groupValues?.get(1)?.let { priceStr ->
            rates.add(AssetRate(AssetType.FX, "aed", "درهم امارات", cleanPrice(priceStr), now))
        }

        return rates
    }

    private fun cleanPrice(priceStr: String): Long {
        return priceStr.replace(",", "").replace(" ", "").toLongOrNull() ?: 0L
    }
}
