package com.kazemieh.ai_insights.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
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
    private val transactionRepository: TransactionRepository
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
                            suggestions = generateDynamicSuggestions(pattern)
                        )
                    }
                }
        }
    }

    private fun generateDynamicSuggestions(pattern: com.kazemieh.domain.usecase.SpendingPattern): List<InvestmentSuggestion> {
        val suggestions = mutableListOf<InvestmentSuggestion>()

        // 1. Budgeting suggestion based on top category growth
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

        // 2. High potential saving -> Investment
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

        // 3. Low potential saving -> Income increase or debt reduction
        if (pattern.savingPotentialPercentage < 5) {
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

        // Add one fallback if empty
        if (suggestions.isEmpty()) {
            suggestions.addAll(getMockSuggestions())
        }

        return suggestions
    }

    private fun getMockSuggestions() = listOf(
        InvestmentSuggestion(
            title = Res.string.ai_suggestion_gold_title,
            body = Res.string.ai_suggestion_gold_body,
            detail = Res.string.ai_suggestion_gold_detail,
            icon = Res.drawable.ic_cat_investment,
            colorHex = 0xFFF59E0B, // GlassAmber
            risk = RiskLevel.Low,
            returnRate = Res.string.ai_suggestion_gold_return
        ),
        InvestmentSuggestion(
            title = Res.string.ai_suggestion_bank_title,
            body = Res.string.ai_suggestion_bank_body,
            detail = Res.string.ai_suggestion_bank_detail,
            icon = Res.drawable.ic_cat_bank,
            colorHex = 0xFF22C55E, // GlassGreen
            risk = RiskLevel.Low,
            returnRate = Res.string.ai_suggestion_bank_return
        ),
        InvestmentSuggestion(
            title = Res.string.ai_suggestion_fund_title,
            body = Res.string.ai_suggestion_fund_body,
            detail = Res.string.ai_suggestion_fund_detail,
            icon = Res.drawable.ic_93,
            colorHex = 0xFF60A5FA, // GlassBlue
            risk = RiskLevel.Medium,
            returnRate = Res.string.ai_suggestion_fund_return
        )
    )
}
