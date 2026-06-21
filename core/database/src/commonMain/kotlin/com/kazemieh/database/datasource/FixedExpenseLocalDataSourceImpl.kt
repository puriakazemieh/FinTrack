package com.kazemieh.database.datasource

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.data_contract.datasource.FixedExpenseLocalDataSource
import com.kazemieh.database.FinTrackDatabase
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

    override suspend fun insertFixedExpense(expense: FixedExpense): Long = withContext(Dispatchers.Default) {
        val now = Clock.System.now().toEpochMilliseconds()
        queries.insertFixedExpense(
            amount = expense.amount,
            categoryId = expense.categoryId,
            sourceId = expense.sourceId,
            description = expense.description,
            recurrence = expense.recurrence.name,
            startDate = expense.startDate,
            nextDueDate = expense.nextDueDate,
            isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
            isActive = if (expense.isActive) 1L else 0L,
            updatedAt = now,
            syncStatus = 1
        )
        queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateFixedExpense(expense: FixedExpense) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateFixedExpense(
                amount = expense.amount,
                categoryId = expense.categoryId,
                sourceId = expense.sourceId,
                description = expense.description,
                recurrence = expense.recurrence.name,
                startDate = expense.startDate,
                nextDueDate = expense.nextDueDate,
                isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
                isActive = if (expense.isActive) 1L else 0L,
                updatedAt = now,
                syncStatus = 1,
                id = expense.id
            )
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

    override suspend fun getAllFixedExpenses(): List<FixedExpense> = withContext(Dispatchers.Default) {
        queries.observeAllFixedExpenses().awaitAsList().map { it.toFixedExpense() }
    }

    override suspend fun insertFullFixedExpense(expense: FixedExpense) {
        withContext(Dispatchers.Default) {
            queries.insertFullFixedExpense(
                id = expense.id,
                amount = expense.amount,
                categoryId = expense.categoryId,
                sourceId = expense.sourceId,
                description = expense.description,
                recurrence = expense.recurrence.name,
                startDate = expense.startDate,
                nextDueDate = expense.nextDueDate,
                isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
                isActive = if (expense.isActive) 1L else 0L,
                updatedAt = expense.updatedAt,
                syncStatus = expense.syncStatus.value.toLong()
            )
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
