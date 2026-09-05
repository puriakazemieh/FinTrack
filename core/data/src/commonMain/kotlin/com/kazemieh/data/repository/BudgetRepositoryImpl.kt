package com.kazemieh.data.repository

import com.kazemieh.common.model.Budget
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.data_contract.datasource.BudgetLocalDataSource
import com.kazemieh.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class BudgetRepositoryImpl(
    private val localDataSource: BudgetLocalDataSource
,
    private val preferenceRepository: com.kazemieh.domain.repository.PreferenceRepository
) : BudgetRepository {
    override fun observeBudgetsWithProgress(from: Long, to: Long): Flow<List<BudgetWithProgress>> {
        return localDataSource.observeBudgetsWithProgress(from, to)
    }

    override suspend fun getBudgetWithProgressByCategory(categoryId: Long): BudgetWithProgress? {
        return localDataSource.getBudgetWithProgressByCategory(categoryId)
    }

    override suspend fun getBudgetByCategoryId(categoryId: Long): Budget? {
        return localDataSource.getBudgetByCategoryId(categoryId)
    }

    override suspend fun addBudget(budget: Budget): Long {
        val code = preferenceRepository.getString("PREF_CURRENCY", "IRT")
        return localDataSource.addBudget(budget.copy(currencyCode = code))
    }

    override suspend fun updateBudget(budget: Budget): Int {
        val code = preferenceRepository.getString("PREF_CURRENCY", "IRT")
        return localDataSource.updateBudget(budget.copy(currencyCode = code))
    }

    override suspend fun deleteBudget(id: Long) {
        localDataSource.deleteBudget(id)
    }

    override suspend fun getSpentAmountByCategory(categoryId: Long, from: Long, to: Long): Long {
        return localDataSource.getSpentAmountByCategory(categoryId, from, to)
    }

    override suspend fun hasAnyBudgets(): Boolean {
        return localDataSource.hasAnyBudgets()
    }
}
