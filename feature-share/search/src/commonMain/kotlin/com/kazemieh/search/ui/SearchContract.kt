package com.kazemieh.search.ui

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionWithRelations

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val transactions: List<TransactionWithRelations> = emptyList(),
    val categories: List<Category> = emptyList(),
    val sources: List<Source> = emptyList(),
    val persons: List<Person> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val quickFilters: List<QuickFilter> = QuickFilter.entries
)

sealed interface SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data class ClearQuery(val query: String = "") : SearchIntent
    data class SelectRecentSearch(val query: String) : SearchIntent
    data class SelectQuickFilter(val filter: QuickFilter) : SearchIntent
    data class DeleteRecentSearch(val query: String) : SearchIntent
}

sealed interface SearchEffect {
    data class NavigateToTransaction(val transaction: TransactionWithRelations) : SearchEffect
    data class NavigateToCategory(val category: Category) : SearchEffect
    data class NavigateToSource(val source: Source) : SearchEffect
    data class NavigateToPerson(val person: Person) : SearchEffect
    data class NavigateToTag(val tag: Tag) : SearchEffect
    data object GoBack : SearchEffect
}

enum class QuickFilter {
    INCOME, EXPENSE, TRANSFER, RECENT_TRANSACTIONS
}
