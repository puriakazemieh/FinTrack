package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Goal
import com.kazemieh.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

class ObserveGoalsUseCase(private val repository: GoalRepository) {
    operator fun invoke(): Flow<List<Goal>> = repository.observeGoals()
}

class AddGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: Goal) = repository.addGoal(goal)
}

class UpdateGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(goal: Goal) = repository.updateGoal(goal)
}

class DeleteGoalUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteGoal(id)
}

class GetGoalByIdUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(id: Long) = repository.getGoalById(id)
}

data class GoalUseCases(
    val observeGoals: ObserveGoalsUseCase,
    val addGoal: AddGoalUseCase,
    val updateGoal: UpdateGoalUseCase,
    val deleteGoal: DeleteGoalUseCase,
    val getGoalById: GetGoalByIdUseCase
)
