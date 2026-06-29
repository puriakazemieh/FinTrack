package com.kazemieh.domain.repository

import com.kazemieh.common.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: Long): Goal?
    suspend fun addGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal): Int
    suspend fun deleteGoal(id: Long)
}
