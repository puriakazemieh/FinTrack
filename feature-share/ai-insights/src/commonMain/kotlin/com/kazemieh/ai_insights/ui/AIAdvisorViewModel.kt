package com.kazemieh.ai_insights.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.usecase.DetectSubscriptionsUseCase
import com.kazemieh.domain.usecase.ObserveCategorySumsUseCase
import com.kazemieh.domain.usecase.ObserveSpendingPatternUseCase
import com.kazemieh.domain.repository.TransactionRepository
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassAmber
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AIAdvisorViewModel(
    private val observeSpendingPatternUseCase: ObserveSpendingPatternUseCase,
    private val transactionRepository: TransactionRepository,
    private val detectSubscriptionsUseCase: DetectSubscriptionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AIAdvisorState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AIAdvisorEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadData()
    }

    fun onIntent(intent: AIAdvisorIntent) {
        when (intent) {
            AIAdvisorIntent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Calculate active days
            val allTransactions = transactionRepository.getAllTransactions()
            val earliestDate = allTransactions.filter { it.syncStatus != SyncStatus.DELETED }.minOfOrNull { it.timeStamp }
            val activeDays = if (earliestDate != null) {
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                val diff = now - earliestDate
                (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } else 0

            if (activeDays == 0) {
                _state.update { it.copy(isLoading = false, isEmpty = true, activeDays = 0) }
                return@launch
            }

            val subscriptions = detectSubscriptionsUseCase()

            observeSpendingPatternUseCase()
                .take(1)
                .collect { pattern ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEmpty = pattern.totalIncome == 0L && pattern.totalExpense == 0L,
                            activeDays = activeDays,
                            savingPotentialPercentage = pattern.savingPotentialPercentage,
                            savingPotentialAmount = pattern.savingPotentialAmount,
                            monthIncome = pattern.totalIncome,
                            monthExpense = pattern.totalExpense,
                            topExpenseCategoryName = pattern.topExpenseCategoryName,
                            topExpenseAmount = pattern.topExpenseAmount,
                            suggestions = generateDynamicSuggestions(pattern),
                            subscriptions = subscriptions
                        )
                    }
                }
        }
    }

    private fun generateDynamicSuggestions(pattern: com.kazemieh.domain.usecase.SpendingPattern): List<InvestmentSuggestion> {
        val suggestions = mutableListOf<InvestmentSuggestion>()

        // 1. Overspending — the most urgent signal: last 30 days spent more than earned.
        if (pattern.isOverspending && pattern.totalExpense > 0) {
            suggestions.add(
                InvestmentSuggestion(
                    title = Res.string.ai_suggestion_overspend_title,
                    body = Res.string.ai_suggestion_overspend_body,
                    detail = Res.string.ai_suggestion_overspend_detail,
                    icon = Res.drawable.ic_cat_shopping,
                    colorHex = 0xFFEF4444, // GlassRed
                    risk = RiskLevel.High,
                    returnRate = Res.string.label_zero
                )
            )
        }

        // 2. Budgeting suggestion based on top category growth.
        if (pattern.growthPercentage > 10 && pattern.growthCategoryName != null) {
            suggestions.add(
                InvestmentSuggestion(
                    title = Res.string.ai_suggestion_budget_title,
                    titleArgs = listOf(pattern.growthCategoryName!!),
                    body = Res.string.ai_suggestion_budget_body,
                    bodyArgs = listOf(pattern.growthCategoryName!!, pattern.growthPercentage.toString()),
                    detail = Res.string.ai_suggestion_gold_detail, // Fallback detail
                    icon = Res.drawable.ic_cat_shopping,
                    colorHex = 0xFFEF4444, // GlassRed
                    risk = RiskLevel.Low,
                    returnRate = Res.string.label_zero // Not applicable
                )
            )
        }

        // 3. Concentration — one category eats a large share of all spending.
        if (pattern.topExpenseSharePercentage >= 40 && pattern.topExpenseCategoryName != null) {
            suggestions.add(
                InvestmentSuggestion(
                    title = Res.string.ai_suggestion_concentration_title,
                    titleArgs = listOf(pattern.topExpenseCategoryName!!),
                    body = Res.string.ai_suggestion_concentration_body,
                    bodyArgs = listOf(pattern.topExpenseCategoryName!!, pattern.topExpenseSharePercentage.toString()),
                    detail = Res.string.ai_suggestion_concentration_detail,
                    icon = Res.drawable.ic_cat_shopping,
                    colorHex = 0xFFF59E0B, // GlassAmber
                    risk = RiskLevel.Medium,
                    returnRate = Res.string.label_zero
                )
            )
        }

        // 4. High potential saving -> Investment.
        if (pattern.savingPotentialPercentage > 15) {
            suggestions.add(
                InvestmentSuggestion(
                    title = Res.string.ai_suggestion_gold_title,
                    body = Res.string.ai_suggestion_gold_body,
                    detail = Res.string.ai_suggestion_gold_detail,
                    icon = Res.drawable.ic_cat_investment,
                    colorHex = 0xFFF59E0B, // GlassAmber
                    risk = RiskLevel.Low,
                    returnRate = Res.string.ai_suggestion_gold_return
                )
            )
        }

        // 5. Healthy but moderate saving (5–15%) -> build an emergency fund.
        if (pattern.savingPotentialPercentage in 5..15) {
            suggestions.add(
                InvestmentSuggestion(
                    title = Res.string.ai_suggestion_emergency_title,
                    body = Res.string.ai_suggestion_emergency_body,
                    detail = Res.string.ai_suggestion_emergency_detail,
                    icon = Res.drawable.ic_cat_bank,
                    colorHex = 0xFF22C55E, // GlassGreen
                    risk = RiskLevel.Low,
                    returnRate = Res.string.ai_suggestion_fund_return
                )
            )
        }

        // 6. Low potential saving -> low-risk bank deposit / income focus.
        if (pattern.savingPotentialPercentage < 5 && !pattern.isOverspending) {
             suggestions.add(
                InvestmentSuggestion(
                    title = Res.string.ai_suggestion_bank_title,
                    body = Res.string.ai_suggestion_bank_body,
                    detail = Res.string.ai_suggestion_bank_detail,
                    icon = Res.drawable.ic_cat_bank,
                    colorHex = 0xFF22C55E, // GlassGreen
                    risk = RiskLevel.Low,
                    returnRate = Res.string.ai_suggestion_bank_return
                )
            )
        }

        // No fabricated fallback: when no rule fires we return an empty list so the
        // screen shows an honest "no suggestions yet" state instead of demo data.
        return suggestions
    }
}
