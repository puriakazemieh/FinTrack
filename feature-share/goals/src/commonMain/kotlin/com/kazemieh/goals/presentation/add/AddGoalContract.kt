package com.kazemieh.goals.presentation.add

import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.RecurrenceType

data class AddGoalState(
    val id: Long = 0,
    val name: String = "",
    val targetAmount: String = "",
    val savedAmount: String = "0",
    val startDate: Long = 0,
    val endDate: Long? = null,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val monthlyTarget: String = "0",
    val isLoading: Boolean = false
)

sealed interface AddGoalIntent {
    data class InitialData(val goal: Goal?) : AddGoalIntent
    data class UpdateName(val name: String) : AddGoalIntent
    data class UpdateTargetAmount(val amount: String) : AddGoalIntent
    data class UpdateSavedAmount(val amount: String) : AddGoalIntent
    data class UpdateStartDate(val date: Long) : AddGoalIntent
    data class UpdateEndDate(val date: Long?) : AddGoalIntent
    data class UpdateRecurrence(val recurrence: RecurrenceType) : AddGoalIntent
    data class UpdateMonthlyTarget(val amount: String) : AddGoalIntent
    data object SaveGoal : AddGoalIntent
}

sealed interface AddGoalEffect {
    data object GoalSaved : AddGoalEffect
}
