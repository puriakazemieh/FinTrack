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

    override fun observeTransactions(transactionFilterParams: TransactionFilterParams): Flow<PagingData<TransactionWithRelations>> {
        return localDataSource.observeTransactions(transactionFilterParams)
    }

    override fun observeCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> {
        return localDataSource.observeCategorySums(transactionFilterParams)
    }

    override suspend fun addTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long = localDataSource.addTransactionWithBalance(transaction, tagIds, personIds, balanceDeltas)

    override suspend fun updateTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long = localDataSource.updateTransactionWithBalance(transaction, tagIds, personIds, balanceDeltas)

    override suspend fun deleteTransactionWithBalance(
        transaction: Transaction,
        balanceDeltas: Map<Long, Int>
    ) = localDataSource.deleteTransactionWithBalance(transaction, balanceDeltas)

    override fun observeCategories(type: TransactionType): Flow<List<Category>> {
        return localDataSource.observeCategories(type)
    }

    override suspend fun addTag(tag: Tag): Long {
        return localDataSource.addTag(tag)
    }

    override suspend fun addPerson(person: Person): Long {
        return localDataSource.addPerson(person)
    }

    override suspend fun addCategory(category: Category): Long {
        return localDataSource.addCategory(category)
    }

    override suspend fun updateCategory(category: Category): Int {
        return localDataSource.updateCategory(category)
    }

    override suspend fun updateTag(tag: Tag): Int {
        return localDataSource.updateTag(tag)
    }

    override suspend fun updatePerson(person: Person): Int {
        return localDataSource.updatePerson(person)
    }

    override suspend fun updateSource(source: Source): Int {
        return localDataSource.updateSource(source)
    }

    override suspend fun deleteCategory(category: Category, moveCategory: Category?) {
        return localDataSource.deleteCategory(category, moveCategory)
    }

    override suspend fun deleteTag(from: Tag, to: Tag?) {
        return localDataSource.deleteTag(from, to)
    }

    override suspend fun deletePerson(from: Person, to: Person?) {
        return localDataSource.deletePerson(from, to)
    }

    override suspend fun deleteSource(from: Source, to: Source?) {
        return localDataSource.deleteSource(from, to)
    }

    override suspend fun addSource(source: Source): Long {
        return localDataSource.addSource(source)
    }

    override fun observeSources(): Flow<List<Source>> {
        return localDataSource.observeSources()
    }

    override fun observeSource(sourceId: Long): Flow<Source?> {
        return localDataSource.observeSource(sourceId)
    }

    override fun observeTags(): Flow<List<Tag>> {
        return localDataSource.observeTags()
    }

    override fun observePersons(): Flow<List<Person>> {
        return localDataSource.observePersons()
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
