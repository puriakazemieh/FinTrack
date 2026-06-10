package com.kazemieh.domain.repository

import com.kazemieh.common.model.FixedExpense
import kotlinx.coroutines.flow.Flow

interface FixedExpenseRepository {
    suspend fun insertFixedExpense(expense: FixedExpense): Long
    suspend fun updateFixedExpense(expense: FixedExpense)
    suspend fun deleteFixedExpense(id: Long)
    suspend fun getFixedExpenseById(id: Long): FixedExpense?
    fun observeAllFixedExpenses(): Flow<List<FixedExpense>>
    suspend fun updateNextDueDate(id: Long, nextDueDate: Long)
}
