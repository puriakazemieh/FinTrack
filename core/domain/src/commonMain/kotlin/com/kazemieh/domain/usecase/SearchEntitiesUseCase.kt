package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class SearchCategoriesUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: String): Flow<List<Category>> = repository.searchCategories(query)
}

class SearchSourcesUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: String): Flow<List<Source>> = repository.searchSources(query)
}

class SearchPersonsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: String): Flow<List<Person>> = repository.searchPersons(query)
}

class SearchTagsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(query: String): Flow<List<Tag>> = repository.searchTags(query)
}

class GetRecentSearchesUseCase(private val repository: TransactionRepository) {
    operator fun invoke(): Flow<List<String>> = repository.getRecentSearches()
}

class SaveRecentSearchUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(query: String) = repository.saveRecentSearch(query)
}

class DeleteRecentSearchUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(query: String) = repository.deleteRecentSearch(query)
}
