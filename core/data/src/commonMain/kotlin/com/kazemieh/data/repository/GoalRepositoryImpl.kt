package com.kazemieh.data.repository

import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalBasket
import com.kazemieh.common.model.GoalTemplate
import com.kazemieh.data_contract.datasource.GoalLocalDataSource
import com.kazemieh.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

class GoalRepositoryImpl(
    private val localDataSource: GoalLocalDataSource
) : GoalRepository {
    override fun observeGoals(): Flow<List<Goal>> {
        return localDataSource.observeGoals()
    }

    override suspend fun getGoalById(id: Long): Goal? {
        return localDataSource.getGoalById(id)
    }

    override suspend fun addGoal(goal: Goal): Long {
        return localDataSource.addGoal(goal)
    }

    override suspend fun updateGoal(goal: Goal): Int {
        return localDataSource.updateGoal(goal)
    }

    override suspend fun deleteGoal(id: Long) {
        localDataSource.deleteGoal(id)
    }

    override fun observeGoalTemplates(): Flow<List<GoalTemplate>> {
        return localDataSource.observeGoalTemplates()
    }

    override suspend fun addGoalTemplate(template: GoalTemplate): Long {
        return localDataSource.addGoalTemplate(template)
    }

    override suspend fun updateGoalTemplate(template: GoalTemplate): Int {
        return localDataSource.updateGoalTemplate(template)
    }

    override suspend fun deleteGoalTemplate(id: Long) {
        localDataSource.deleteGoalTemplate(id)
    }

    override fun observeGoalBaskets(): Flow<List<GoalBasket>> {
        return localDataSource.observeGoalBaskets()
    }

    override suspend fun addGoalBasket(basket: GoalBasket): Long {
        return localDataSource.addGoalBasket(basket)
    }

    override suspend fun updateGoalBasket(basket: GoalBasket): Int {
        return localDataSource.updateGoalBasket(basket)
    }

    override suspend fun deleteGoalBasket(id: Long) {
        localDataSource.deleteGoalBasket(id)
    }
}
