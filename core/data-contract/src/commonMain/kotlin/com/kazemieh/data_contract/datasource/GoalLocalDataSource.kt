package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalLocalDataSource {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: Long): Goal?
    suspend fun addGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal): Int
    suspend fun deleteGoal(id: Long)

    suspend fun getAllGoals(): List<Goal>
    suspend fun insertFullGoal(goal: Goal)
    suspend fun getModifiedGoals(): List<Goal>
    suspend fun markGoalAsSynced(id: Long)
    suspend fun physicallyDeleteGoal(id: Long)
}
