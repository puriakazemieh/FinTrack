package com.kazemieh.domain.repository

import androidx.paging.PagingData
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun insertTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long

    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun updateTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long

    fun getAllTransactionsFiltered(transactionFilterParams: TransactionFilterParams): Flow<PagingData<TransactionWithRelations>>
    fun getCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>>

    suspend fun getAllCategory(type: TransactionType): Flow<List<Category>>
    suspend fun insertTag(tag: Tag): Long
    suspend fun insertPerson(person: Person): Long
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category): Int
    suspend fun deleteCategory(category: Category)
    suspend fun insertFinancialSource(source: Source): Long
    suspend fun getAllFinancialSource(): Flow<List<Source>>
    suspend fun getAllTag(): Flow<List<Tag>>
    suspend fun getAllPersons(): Flow<List<Person>>
    suspend fun increaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int)
    suspend fun getDefaultCategory(type: TransactionType): Category
    suspend fun getTransferCategory(): Category
    suspend fun getDefaultSource(): Source?
}
