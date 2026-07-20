package com.kazemieh.data.repository

import com.kazemieh.common.model.FAQItem
import com.kazemieh.common.model.FinancialEvent
import com.kazemieh.common.model.NewsItem
import com.kazemieh.domain.repository.UtilitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlinx.datetime.*

class UtilitiesRepositoryImpl : UtilitiesRepository {

    private val newsList = GoalEducationContent.allNews

    override fun observeNews(): Flow<List<NewsItem>> = flowOf(newsList)

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

    override suspend fun getNewsById(id: String): NewsItem? {
        return newsList.find { it.id.trim().equals(id.trim(), ignoreCase = true) }
            ?: newsList.firstOrNull()
    }
}
