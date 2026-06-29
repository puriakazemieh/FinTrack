package com.kazemieh.goals.presentation.add

import com.kazemieh.common.model.Goal

data class AddGoalState(
    val id: Long = 0,
    val name: String = "",
    val targetAmount: String = "",
    val savedAmount: String = "0",
    val iconId: Int = 1,
    val colorId: Int = 1,
    val endDate: String = "",
    val monthlyTarget: String = "0",
    val isLoading: Boolean = false
)

sealed interface AddGoalIntent {
    data class InitialData(val goal: Goal?) : AddGoalIntent
    data class UpdateName(val name: String) : AddGoalIntent
    data class UpdateTargetAmount(val amount: String) : AddGoalIntent
    data class UpdateSavedAmount(val amount: String) : AddGoalIntent
    data class UpdateEndDate(val date: String) : AddGoalIntent
    data class UpdateMonthlyTarget(val amount: String) : AddGoalIntent
    data class UpdateColorIcon(val colorId: Int, val iconId: Int) : AddGoalIntent
    data object SaveGoal : AddGoalIntent
}

sealed interface AddGoalEffect {
    data object GoalSaved : AddGoalEffect
}
