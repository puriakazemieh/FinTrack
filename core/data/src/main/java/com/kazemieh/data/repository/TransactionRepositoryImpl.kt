package com.kazemieh.data.repository

import androidx.paging.PagingData
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.CategorySum
import com.kazemieh.common.model.FinancialSource
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository {

    override fun getAllTransactions(): Flow<PagingData<TransactionWithRelations>> {
        return localDataSource.getAllTransactions()
    }

    override fun getAllTransactionsByType(type: Int): Flow<List<TransactionWithRelations>> {
        return localDataSource.getAllTransactionsByType(type)
    }

    override fun getAllTransactionsFiltered(
        type: Int?,
        categoryIds: List<Int>,
        sourceIds: List<Int>,
        tagIds: List<Int>,
        personIds: List<Int>,
        fromTimestamp: Long?,
        toTimestamp: Long?
    ): Flow<PagingData<TransactionWithRelations>> {
        return localDataSource.getAllTransactionsFiltered(
            type = type,
            categoryIds = categoryIds,
            sourceIds = sourceIds,
            tagIds = tagIds,
            personIds = personIds,
            fromTimestamp = fromTimestamp,
            toTimestamp = toTimestamp
        )
    }

    override fun getCategorySums(
        type: Int?,
        categoryIds: List<Int>,
        sourceIds: List<Int>,
        tagIds: List<Int>,
        personIds: List<Int>,
        fromTimestamp: Long?,
        toTimestamp: Long?
    ): Flow<List<CategorySum>> {
        return localDataSource.getCategorySums(
            type = type,
            categoryIds = categoryIds,
            sourceIds = sourceIds,
            tagIds = tagIds,
            personIds = personIds,
            fromTimestamp = fromTimestamp,
            toTimestamp = toTimestamp
        )
    }

    override suspend fun insertTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        return localDataSource.insertTransaction(transaction, tagIds, personIds)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        localDataSource.delete(transaction)
    }

    override suspend fun getAllCategory(type: Int): Flow<List<Category>> {
        return localDataSource.getAllCategory(type)
    }

    override suspend fun insertTag(tag: Tag): Long {
        return localDataSource.insertTag(tag)
    }

    override suspend fun insertPerson(person: Person): Long {
        return localDataSource.insertPerson(person)
    }

    override suspend fun insertCategory(category: Category): Long {
        return localDataSource.insertCategory(category)
    }

    override suspend fun insertFinancialSource(financialSource: FinancialSource): Long {
        return localDataSource.insertFinancialSource(financialSource)
    }

    override suspend fun getAllFinancialSource(): Flow<List<FinancialSource>> {
        return localDataSource.getAllFinancialSource()
    }

    override suspend fun getAllTag(): Flow<List<Tag>> {
        return localDataSource.getAllTag()
    }

    override suspend fun getAllPersons(): Flow<List<Person>> {
        return localDataSource.getAllPersons()
    }

    override suspend fun increaseBalanceFinancialSource(id: Long, amount: Int) {
        localDataSource.increaseBalanceFinancialSource(id, amount)
    }

    override suspend fun decreaseBalanceFinancialSource(id: Long, amount: Int) {
        localDataSource.decreaseBalanceFinancialSource(id, amount)
    }

    override suspend fun getDefaultCategory(type: Int): Category {
        return localDataSource.getDefaultCategory(type)
    }

    override suspend fun getTransferCategory(): Category {
        return localDataSource.getTransferCategory()
    }

    override suspend fun getDefaultSource(): FinancialSource {
        return localDataSource.getDefaultSource()
    }
}
