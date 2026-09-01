package com.kazemieh.common.model

import kotlin.time.Instant

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val date: Instant,
    val category: String,
    val readTimeMinutes: Int,
    val source: String
)

data class FAQItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)

data class FinancialEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: Instant,
    val isMajor: Boolean
)
