package com.kazemieh.domain.repository

import androidx.paging.PagingData
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.FinancialSource
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insertTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long

    suspend fun deleteTransaction(transaction: Transaction)
    fun getAllTransactions(): Flow<PagingData<TransactionWithRelations>>
    fun getAllTransactionsByType(type: Int): Flow<List<TransactionWithRelations>>
    fun getAllTransactionsFiltered(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        tagIds: List<Int> = emptyList(),
        personIds: List<Int> = emptyList(),
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<PagingData<TransactionWithRelations>>

    fun getCategorySums(
        type: Int? = null,
        categoryIds: List<Int> = emptyList(),
        sourceIds: List<Int> = emptyList(),
        tagIds: List<Int> = emptyList(),
        personIds: List<Int> = emptyList(),
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<List<CategorySum>>

    suspend fun getAllCategory(type: Int): Flow<List<Category>>
    suspend fun insertTag(tag: Tag): Long
    suspend fun insertPerson(person: Person): Long
    suspend fun insertCategory(category: Category): Long
    suspend fun insertFinancialSource(financialSource: FinancialSource): Long
    suspend fun getAllFinancialSource(): Flow<List<FinancialSource>>
    suspend fun getAllTag(): Flow<List<Tag>>
    suspend fun getAllPersons(): Flow<List<Person>>
    suspend fun increaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun getDefaultCategory(type: Int): Category
    suspend fun getTransferCategory(): Category
    suspend fun getDefaultSource(): FinancialSource
}
