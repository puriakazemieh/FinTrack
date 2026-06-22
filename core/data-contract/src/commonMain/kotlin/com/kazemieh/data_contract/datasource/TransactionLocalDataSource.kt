package com.kazemieh.data_contract.datasource

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

interface TransactionLocalDataSource {

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

    fun observeCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>>

    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category): Int
    suspend fun updateSource(source: Source): Int
    suspend fun updateTag(tag: Tag): Int
    suspend fun updatePerson(person: Person): Int
    suspend fun deleteCategory(category: Category, moveCategory: Category?)
    suspend fun deleteTag(deleteTag: Tag, moveTag: Tag?)
    suspend fun deletePerson(deletePerson: Person, movePerson: Person?)
    suspend fun deleteSource(deleteSource: Source, moveSource: Source?)
    suspend fun addSource(source: Source): Long
    suspend fun addTag(tag: Tag): Long
    fun observeCategories(type: TransactionType?, parentId: Long? = null): Flow<List<Category>>
    fun observeCategoriesFlat(type: TransactionType?): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    fun observeSources(): Flow<List<Source>>
    fun observeSource(sourceId: Long): Flow<Source?>
    fun observeTags(): Flow<List<Tag>>
    suspend fun adjustSourceBalance(id: Long, delta: Int)
    suspend fun getDefaultCategory(type: TransactionType): Category
    suspend fun getTransferCategory(): Category
    suspend fun getSourceByIdentifier(identifier: String): Source?
    suspend fun getDefaultSource(): Source?


    suspend fun addPerson(person: Person): Long
    fun observePersons(): Flow<List<Person>>

    fun observeMostUsedCategories(type: TransactionType?, limit: Long): Flow<List<Category>>
    fun observeMostUsedSources(limit: Long): Flow<List<Source>>
    fun observeMostUsedTags(limit: Long): Flow<List<Tag>>
    fun observeMostUsedPersons(limit: Long): Flow<List<Person>>

    fun searchCategories(query: String): Flow<List<Category>>
    fun searchSources(query: String): Flow<List<Source>>
    fun searchPersons(query: String): Flow<List<Person>>
    fun searchTags(query: String): Flow<List<Tag>>

    suspend fun updateCategoryPosition(id: Long, position: Int)
    suspend fun updateSourcePosition(id: Long, position: Int)
    suspend fun updateTagPosition(id: Long, position: Int)
    suspend fun updatePersonPosition(id: Long, position: Int)

    suspend fun getTransactionAmountRange(): Pair<Long, Long>

    suspend fun getAllTransactions(): List<Transaction>
    suspend fun insertFullTransaction(transaction: Transaction)
    suspend fun getAllCategories(): List<Category>
    suspend fun insertFullCategory(category: Category)
    suspend fun getAllSources(): List<Source>
    suspend fun insertFullSource(source: Source)
    suspend fun getAllTags(): List<Tag>
    suspend fun insertFullTag(tag: Tag)
    suspend fun getAllPersons(): List<Person>
    suspend fun insertFullPerson(person: Person)

    suspend fun getModifiedTransactions(): List<Transaction>
    suspend fun markTransactionAsSynced(id: Long)
    suspend fun getModifiedCategories(): List<Category>
    suspend fun markCategoryAsSynced(id: Long)
    suspend fun getModifiedSources(): List<Source>
    suspend fun markSourceAsSynced(id: Long)
    suspend fun getModifiedTags(): List<Tag>
    suspend fun markTagAsSynced(id: Long)
    suspend fun getModifiedPersons(): List<Person>
    suspend fun markPersonAsSynced(id: Long)

    suspend fun physicallyDeleteTransaction(id: Long)
    suspend fun physicallyDeleteCategory(id: Long)
    suspend fun physicallyDeleteSource(id: Long)
    suspend fun physicallyDeleteTag(id: Long)
    suspend fun physicallyDeletePerson(id: Long)
}
