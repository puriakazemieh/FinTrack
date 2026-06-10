package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetWithProgress
import kotlinx.coroutines.flow.Flow

interface BudgetLocalDataSource {
    fun observeBudgetsWithProgress(): Flow<List<BudgetWithProgress>>
    suspend fun getBudgetByCategoryId(categoryId: Long): Budget?
    suspend fun addBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget): Int
    suspend fun deleteBudget(id: Long)
    suspend fun getSpentAmountByCategory(categoryId: Long, from: Long, to: Long): Long
}
