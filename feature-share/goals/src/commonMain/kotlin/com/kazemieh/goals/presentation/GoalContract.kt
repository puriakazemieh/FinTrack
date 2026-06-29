package com.kazemieh.goals.presentation

import com.kazemieh.common.model.Goal

data class GoalState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = false,
    val totalSavedAmount: Long = 0,
    val totalTargetAmount: Long = 0,
    val isAddGoalShow: Boolean = false,
    val selectedGoal: Goal? = null,
    val searchQuery: String = ""
) {
    val totalProgress: Float
        get() = if (totalTargetAmount > 0) totalSavedAmount.toFloat() / totalTargetAmount.toFloat() else 0f
    
    val totalPercent: Int
        get() = (totalProgress * 100).toInt()
}

sealed interface GoalIntent {
    data object LoadGoals : GoalIntent
    data class UpdateSearchQuery(val query: String) : GoalIntent
    data class DeleteGoal(val id: Long) : GoalIntent
    data class ShowAddGoal(val goal: Goal? = null) : GoalIntent
    data class AddAmountToGoal(val id: Long, val amount: Long) : GoalIntent
}

sealed interface GoalEffect {
    data class ShowError(val message: String) : GoalEffect
    data object GoalAdded : GoalEffect
}
