package com.kazemieh.utilities.ui.news

import com.kazemieh.common.model.NewsItem

data class NewsState(
    val news: List<NewsItem> = emptyList(),
    val selectedArticle: NewsItem? = null,
    val isLoading: Boolean = false
)

sealed interface NewsIntent {
    data object LoadNews : NewsIntent
    data class SelectArticle(val id: String) : NewsIntent
}

sealed interface NewsEffect {
    data class ShowError(val message: String) : NewsEffect
}
