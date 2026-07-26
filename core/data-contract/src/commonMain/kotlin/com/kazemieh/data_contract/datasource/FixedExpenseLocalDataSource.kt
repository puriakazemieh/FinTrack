package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.FixedExpense
import kotlinx.coroutines.flow.Flow

interface FixedExpenseLocalDataSource {
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

    suspend fun getAllFixedExpenses(): List<FixedExpense>
    suspend fun insertFullFixedExpense(expense: FixedExpense)
    suspend fun getModifiedFixedExpenses(): List<FixedExpense>
    suspend fun markFixedExpenseAsSynced(id: Long)
    suspend fun physicallyDeleteFixedExpense(id: Long)
}
