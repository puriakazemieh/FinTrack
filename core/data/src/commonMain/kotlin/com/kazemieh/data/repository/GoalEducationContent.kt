package com.kazemieh.data.repository

import com.kazemieh.common.model.NewsItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.time.Clock

object GoalEducationContent {
    val allNews = listOf(
        NewsItem(
            id = "edu_smart_goals",
            title = "",
            summary = "",
            content = "",
            date = Clock.System.now(),
            category = "edu",
            readTimeMinutes = 6,
            source = "app"
        ),
        NewsItem(
            id = "edu_financial_freedom_stages",
            title = "",
            summary = "",
            content = "",
            date =Clock.System.now().minus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
            category = "edu",
            readTimeMinutes = 10,
            source = "app"
        ),
        NewsItem(
            id = "edu_financial_basket",
            title = "",
            summary = "",
            content = "",
            date =Clock.System.now().minus(2, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
            category = "edu",
            readTimeMinutes = 8,
            source = "app"
        ),
        NewsItem(
            id = "news_1",
            title = "",
            summary = "",
            content = "",
            date =Clock.System.now(),
            category = "market",
            readTimeMinutes = 5,
            source = "eco"
        ),
        NewsItem(
            id = "news_2",
            title = "",
            summary = "",
            content = "",
            date =Clock.System.now().minus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
            category = "edu",
            readTimeMinutes = 8,
            source = "app"
        ),
        NewsItem(
            id = "news_3",
            title = "",
            summary = "",
            content = "",
            date =Clock.System.now().minus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
            category = "market",
            readTimeMinutes = 4,
            source = "eco"
        )
    )
}
