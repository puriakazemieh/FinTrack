package com.kazemieh.ai_insights.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.SyncStatus
import com.kazemieh.domain.repository.AiConfig
import com.kazemieh.domain.repository.TransactionRepository
import com.kazemieh.domain.usecase.AiConfigUseCase
import com.kazemieh.domain.usecase.DetectSubscriptionsUseCase
import com.kazemieh.domain.usecase.GenerateAiInsightUseCase
import com.kazemieh.domain.usecase.GetFinancialSummaryUseCase
import com.kazemieh.domain.usecase.ObserveSpendingPatternUseCase
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.ai_suggestion_bank_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_bank_detail
import fintrack.core.designsystem.generated.resources.ai_suggestion_bank_return
import fintrack.core.designsystem.generated.resources.ai_suggestion_bank_title
import fintrack.core.designsystem.generated.resources.ai_suggestion_budget_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_budget_title
import fintrack.core.designsystem.generated.resources.ai_suggestion_concentration_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_concentration_detail
import fintrack.core.designsystem.generated.resources.ai_suggestion_concentration_title
import fintrack.core.designsystem.generated.resources.ai_suggestion_emergency_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_emergency_detail
import fintrack.core.designsystem.generated.resources.ai_suggestion_emergency_title
import fintrack.core.designsystem.generated.resources.ai_suggestion_fund_return
import fintrack.core.designsystem.generated.resources.ai_suggestion_gold_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_gold_detail
import fintrack.core.designsystem.generated.resources.ai_suggestion_gold_return
import fintrack.core.designsystem.generated.resources.ai_suggestion_gold_title
import fintrack.core.designsystem.generated.resources.ai_suggestion_overspend_body
import fintrack.core.designsystem.generated.resources.ai_suggestion_overspend_detail
import fintrack.core.designsystem.generated.resources.ai_suggestion_overspend_title
import fintrack.core.designsystem.generated.resources.ic_cat_bank
import fintrack.core.designsystem.generated.resources.ic_cat_investment
import fintrack.core.designsystem.generated.resources.ic_cat_shopping
import fintrack.core.designsystem.generated.resources.label_zero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AIAdvisorViewModel(
    private val observeSpendingPatternUseCase: ObserveSpendingPatternUseCase,
    private val transactionRepository: TransactionRepository,
    private val detectSubscriptionsUseCase: DetectSubscriptionsUseCase,
    private val generateAiInsightUseCase: GenerateAiInsightUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase,
    private val aiConfigUseCase: AiConfigUseCase,
    private val analytics: com.kazemieh.common.analytics.AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(AIAdvisorState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AIAdvisorEffect>()
    val effect = _effect.asSharedFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.AiAdvisorOpened)
        _state.update { it.copy(aiConfig = aiConfigUseCase.get()) }
        loadData()
    }

    fun onIntent(intent: AIAdvisorIntent) {
        when (intent) {
            AIAdvisorIntent.Refresh -> loadData()
            AIAdvisorIntent.ToggleAiSettings -> _state.update { it.copy(showAiSettings = !it.showAiSettings) }
            is AIAdvisorIntent.SaveAiConfig -> saveAiConfig(intent.config)
        }
    }

    private fun saveAiConfig(config: AiConfig) {
        aiConfigUseCase.save(config)
        _state.update { it.copy(aiConfig = config, showAiSettings = false) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Calculate active days
            val allTransactions = transactionRepository.getAllTransactions()
            val earliestDate = allTransactions.filter { it.syncStatus != com.kazemieh.common.model.SyncStatus.DELETED }
                .minOfOrNull { it.timeStamp }
            val activeDays = if (earliestDate != null) {
                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
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
                            subscriptions = subscriptions,
                            cloudEnabled = generateAiInsightUseCase.isEnabled()
                        )
                    }
                    loadCloudInsight(pattern)
                }
        }
    }

    private fun loadCloudInsight(pattern: com.kazemieh.domain.usecase.SpendingPattern) {
        if (!generateAiInsightUseCase.isEnabled()) return
        viewModelScope.launch {
            _state.update { it.copy(cloudInsightLoading = true, cloudInsight = null) }
            val summary = getFinancialSummaryUseCase()
            val insight = generateAiInsightUseCase(buildInsightContext(pattern, summary))
            if (insight.isNotBlank()) {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.AiInsightGenerated)
            }
            _state.update { it.copy(cloudInsight = insight, cloudInsightLoading = false) }
        }
    }

    private fun buildInsightContext(
        pattern: com.kazemieh.domain.usecase.SpendingPattern,
        summary: com.kazemieh.domain.usecase.FinancialSummary
    ): String {
        return buildString {
            append("--- خلاصه وضعیت مالی حرفه‌ای ---")

            append("\n[نقدینگی و دارایی]")
            append("\nموجودی نقد کل: ${summary.totalBalance} تومان")
            append("\nارزش کل دارایی‌های سرمایه‌ای: ${summary.totalAssets} تومان")
            if (summary.assetBreakdown.isNotEmpty()) {
                append("\nتفکیک دارایی‌ها: ${summary.assetBreakdown.entries.joinToString { "${it.key}: ${it.value}" }}")
            }

            append("\n\n[تعهدات و بدهی]")
            append("\nمجموع بدهی‌های پرداخت نشده: ${summary.totalDebt} تومان")
            if (summary.upcomingInstallments.isNotEmpty()) {
                append("\nاقساط سررسید نزدیک (۱۴ روز): ${summary.upcomingInstallments.joinToString { "${it.first}: ${it.second}" }}")
            }
            if (summary.upcomingFixedExpenses.isNotEmpty()) {
                append("\nهزینه‌های ثابت نزدیک: ${summary.upcomingFixedExpenses.joinToString { "${it.first}: ${it.second}" }}")
            }

            append("\n\n[بودجه‌بندی]")
            append("\nتعداد بودجه‌های فعال: ${summary.activeBudgetsCount}")
            append("\nتعداد بودجه‌های فراتر از حد: ${summary.exceededBudgetsCount}")

            append("\n\n[عملکرد ۳۰ روز اخیر و روند]")
            append("\nدرآمد این ماه: ${summary.monthlyIncome} (ماه قبل: ${summary.prevMonthlyIncome})")
            append("\nهزینه این ماه: ${summary.monthlyExpense} (ماه قبل: ${summary.prevMonthlyExpense})")
            append("\nدرصد پس‌انداز بالقوه: ${pattern.savingPotentialPercentage}٪")
            if (summary.topExpenseCategories.isNotEmpty()) {
                append("\nسه دسته پرهزینه: ${summary.topExpenseCategories.joinToString { "${it.first}: ${it.second}" }}")
            }

            if (summary.goalProgress.isNotEmpty()) {
                append("\n\n[اهداف مالی]")
                append("\nپیشرفت اهداف: ${summary.goalProgress.joinToString { "${it.first}: ${it.second}%" }}")
            }

            append("\n\n--- دستورالعمل تحلیل ---")
            append("\nبه عنوان یک مشاور مالی خبره، این داده‌های جامع را تحلیل کن.")
            append("\n1. روند درآمد و هزینه را با ماه قبل مقایسه کن و نکات مثبت یا منفی را بگو.")
            append("\n2. اگر نقدینگی نسبت به بدهی‌ها یا هزینه‌های سررسید نزدیک کم است، هشدار بده.")
            append("\n3. به اهداف مالی کاربر توجه کن و برای رسیدن سریع‌تر به آن‌ها راهکار بده.")
            append("\n4. اگر بودجه‌ای شکسته شده، برای مدیریت آن دسته خاص توصیه عملی بده.")
            append("\nتحلیل باید حرفه‌ای، دقیق، بدون کلیشه‌ و حداکثر در ۶ جمله باشد.")
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
                    bodyArgs = listOf(
                        pattern.growthCategoryName!!,
                        pattern.growthPercentage.toString()
                    ),
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
                    bodyArgs = listOf(
                        pattern.topExpenseCategoryName!!,
                        pattern.topExpenseSharePercentage.toString()
                    ),
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
