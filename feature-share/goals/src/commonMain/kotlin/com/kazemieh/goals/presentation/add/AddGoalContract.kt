package com.kazemieh.goals.presentation.add

import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalBasket
import com.kazemieh.common.model.GoalCategory
import com.kazemieh.common.model.GoalPriority
import com.kazemieh.common.model.GoalTemplate
import com.kazemieh.common.model.GoalType
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
    val type: GoalType = GoalType.SAVINGS,
    val category: GoalCategory = GoalCategory.SHORT_TERM,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val description: String = "",
    val templateId: Long? = null,
    val basketId: Long? = null,
    val templates: List<GoalTemplate> = emptyList(),
    val baskets: List<GoalBasket> = emptyList(),
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
    data class UpdateType(val type: GoalType) : AddGoalIntent
    data class UpdateCategory(val category: GoalCategory) : AddGoalIntent
    data class UpdatePriority(val priority: GoalPriority) : AddGoalIntent
    data class UpdateDescription(val description: String) : AddGoalIntent
    data class UpdateTemplateId(val id: Long?) : AddGoalIntent
    data class UpdateBasketId(val id: Long?) : AddGoalIntent
    
    // Management
    data class AddTemplate(val template: GoalTemplate) : AddGoalIntent
    data class DeleteTemplate(val id: Long) : AddGoalIntent
    data class AddBasket(val basket: GoalBasket) : AddGoalIntent
    data class DeleteBasket(val id: Long) : AddGoalIntent
    
    data object SaveGoal : AddGoalIntent
}

sealed interface AddGoalEffect {
    data object GoalSaved : AddGoalEffect
}
