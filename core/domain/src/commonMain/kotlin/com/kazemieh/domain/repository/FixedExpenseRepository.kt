package com.kazemieh.domain.repository

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.FixedExpensePosting
import kotlinx.coroutines.flow.Flow

interface FixedExpenseRepository {
    suspend fun getFixedExpensePostings(expenseId: Long): List<FixedExpensePosting>
    suspend fun insertFixedExpense(expense: FixedExpense): Long
    suspend fun updateFixedExpense(expense: FixedExpense)
    suspend fun deleteFixedExpense(id: Long)
    suspend fun getFixedExpenseById(id: Long): FixedExpense?
    fun observeAllFixedExpenses(): Flow<List<FixedExpense>>
    fun observeFixedExpensesFiltered(
        query: String?,
        categoryIds: List<Long>,
        sourceIds: List<Long>,
        tagIds: List<Long>,
        personIds: List<Long>
    ): Flow<List<FixedExpense>>

    suspend fun updateNextDueDate(id: Long, nextDueDate: Long)
}
