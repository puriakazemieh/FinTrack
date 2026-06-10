package com.kazemieh.database.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.data_contract.datasource.FixedExpenseLocalDataSource
import com.kazemieh.database.FinTrackDatabase
import com.kazemieh.database.mapper.toFixedExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FixedExpenseLocalDataSourceImpl(
    private val database: FinTrackDatabase
) : FixedExpenseLocalDataSource {
    private val queries = database.fixedExpenseQueries

    override suspend fun insertFixedExpense(expense: FixedExpense): Long {
        queries.insertFixedExpense(
            amount = expense.amount,
            categoryId = expense.categoryId,
            sourceId = expense.sourceId,
            description = expense.description,
            recurrence = expense.recurrence.name,
            startDate = expense.startDate,
            nextDueDate = expense.nextDueDate,
            isAutoPostEnabled = if (expense.isAutoPostEnabled) 1L else 0L,
            isActive = if (expense.isActive) 1L else 0L
        )
        return queries.lastInsertRowId().executeAsOne()
    }

    override suspend fun updateFixedExpense(expense: FixedExpense) {
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
            id = expense.id
        )
    }

    override suspend fun deleteFixedExpense(id: Long) {
        queries.deleteFixedExpense(id)
    }

    override suspend fun getFixedExpenseById(id: Long): FixedExpense? {
        return queries.getFixedExpenseById(id).executeAsOneOrNull()?.toFixedExpense()
    }

    override fun observeAllFixedExpenses(): Flow<List<FixedExpense>> {
        return queries.observeAllFixedExpenses().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toFixedExpense() }
        }
    }

    override suspend fun updateNextDueDate(id: Long, nextDueDate: Long) {
        queries.updateNextDueDate(nextDueDate, id)
    }
}
