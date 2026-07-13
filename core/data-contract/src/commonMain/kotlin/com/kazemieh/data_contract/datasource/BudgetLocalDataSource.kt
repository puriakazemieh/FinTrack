package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetWithProgress
import kotlinx.coroutines.flow.Flow

interface BudgetLocalDataSource {
    fun observeBudgetsWithProgress(from: Long, to: Long): Flow<List<BudgetWithProgress>>
    suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress?
    suspend fun getBudgetByCategoryId(categoryId: Long): Budget?
    suspend fun addBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget): Int
    suspend fun deleteBudget(id: Long)
    suspend fun getSpentAmountByCategory(categoryId: Long, from: Long, to: Long): Long

    suspend fun getAllBudgets(): List<Budget>
    suspend fun insertFullBudget(budget: Budget)
    suspend fun getModifiedBudgets(): List<Budget>
    suspend fun markBudgetAsSynced(id: Long)
    suspend fun physicallyDeleteBudget(id: Long)
    suspend fun hasAnyBudgets(): Boolean
}
