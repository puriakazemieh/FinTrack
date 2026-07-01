package com.kazemieh.ai_insights.ui

import androidx.compose.runtime.Immutable
import com.kazemieh.common.model.TransactionType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Immutable
data class AIAdvisorState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val savingPotentialPercentage: Int = 12,
    val savingPotentialAmount: Long = 3000000,
    val activeDays: Int = 87,
    val suggestions: List<InvestmentSuggestion> = emptyList()
)

sealed interface AIAdvisorIntent {
    data object Refresh : AIAdvisorIntent
}

sealed interface AIAdvisorEffect {
    data class ShowError(val message: String) : AIAdvisorEffect
}

data class InvestmentSuggestion(
    val title: StringResource,
    val titleArgs: List<String> = emptyList(),
    val body: StringResource,
    val bodyArgs: List<String> = emptyList(),
    val detail: StringResource,
    val icon: DrawableResource,
    val colorHex: Long,
    val risk: RiskLevel,
    val returnRate: StringResource
)

enum class RiskLevel {
    Low, Medium, High
}
