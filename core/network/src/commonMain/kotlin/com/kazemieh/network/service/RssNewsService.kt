package com.kazemieh.network.service

import com.kazemieh.common.model.NewsItem
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlin.time.Clock

/**
 * Fetches and parses a standard RSS 2.0 feed into [NewsItem]s. Parsing is done with lightweight
 * regex rather than pulling in a multiplatform XML dependency; any failure returns an empty list so
 * the news screen degrades to an honest empty state instead of showing stale placeholders.
 */
class RssNewsService(private val client: HttpClient) {

    suspend fun fetchNews(feedUrl: String): List<NewsItem> {
        return try {
            val xml = client.get(feedUrl) {
                header("User-Agent", "Mozilla/5.0 (Android) FinTrack")
                header("Accept", "application/rss+xml, application/xml, text/xml")
            }.bodyAsText()
            parseRss(xml)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRss(xml: String): List<NewsItem> {
        val now = Clock.System.now()
        val itemRegex = "<item[\\s\\S]*?</item>".toRegex()
        return itemRegex.findAll(xml).mapIndexedNotNull { index, match ->
            val block = match.value
            val title = extractTag(block, "title")
            if (title.isBlank()) return@mapIndexedNotNull null
            val description = stripHtml(extractTag(block, "description"))
            val link = extractTag(block, "link")
            NewsItem(
                id = link.ifBlank { "rss_$index" },
                title = title,
                summary = description.take(220),
                content = description,
                // RSS pubDate is RFC-822; ordering already comes from the feed, so we stamp fetch
                // time rather than pull in a date parser just for display.
                date = now,
                category = "market",
                readTimeMinutes = (description.length / 800).coerceAtLeast(1),
                source = "eco"
            )
        }.take(30).toList()
    }

    private fun extractTag(block: String, tag: String): String {
        val raw = "<$tag[^>]*>([\\s\\S]*?)</$tag>".toRegex()
            .find(block)?.groupValues?.get(1)?.trim() ?: return ""
        // Unwrap CDATA and decode the handful of entities RSS titles/descriptions actually use.
        return raw
            .replace("<![CDATA[", "")
            .replace("]]>", "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .trim()
    }

    private fun stripHtml(text: String): String =
        text.replace("<[^>]*>".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
}
