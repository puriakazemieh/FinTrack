package com.kazemieh.domain.usecase

import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalBasket
import com.kazemieh.common.model.GoalTemplate
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

// Template UseCases
class ObserveGoalTemplatesUseCase(private val repository: GoalRepository) {
    operator fun invoke(): Flow<List<GoalTemplate>> = repository.observeGoalTemplates()
}

class AddGoalTemplateUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(template: GoalTemplate) = repository.addGoalTemplate(template)
}

class UpdateGoalTemplateUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(template: GoalTemplate) = repository.updateGoalTemplate(template)
}

class DeleteGoalTemplateUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteGoalTemplate(id)
}

// Basket UseCases
class ObserveGoalBasketsUseCase(private val repository: GoalRepository) {
    operator fun invoke(): Flow<List<GoalBasket>> = repository.observeGoalBaskets()
}

class AddGoalBasketUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(basket: GoalBasket) = repository.addGoalBasket(basket)
}

class UpdateGoalBasketUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(basket: GoalBasket) = repository.updateGoalBasket(basket)
}

class DeleteGoalBasketUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteGoalBasket(id)
}

data class GoalUseCases(
    val observeGoals: ObserveGoalsUseCase,
    val addGoal: AddGoalUseCase,
    val updateGoal: UpdateGoalUseCase,
    val deleteGoal: DeleteGoalUseCase,
    val getGoalById: GetGoalByIdUseCase,
    val calculateFreedomStage: CalculateFreedomStageUseCase,
    // Templates
    val observeGoalTemplates: ObserveGoalTemplatesUseCase,
    val addGoalTemplate: AddGoalTemplateUseCase,
    val updateGoalTemplate: UpdateGoalTemplateUseCase,
    val deleteGoalTemplate: DeleteGoalTemplateUseCase,
    // Baskets
    val observeGoalBaskets: ObserveGoalBasketsUseCase,
    val addGoalBasket: AddGoalBasketUseCase,
    val updateGoalBasket: UpdateGoalBasketUseCase,
    val deleteGoalBasket: DeleteGoalBasketUseCase
)
