package com.kazemieh.data.repository

import com.kazemieh.domain.repository.UtilitiesRepository
import com.kazemieh.common.model.FAQItem
import com.kazemieh.common.model.FinancialEvent
import com.kazemieh.common.model.NewsItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class UtilitiesRepositoryImpl : UtilitiesRepository {

    override fun observeNews(): Flow<List<NewsItem>> = flowOf(
        listOf(
            NewsItem(
                id = "news_1",
                title = "", // Handled in UI via ID
                summary = "",
                content = "",
                date = Clock.System.now(),
                category = "market",
                readTimeMinutes = 5,
                source = "eco"
            ),
            NewsItem(
                id = "news_2",
                title = "",
                summary = "",
                content = "",
                date = Clock.System.now().minus(1.days),
                category = "edu",
                readTimeMinutes = 8,
                source = "app"
            )
        )
    )

    override fun observeFAQs(): Flow<List<FAQItem>> = flowOf(
        listOf(
            FAQItem(id = "faq_1", question = "", answer = "", category = "gen"),
            FAQItem(id = "faq_2", question = "", answer = "", category = "sec")
        )
    )

    override fun observeEvents(): Flow<List<FinancialEvent>> = flowOf(
        listOf(
            FinancialEvent(
                id = "event_1",
                title = "",
                description = "",
                date = Clock.System.now().plus(2.days),
                isMajor = true
            ),
            FinancialEvent(
                id = "event_2",
                title = "",
                description = "",
                date = Clock.System.now().plus(15.days),
                isMajor = false
            )
        )
    )

    override suspend fun getNewsById(id: String): NewsItem? = null 
}
