package com.kazemieh.data.repository

import com.kazemieh.common.model.Goal
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
}
