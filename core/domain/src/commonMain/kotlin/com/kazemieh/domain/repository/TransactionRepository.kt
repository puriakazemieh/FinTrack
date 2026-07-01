package com.kazemieh.domain.repository

import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Page
import com.kazemieh.common.model.PageRequest
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun addTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long

    suspend fun updateTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long

    suspend fun deleteTransactionWithBalance(
        transaction: Transaction,
        balanceDeltas: Map<Long, Int>
    )

    fun observeTransactions(
        transactionFilterParams: TransactionFilterParams,
        request: PageRequest
    ): Flow<Page<TransactionWithRelations>>


    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category): Int
    suspend fun deleteCategory(category: Category, moveCategory: Category?)
    fun observeCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>>
    fun observeCategories(type: TransactionType?, parentId: Long? = null): Flow<List<Category>>
    fun observeCategoriesFlat(type: TransactionType?): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun getDefaultCategory(type: TransactionType): Category
    suspend fun getTransferCategory(): Category


    suspend fun addTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag): Int
    suspend fun deleteTag(from: Tag, to: Tag?)
    fun observeTags(): Flow<List<Tag>>


    suspend fun addPerson(person: Person): Long
    suspend fun updatePerson(person: Person): Int
    suspend fun deletePerson(from: Person, to: Person?)
    fun observePersons(): Flow<List<Person>>
    suspend fun getPersonById(id: Long): Person?


    suspend fun addSource(source: Source): Long
    suspend fun updateSource(source: Source): Int
    suspend fun deleteSource(from: Source, to: Source?)
    fun observeSources(): Flow<List<Source>>
    fun observeSource(sourceId: Long): Flow<Source?>
    suspend fun getSourceById(id: Long): Source?
    suspend fun getSourceByIdentifier(identifier: String): Source?
    suspend fun getDefaultSource(): Source?

    fun observeMostUsedCategories(type: TransactionType?, limit: Long): Flow<List<Category>>
    fun observeMostUsedSources(limit: Long): Flow<List<Source>>
    fun observeMostUsedTags(limit: Long): Flow<List<Tag>>
    fun observeMostUsedPersons(limit: Long): Flow<List<Person>>

    fun searchCategories(query: String): Flow<List<Category>>
    fun searchSources(query: String): Flow<List<Source>>
    fun searchPersons(query: String): Flow<List<Person>>
    fun searchTags(query: String): Flow<List<Tag>>

    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveRecentSearch(query: String)
    suspend fun deleteRecentSearch(query: String)

    suspend fun updateCategoryPositions(positions: Map<Long, Int>)
    suspend fun updateSourcePositions(positions: Map<Long, Int>)
    suspend fun updateTagPositions(positions: Map<Long, Int>)
    suspend fun updatePersonPositions(positions: Map<Long, Int>)

    suspend fun getTransactionAmountRange(): Pair<Long, Long>

    suspend fun getTransactionCount(): Long

    suspend fun getAllTransactions(): List<Transaction>
}
