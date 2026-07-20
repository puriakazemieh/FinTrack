package com.kazemieh.goals.presentation

import com.kazemieh.common.model.FreedomStage
import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalBasket
import com.kazemieh.common.model.GoalTemplate

enum class GoalTab {
    GOALS,
    ROADMAP,
    BASKET
}

data class BasketData(
    val id: Long = 0,
    val name: String = "",
    val amount: Long = 0,
    val percent: Int = 0,
    val colorId: Int = 1
)

data class GoalState(
    val goals: List<Goal> = emptyList(),
    val templates: List<GoalTemplate> = emptyList(),
    val baskets: List<GoalBasket> = emptyList(),
    val basketItems: List<BasketData> = emptyList(),
    val currentTab: GoalTab = GoalTab.GOALS,
    val freedomStage: FreedomStage = FreedomStage.DEPENDENCE,
    val isLoading: Boolean = false,
    val totalSavedAmount: Long = 0,
    val totalTargetAmount: Long = 0,
    val isAddGoalShow: Boolean = false,
    val selectedGoal: Goal? = null,
    val searchQuery: String = "",
    val isRoundUpEnabled: Boolean = false,
    val roundUpGoalId: Long? = null,
    val roundUpUnit: Long = 5000L,
    val showRoundUpSettings: Boolean = false
) {
    val totalProgress: Float
        get() = if (totalTargetAmount > 0) totalSavedAmount.toFloat() / totalTargetAmount.toFloat() else 0f
    
    val totalPercent: Int
        get() = (totalProgress * 100).toInt()
}

sealed interface GoalIntent {
    data object LoadGoals : GoalIntent
    data class SelectTab(val tab: GoalTab) : GoalIntent
    data class UpdateSearchQuery(val query: String) : GoalIntent
    data class DeleteGoal(val id: Long) : GoalIntent
    data class ShowAddGoal(val goal: Goal? = null) : GoalIntent
    data class AddAmountToGoal(val id: Long, val amount: Long) : GoalIntent
    data object ToggleRoundUpSettings : GoalIntent
    data object ToggleRoundUpEnabled : GoalIntent
    data class SetRoundUpGoal(val goalId: Long) : GoalIntent
    data class SetRoundUpUnit(val unit: Long) : GoalIntent

    // Template management
    data class AddTemplate(val template: GoalTemplate) : GoalIntent
    data class UpdateTemplate(val template: GoalTemplate) : GoalIntent
    data class DeleteTemplate(val id: Long) : GoalIntent

    // Basket management
    data class AddBasket(val basket: GoalBasket) : GoalIntent
    data class UpdateBasket(val basket: GoalBasket) : GoalIntent
    data class DeleteBasket(val id: Long) : GoalIntent
}

sealed interface GoalEffect {
    data class ShowError(val message: String) : GoalEffect
    data object GoalAdded : GoalEffect
}
