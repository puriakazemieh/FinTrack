package com.kazemieh.data_contract.datasource

import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalBasket
import com.kazemieh.common.model.GoalTemplate
import kotlinx.coroutines.flow.Flow

interface GoalLocalDataSource {
    fun observeGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: Long): Goal?
    suspend fun addGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal): Int
    suspend fun deleteGoal(id: Long)

    fun observeGoalTemplates(): Flow<List<GoalTemplate>>
    suspend fun addGoalTemplate(template: GoalTemplate): Long
    suspend fun updateGoalTemplate(template: GoalTemplate): Int
    suspend fun deleteGoalTemplate(id: Long)

    fun observeGoalBaskets(): Flow<List<GoalBasket>>
    suspend fun addGoalBasket(basket: GoalBasket): Long
    suspend fun updateGoalBasket(basket: GoalBasket): Int
    suspend fun deleteGoalBasket(id: Long)

    suspend fun getAllGoals(): List<Goal>
    suspend fun insertFullGoal(goal: Goal)
    suspend fun getModifiedGoals(): List<Goal>
    suspend fun markGoalAsSynced(id: Long)
    suspend fun physicallyDeleteGoal(id: Long)
}
