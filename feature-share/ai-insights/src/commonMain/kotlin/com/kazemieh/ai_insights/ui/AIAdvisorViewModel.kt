package com.kazemieh.ai_insights.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.TransactionFilterParams
import com.kazemieh.common.model.TransactionType
import com.kazemieh.domain.usecase.ObserveCategorySumsUseCase
import com.kazemieh.domain.repository.TransactionRepository
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassAmber
import androidx.compose.ui.graphics.toArgb
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AIAdvisorViewModel(
    private val observeCategorySumsUseCase: ObserveCategorySumsUseCase,
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
            val earliestDate = allTransactions.filter { it.syncStatus != 2 }.minOfOrNull { it.timeStamp }
            val activeDays = if (earliestDate != null) {
                val now = Clock.System.now().toEpochMilliseconds()
                val diff = now - earliestDate
                (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } else 0

            if (activeDays == 0) {
                _state.update { it.copy(isLoading = false, isEmpty = true, activeDays = 0) }
                return@launch
            }

            // Monthly Analysis: filter for last 30 days
            val now = Clock.System.now().toEpochMilliseconds()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            val filter = TransactionFilterParams(
                fromTimestamp = thirtyDaysAgo,
                toTimestamp = now
            )

            observeCategorySumsUseCase(filter)
                .take(1)
                .collect { sums ->
                    val income = sums.filter { it.type == TransactionType.INCOME }.sumOf { it.totalAmount }
                    val expense = sums.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalAmount }
                    
                    val potential = if (income > 0) {
                        ((income - expense).coerceAtLeast(0) * 100 / income).toInt()
                    } else 0
                    
                    val potentialAmount = (income - expense).coerceAtLeast(0)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEmpty = income == 0L && expense == 0L,
                            activeDays = activeDays,
                            savingPotentialPercentage = potential,
                            savingPotentialAmount = potentialAmount,
                            suggestions = getMockSuggestions()
                        )
                    }
                }
        }
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
