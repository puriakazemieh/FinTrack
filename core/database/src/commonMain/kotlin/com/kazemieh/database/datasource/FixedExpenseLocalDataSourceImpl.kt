package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.FixedExpensePosting
import com.kazemieh.data_contract.datasource.FixedExpenseLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.Fixed_expense_posting
import com.kazemieh.database.mapper.toFixedExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class FixedExpenseLocalDataSourceImpl(
    private val database: FinTrackDatabase
) : FixedExpenseLocalDataSource {
    private val queries = database.fixedExpenseQueries
    private val tagQueries = database.fixedExpenseTagQueries
    private val personQueries = database.fixedExpensePersonQueries

    override suspend fun insertFixedExpense(expense: FixedExpense): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        database.transactionWithResult {
            queries.insertFixedExpense(
                title = expense.title,
                amount = expense.amount,
                categoryId = expense.categoryId,
                sourceId = expense.sourceId,
                description = expense.description,
                recurrence = expense.recurrence.name,
                startDate = expense.startDate,
                nextDueDate = expense.nextDueDate,
                endDate = expense.endDate,
                isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
                isActive = if (expense.isActive) 1L else 0L,
                updatedAt = now,
                currencyCode = expense.currencyCode,
                reminderEnabled = if (expense.reminderEnabled) 1L else 0L,
                reminderTime = expense.reminderTime,
                syncStatus = 1
            )
            val id = queries.lastInsertRowId().executeAsOne()
            expense.tagIds.forEach { tagId ->
                tagQueries.insertFixedExpenseTag(id, tagId)
            }
            expense.personIds.forEach { personId ->
                personQueries.insertFixedExpensePerson(id, personId)
            }
            id
        }
    }

    override suspend fun updateFixedExpense(expense: FixedExpense) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            database.transaction {
                queries.updateFixedExpense(
                    title = expense.title,
                    amount = expense.amount,
                    categoryId = expense.categoryId,
                    sourceId = expense.sourceId,
                    description = expense.description,
                    recurrence = expense.recurrence.name,
                    startDate = expense.startDate,
                    nextDueDate = expense.nextDueDate,
                    endDate = expense.endDate,
                    isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
                    isActive = if (expense.isActive) 1L else 0L,
                    updatedAt = now,
                    currencyCode = expense.currencyCode,
                    reminderEnabled = if (expense.reminderEnabled) 1L else 0L,
                    reminderTime = expense.reminderTime,
                    syncStatus = 1,
                    id = expense.id
                )
                tagQueries.deleteTagsByFixedExpenseId(expense.id)
                expense.tagIds.forEach { tagId ->
                    tagQueries.insertFixedExpenseTag(expense.id, tagId)
                }
                personQueries.deletePersonsByFixedExpenseId(expense.id)
                expense.personIds.forEach { personId ->
                    personQueries.insertFixedExpensePerson(expense.id, personId)
                }
            }
        }
    }

    override suspend fun deleteFixedExpense(id: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.deleteFixedExpense(now, id)
        }
    }

    override suspend fun updateNextDueDate(id: Long, nextDueDate: Long) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateNextDueDate(nextDueDate, now, 1, id)
        }
    }

    override suspend fun getFixedExpenseById(id: Long): FixedExpense? = withContext(Dispatchers.Default) {
        queries.getFixedExpenseById(id).executeAsOneOrNull()?.toFixedExpense()
    }

    override fun observeAllFixedExpenses(): Flow<List<FixedExpense>> {
        return queries.observeAllFixedExpenses().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toFixedExpense() }
        }
    }

    override fun observeFixedExpensesFiltered(
        query: String?,
        categoryIds: List<Long>,
        sourceIds: List<Long>,
        tagIds: List<Long>,
        personIds: List<Long>
    ): Flow<List<FixedExpense>> {
        return queries.observeFixedExpensesFiltered(
            query = query,
            categoryIds = categoryIds,
            categoryIdsSize = categoryIds.size.toLong(),
            sourceIds = sourceIds,
            sourceIdsSize = sourceIds.size.toLong(),
            tagIds = tagIds,
            tagIdsSize = tagIds.size.toLong(),
            personIds = personIds,
            personIdsSize = personIds.size.toLong()
        ).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toFixedExpense() }
        }
    }

    override suspend fun getAllFixedExpenses(): List<FixedExpense> = withContext(Dispatchers.Default) {
        queries.observeAllFixedExpenses().awaitAsList().map { it.toFixedExpense() }
    }

    override suspend fun insertFullFixedExpense(expense: FixedExpense) {
        withContext(Dispatchers.Default) {
            database.transaction {
                // REPLACE deletes the old row and cascades its existing relations.
                if (queries.fixedExpenseExistsIncludingDeleted(expense.id).executeAsOne() > 0L) {
                    queries.updateFixedExpense(
                        id = expense.id, title = expense.title, amount = expense.amount,
                        categoryId = expense.categoryId, sourceId = expense.sourceId,
                        description = expense.description, recurrence = expense.recurrence.name,
                        startDate = expense.startDate, nextDueDate = expense.nextDueDate, endDate = expense.endDate,
                        isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
                        isActive = if (expense.isActive) 1L else 0L,
                        updatedAt = expense.updatedAt, syncStatus = expense.syncStatus.value.toLong(),
                        currencyCode = expense.currencyCode,
                        reminderEnabled = if (expense.reminderEnabled) 1L else 0L,
                        reminderTime = expense.reminderTime
                    )
                } else {
                    queries.insertFullFixedExpense(
                        id = expense.id,
                        title = expense.title,
                        amount = expense.amount,
                        categoryId = expense.categoryId,
                        sourceId = expense.sourceId,
                        description = expense.description,
                        recurrence = expense.recurrence.name,
                        startDate = expense.startDate,
                        nextDueDate = expense.nextDueDate,
                        endDate = expense.endDate,
                        isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
                        isActive = if (expense.isActive) 1L else 0L,
                        updatedAt = expense.updatedAt,
                        syncStatus = expense.syncStatus.value.toLong(),
                        currencyCode = expense.currencyCode,
                        reminderEnabled = if (expense.reminderEnabled) 1L else 0L,
                        reminderTime = expense.reminderTime,
                    )
                }
            }
        }
    }

    override fun observeFixedExpensePostings(): Flow<List<FixedExpensePosting>> =
        database.fixedExpensePostingQueries.observeAllPostings().asFlow().mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toPosting() } }

    override suspend fun getFixedExpensePostings(expenseId: Long): List<FixedExpensePosting> =
        withContext(Dispatchers.Default) {
            database.fixedExpensePostingQueries.getPostingsForExpense(expenseId).awaitAsList().map { it.toPosting() }
        }

    override suspend fun getAllFixedExpensePostings(): List<FixedExpensePosting> = withContext(Dispatchers.Default) {
        database.fixedExpensePostingQueries.observeAllPostings().awaitAsList().map { it.toPosting() }
    }

    override suspend fun insertFullFixedExpensePosting(posting: FixedExpensePosting) = withContext(Dispatchers.Default) {
        require(posting.operationId.isNotBlank() && posting.expenseId > 0 && posting.transactionId > 0)
        database.transaction {
            val existing = database.fixedExpensePostingQueries.getPostingByOperationId(posting.operationId).executeAsOneOrNull()
            if (existing != null) {
                require(existing.toPosting() == posting) { "Conflicting fixed expense posting receipt" }
            } else {
                require(queries.fixedExpenseExistsIncludingDeleted(posting.expenseId).executeAsOne() > 0L) {
                    "Missing fixed expense for posting receipt"
                }
                database.fixedExpensePostingQueries.insertPosting(posting.operationId, posting.expenseId,
                    posting.periodDueDate, posting.transactionId, posting.postedAt, if (posting.isRepeat) 1L else 0L)
            }
        }
    }

    override suspend fun getModifiedFixedExpenses(): List<FixedExpense> = withContext(Dispatchers.Default) {
        queries.getModifiedFixedExpenses().awaitAsList().map { it.toFixedExpense() }
    }

    override suspend fun markFixedExpenseAsSynced(id: Long) {
        queries.markFixedExpenseAsSynced(id)
    }

    override suspend fun physicallyDeleteFixedExpense(id: Long) {
        queries.physicallyDeleteFixedExpense(id)
    }
}

private fun Fixed_expense_posting.toPosting() = FixedExpensePosting(
    operationId, expenseId, periodDueDate, transactionId, postedAt, isRepeat != 0L
)
