package com.kazemieh.data.repository

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
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource
) : TransactionRepository {

    override fun getAllTransactionsFiltered(transactionFilterParams: TransactionFilterParams): Flow<PagingData<TransactionWithRelations>> {
        return localDataSource.getAllTransactionsFiltered(transactionFilterParams)
    }

    override fun getCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> {
        return localDataSource.getCategorySums(transactionFilterParams)
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

    override suspend fun updateTransaction(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
    ): Long {
        return localDataSource.update(transaction, tagIds, personIds)
    }

    override suspend fun getAllCategory(type: TransactionType): Flow<List<Category>> {
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

    override suspend fun updateCategory(category: Category): Int {
        return localDataSource.updateCategory(category)
    }

    override suspend fun updateTag(tag: Tag): Int {
        return localDataSource.updateTag(tag)
    }

    override suspend fun updateSource(source: Source): Int {
        return localDataSource.updateSource(source)
    }

    override suspend fun deleteCategory(category: Category, moveCategory: Category?) {
        return localDataSource.deleteCategory(category, moveCategory)
    }

    override suspend fun deleteTag(deleteTag: Tag, moveTag: Tag?) {
        return localDataSource.deleteTag(deleteTag, moveTag)
    }

    override suspend fun deleteSource(deleteSource: Source, moveSource: Source?) {
        return localDataSource.deleteSource(deleteSource, moveSource)
    }

    override suspend fun insertFinancialSource(source: Source): Long {
        return localDataSource.insertFinancialSource(source)
    }

    override suspend fun getAllFinancialSource(): Flow<List<Source>> {
        return localDataSource.getAllFinancialSource()
    }

    override fun getSource(sourceId: Long): Flow<Source?> {
        return localDataSource.getSource(sourceId)
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

    override suspend fun getDefaultCategory(type: TransactionType): Category {
        return localDataSource.getDefaultCategory(type)
    }

    override suspend fun getTransferCategory(): Category {
        return localDataSource.getTransferCategory()
    }

    override suspend fun getDefaultSource(): Source? {
        return localDataSource.getDefaultSource()
    }
}
