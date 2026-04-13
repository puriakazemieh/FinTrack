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

    suspend fun insertTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long

    suspend fun updateTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long

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
    fun observeCategories(type: TransactionType): Flow<List<Category>>
    fun observeSources(): Flow<List<Source>>
    fun observeSource(sourceId: Long): Flow<Source?>
    fun observeTags(): Flow<List<Tag>>
    suspend fun adjustSourceBalance(id: Long, delta: Int)
    suspend fun getDefaultCategory(type: TransactionType): Category
    suspend fun getTransferCategory(): Category
    suspend fun getDefaultSource(): Source?


    suspend fun addPerson(person: Person): Long
    fun observePersons(): Flow<List<Person>>


}