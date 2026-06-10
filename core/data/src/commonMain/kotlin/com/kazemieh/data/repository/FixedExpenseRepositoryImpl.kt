package com.kazemieh.data.repository

import com.kazemieh.common.model.FixedExpense
import com.kazemieh.data_contract.datasource.FixedExpenseLocalDataSource
import com.kazemieh.domain.repository.FixedExpenseRepository
import kotlinx.coroutines.flow.Flow

class FixedExpenseRepositoryImpl(
    private val localDataSource: FixedExpenseLocalDataSource
) : FixedExpenseRepository {
    override suspend fun insertFixedExpense(expense: FixedExpense): Long = localDataSource.insertFixedExpense(expense)
    override suspend fun updateFixedExpense(expense: FixedExpense) = localDataSource.updateFixedExpense(expense)
    override suspend fun deleteFixedExpense(id: Long) = localDataSource.deleteFixedExpense(id)
    override suspend fun getFixedExpenseById(id: Long): FixedExpense? = localDataSource.getFixedExpenseById(id)
    override fun observeAllFixedExpenses(): Flow<List<FixedExpense>> = localDataSource.observeAllFixedExpenses()
    override suspend fun updateNextDueDate(id: Long, nextDueDate: Long) = localDataSource.updateNextDueDate(id, nextDueDate)
}
