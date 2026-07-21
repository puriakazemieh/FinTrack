package com.kazemieh.data.repository

import com.kazemieh.common.model.FAQItem
import com.kazemieh.common.model.FinancialEvent
import com.kazemieh.common.model.NewsItem
import com.kazemieh.domain.repository.UtilitiesRepository
import com.kazemieh.network.service.RssNewsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlinx.datetime.*

class UtilitiesRepositoryImpl(
    private val rssNewsService: RssNewsService
) : UtilitiesRepository {

    // Curated educational content, used as a fallback when the live feed is unreachable.
    private val fallbackNews = GoalEducationContent.allNews

    // The last emitted articles, so the reader can resolve an item by id without re-fetching.
    private var cachedNews: List<NewsItem> = emptyList()

    override fun observeNews(): Flow<List<NewsItem>> = flow {
        val live = rssNewsService.fetchNews(DEFAULT_NEWS_FEED)
        val news = live.ifEmpty { fallbackNews }
        cachedNews = news
        emit(news)
    }

    override fun observeFAQs(): Flow<List<FAQItem>> = flowOf(
        listOf(
            FAQItem(id = "faq_1", question = "", answer = "", category = "gen"),
            FAQItem(id = "faq_2", question = "", answer = "", category = "sec"),
            FAQItem(id = "faq_3", question = "", answer = "", category = "gen")
        )
    )

    override fun observeEvents(): Flow<List<FinancialEvent>> = flowOf(
        listOf(
            FinancialEvent(
                id = "event_1",
                title = "",
                description = "",
                date = Clock.System.now().plus(2, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
                isMajor = true
            ),
            FinancialEvent(
                id = "event_2",
                title = "",
                description = "",
                date = Clock.System.now().plus(15, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
                isMajor = false
            ),
            FinancialEvent(
                id = "event_3",
                title = "",
                description = "",
                date = Clock.System.now().plus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
                isMajor = true
            )
        )
    )

    override suspend fun getNewsById(id: String): NewsItem? =
        cachedNews.find { it.id.trim().equals(id.trim(), ignoreCase = true) }
            ?: fallbackNews.find { it.id.trim().equals(id.trim(), ignoreCase = true) }
            ?: cachedNews.firstOrNull()
            ?: fallbackNews.firstOrNull()

    private companion object {
        // Default Persian economic RSS feed. Falls back to curated content if unreachable.
        const val DEFAULT_NEWS_FEED = "https://www.eghtesadnews.com/fa/rss"
    }
}
