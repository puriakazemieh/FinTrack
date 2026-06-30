package com.kazemieh.domain.repository

import com.kazemieh.common.model.FAQItem
import com.kazemieh.common.model.FinancialEvent
import com.kazemieh.common.model.NewsItem
import kotlinx.coroutines.flow.Flow

interface UtilitiesRepository {
    fun observeNews(): Flow<List<NewsItem>>
    fun observeFAQs(): Flow<List<FAQItem>>
    fun observeEvents(): Flow<List<FinancialEvent>>
    suspend fun getNewsById(id: String): NewsItem?
}
