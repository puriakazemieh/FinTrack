package com.kazemieh.data.repository

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
import com.kazemieh.data_contract.datasource.TransactionLocalDataSource
import com.kazemieh.domain.repository.PreferenceRepository
import com.kazemieh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TransactionRepositoryImpl(
    private val localDataSource: TransactionLocalDataSource,
    private val preferenceRepository: PreferenceRepository
) : TransactionRepository {

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())

    init {
        val recents = preferenceRepository.getString("recent_searches", "")
        if (recents.isNotEmpty()) {
            _recentSearches.value = recents.split("|")
        }
    }

    override fun getRecentSearches(): Flow<List<String>> = _recentSearches.asStateFlow()

    override suspend fun saveRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        val limited = current.take(10)
        preferenceRepository.putString("recent_searches", limited.joinToString("|"))
        _recentSearches.update { limited }
    }

    override suspend fun deleteRecentSearch(query: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        preferenceRepository.putString("recent_searches", current.joinToString("|"))
        _recentSearches.update { current }
    }

    override fun observeTransactions(
        transactionFilterParams: TransactionFilterParams,
        request: PageRequest
    ): Flow<Page<TransactionWithRelations>> {
        return localDataSource.observeTransactions(transactionFilterParams, request)
    }

    override fun observeCategorySums(transactionFilterParams: TransactionFilterParams): Flow<List<CategorySum>> {
        return localDataSource.observeCategorySums(transactionFilterParams)
    }

    override suspend fun addTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long =
        localDataSource.addTransactionWithBalance(transaction, tagIds, personIds, balanceDeltas)

    override suspend fun updateTransactionWithBalance(
        transaction: Transaction,
        tagIds: List<Long>,
        personIds: List<Long>,
        balanceDeltas: Map<Long, Int>
    ): Long =
        localDataSource.updateTransactionWithBalance(transaction, tagIds, personIds, balanceDeltas)

    override suspend fun deleteTransactionWithBalance(
        transaction: Transaction,
        balanceDeltas: Map<Long, Int>
    ) = localDataSource.deleteTransactionWithBalance(transaction, balanceDeltas)

    override fun observeCategories(type: TransactionType?, parentId: Long?): Flow<List<Category>> {
        return localDataSource.observeCategories(type, parentId)
    }

    override fun observeCategoriesFlat(type: TransactionType?): Flow<List<Category>> {
        return localDataSource.observeCategoriesFlat(type)
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return localDataSource.getCategoryById(id)
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

    override suspend fun getSourceById(id: Long): Source? {
        return localDataSource.getSourceById(id)
    }

    override fun observeTags(): Flow<List<Tag>> {
        return localDataSource.observeTags()
    }

    override fun observePersons(): Flow<List<Person>> {
        return localDataSource.observePersons()
    }

    override suspend fun getPersonById(id: Long): Person? {
        return localDataSource.getPersonById(id)
    }

    override suspend fun getDefaultCategory(type: TransactionType): Category {
        return localDataSource.getDefaultCategory(type)
    }

    override suspend fun getTransferCategory(): Category {
        return localDataSource.getTransferCategory()
    }

    override suspend fun getSourceByIdentifier(identifier: String): Source? {
        return localDataSource.getSourceByIdentifier(identifier)
    }

    override suspend fun getDefaultSource(): Source? {
        return localDataSource.getDefaultSource()
    }

    override fun observeMostUsedCategories(type: TransactionType?, limit: Long): Flow<List<Category>> {
        return localDataSource.observeMostUsedCategories(type, limit)
    }

    override fun observeMostUsedSources(limit: Long): Flow<List<Source>> {
        return localDataSource.observeMostUsedSources(limit)
    }

    override fun observeMostUsedTags(limit: Long): Flow<List<Tag>> {
        return localDataSource.observeMostUsedTags(limit)
    }

    override fun observeMostUsedPersons(limit: Long): Flow<List<Person>> {
        return localDataSource.observeMostUsedPersons(limit)
    }

    override fun searchCategories(query: String): Flow<List<Category>> {
        return localDataSource.searchCategories(query)
    }

    override fun searchSources(query: String): Flow<List<Source>> {
        return localDataSource.searchSources(query)
    }

    override fun searchPersons(query: String): Flow<List<Person>> {
        return localDataSource.searchPersons(query)
    }

    override fun searchTags(query: String): Flow<List<Tag>> {
        return localDataSource.searchTags(query)
    }

    override suspend fun updateCategoryPositions(positions: Map<Long, Int>) {
        positions.forEach { (id, position) ->
            localDataSource.updateCategoryPosition(id, position)
        }
    }

    override suspend fun updateSourcePositions(positions: Map<Long, Int>) {
        positions.forEach { (id, position) ->
            localDataSource.updateSourcePosition(id, position)
        }
    }

    override suspend fun updateTagPositions(positions: Map<Long, Int>) {
        positions.forEach { (id, position) ->
            localDataSource.updateTagPosition(id, position)
        }
    }

    override suspend fun updatePersonPositions(positions: Map<Long, Int>) {
        positions.forEach { (id, position) ->
            localDataSource.updatePersonPosition(id, position)
        }
    }

    override suspend fun getTransactionAmountRange(): Pair<Long, Long> {
        return localDataSource.getTransactionAmountRange()
    }

    override suspend fun getAllTransactions(): List<Transaction> {
        return localDataSource.getAllTransactions()
    }
}
