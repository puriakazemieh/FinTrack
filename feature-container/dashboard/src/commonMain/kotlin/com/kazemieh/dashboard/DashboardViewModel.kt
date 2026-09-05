package com.kazemieh.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.Streak
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.repository.GoalRepository
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.usecase.ObserveAchievementsUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesUseCase
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.domain.usecase.ObserveStreakUseCase
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.money.Currency
import com.kazemieh.preferences.FinTrackPreferences
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.placeholder_user_initial
import fintrack.core.designsystem.generated.resources.placeholder_user_name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString


class DashboardViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val preferenceUseCases: PreferenceUseCases,
    private val smsDraftRepository: SmsDraftRepository,
    private val observeCategories: ObserveCategoriesUseCase,
    private val observeSources: ObserveSourcesUseCase,
    private val observeAchievements: ObserveAchievementsUseCase,
    private val observeStreak: ObserveStreakUseCase,
    private val transactionUseCaseGroup: TransactionUseCaseGroup,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardViewed)
        observeUserName()
        observeSmsDrafts()
        loadCategories()
        loadSources()
        loadAchievements()
        loadStreak()
        observeWidgetLayout()
        observeDisabledTools()
        observeHideBalance()
        observeCurrency()
        observeMostUsedData()
    }

    private fun observeMostUsedData() {
        transactionUseCaseGroup.observeMostUsedCategoriesUseCase(null)
            .onEach { categories ->
                _state.update { it.copy(mostUsedCategories = categories) }
            }.launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedSourcesUseCase()
            .onEach { sources ->
                _state.update { it.copy(mostUsedSources = sources) }
            }.launchIn(viewModelScope)
    }

    private fun observeCurrency() {
        preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_CURRENCY, "")
            .onEach { currencyJson ->
                val currency = Currency.valueOf(currencyJson)
                _state.update { it.copy(currency = currency.code) }
            }.launchIn(viewModelScope)
    }

    private fun observeHideBalance() {
        preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_HIDE_BALANCE, "false")
            .onEach { hidden ->
                val isGlobalHidden = hidden.toBoolean()
                _state.update {
                    it.copy(
                        isGlobalBalanceHidden = isGlobalHidden,
                        isBalanceLocallyRevealed = false, // Reset local override when global changes
                        isBalanceVisible = !isGlobalHidden
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun observeWidgetLayout() {
        preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_DASHBOARD_WIDGETS, "")
            .onEach { csv ->
                _state.update { it.copy(dashboardWidgets = DashboardWidget.parse(csv)) }
            }.launchIn(viewModelScope)
    }

    private fun observeDisabledTools() {
        // todo disable feature toggle -> remove or change setStringPreference
        preferenceUseCases.setStringPreference(
            FinTrackPreferences.PREF_DISABLED_TOOLS,
            ToolFeature.serializeDisabled(
                ToolFeature.entries.toSet() -
                        setOf(
                            ToolFeature.SOURCES,
                            ToolFeature.CATEGORIES,
                            ToolFeature.PERSONS,
                            ToolFeature.TAGS,
                            ToolFeature.BUDGETS,
                            ToolFeature.FIXED_EXPENSE,
                            ToolFeature.SHOPPING,
                            ToolFeature.NOTES,
                            ToolFeature.INSTALLMENT,
                            ToolFeature.DEBT,
                            ToolFeature.CHECK,
                        )
            )
        )
        preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_DISABLED_TOOLS, "")
            .onEach { csv ->
                _state.update { it.copy(disabledTools = ToolFeature.parseDisabled(csv)) }
            }.launchIn(viewModelScope)
    }

    private fun loadStreak() {
        viewModelScope.launch {
            observeStreak().collect { streak ->
                _state.update { it.copy(streak = streak) }
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            observeAchievements().collect { achievements ->
                _state.update { it.copy(achievements = achievements) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            observeCategories().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadSources() {
        viewModelScope.launch {
            observeSources().collect { sources ->
                _state.update { it.copy(sources = sources) }
            }
        }
    }

    private fun observeSmsDrafts() {
        smsDraftRepository.observeUnusedSmsDrafts()
            .onEach { drafts ->
                drafts.forEach { draft ->
                    if (draft.sourceId == null && draft.bankName.isNotBlank()) {
                        val source = _state.value.sources.find {
                            it.name.contains(draft.bankName, ignoreCase = true) ||
                                    draft.bankName.contains(it.name, ignoreCase = true)
                        }
                        if (source != null) {
                            viewModelScope.launch {
                                smsDraftRepository.updateSmsDraft(draft.copy(sourceId = source.id))
                            }
                        }
                    }
                }
                _state.update { it.copy(smsDrafts = drafts) }
            }.launchIn(viewModelScope)
    }

    private fun observeUserName() {
        combine(
            preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_USER_NAME, ""),
            preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_USER_FAMILY, "")
        ) { name, family ->
            val fullName = listOf(name, family).filter { it.isNotBlank() }.joinToString(" ")
            val displayName = fullName.ifBlank { getString(Res.string.placeholder_user_name) }
            val displayInitial =
                fullName.ifBlank { getString(Res.string.placeholder_user_initial) }.first()
                    .toString()

            _state.update {
                it.copy(
                    userName = displayName,
                    userInitial = displayInitial
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.ShowTransactionBottomSheet -> {
                val isOpening = !_state.value.showAddTransaction
                if (isOpening) {
                    if (intent.transactionWithRelations == null) {
                        analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardQuickAddClicked)
                    } else {
                        analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardRecentTransactionClicked)
                    }
                }
                _state.update {
                    it.copy(
                        showAddTransaction = !_state.value.showAddTransaction,
                        transactionWithRelations = intent.transactionWithRelations,
                        initialTransactionType = intent.type,
                        smsDraft = intent.smsDraft
                    )
                }
            }

            is DashboardIntent.DeleteTransactionBottomSheet -> _state.update {
                it.copy(
                    showDeleteTransaction = !_state.value.showDeleteTransaction,
                    transactionWithRelations = intent.transactionWithRelations
                )
            }

            is DashboardIntent.ShowAddSource -> _state.update {
                it.copy(
                    showAddSource = !it.showAddSource,
                    selectedSource = intent.source
                )
            }

            is DashboardIntent.AnimationEnabled -> _state.update {
                it.copy(
                    enableAnimationChart = !_state.value.enableAnimationChart,
                    showAddTransaction = false,
                    transactionWithRelations = null,
                    smsDraft = null
                )
            }

            DashboardIntent.ToggleBalanceVisibility -> {
                // Only toggle the LOCAL reveal state — do NOT change the global preference.
                // This way, navigating away from the dashboard resets the reveal.
                val currentlyRevealed = _state.value.isBalanceLocallyRevealed
                val newRevealed = !currentlyRevealed
                _state.update {
                    it.copy(
                        isBalanceLocallyRevealed = newRevealed,
                        isBalanceVisible = if (_state.value.isGlobalBalanceHidden) newRevealed else true
                    )
                }
                if (newRevealed) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardWalletSummaryViewed)
                }
            }

            DashboardIntent.ToggleSmsDetectionSheet -> _state.update {
                it.copy(showSmsDetection = !it.showSmsDetection)
            }

            is DashboardIntent.OpenSmsDraftTransaction -> viewModelScope.launch {
                // Tapped a bank-SMS notification: load that specific draft and open the add sheet
                // pre-filled with its amount / type / source, instead of a blank sheet.
                val draft = smsDraftRepository.getSmsDraftById(intent.draftId)
                if (draft != null) {
                    _state.update {
                        it.copy(
                            showAddTransaction = true,
                            transactionWithRelations = null,
                            initialTransactionType = null,
                            smsDraft = draft
                        )
                    }
                }
            }

            is DashboardIntent.IgnoreSmsDraft -> viewModelScope.launch {
                smsDraftRepository.markSmsDraftAsUsed(intent.draft.id)
                _state.update { it.copy(showDeleteSmsConfirmation = false, smsDraftToDelete = null) }
            }

            is DashboardIntent.ShowDeleteSmsConfirmation -> _state.update {
                it.copy(showDeleteSmsConfirmation = intent.show, smsDraftToDelete = intent.draft)
            }

            is DashboardIntent.UpdateSmsDraft -> viewModelScope.launch {
                smsDraftRepository.updateSmsDraft(intent.draft)
            }

            is DashboardIntent.QuickRegisterSms -> viewModelScope.launch {
                val draft = intent.draft
                if (draft.categoryId == null || draft.sourceId == null) {
                    // Fallback to manual registration if data is missing
                    onIntent(DashboardIntent.ShowTransactionBottomSheet(smsDraft = draft, type = draft.type))
                    return@launch
                }

                _state.update { it.copy(isLoading = true) }
                
                val finalAmount = if (_state.value.currency == "IRT") draft.amount / 10 else draft.amount
                
                val transaction = com.kazemieh.common.model.Transaction(
                    id = 0,
                    amount = finalAmount,
                    categoryId = draft.categoryId!!,
                    sourceId = draft.sourceId!!,
                    description = draft.body,
                    timeStamp = draft.timeStamp,
                    type = draft.type,
                    date = draft.date
                )
                val id = transactionUseCaseGroup.addTransactionUseCase(transaction, emptyList(), emptyList())
                if (id > 0) {
                    smsDraftRepository.markSmsDraftAsUsed(draft.id)
                }
                _state.update { it.copy(isLoading = false) }
            }

            DashboardIntent.ToggleCustomizeSheet -> _state.update {
                it.copy(showCustomizeSheet = !it.showCustomizeSheet)
            }

            is DashboardIntent.SetWidgetLayout -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardWidgetReordered)
                val oldItems = _state.value.dashboardWidgets.associateBy { it.widget }
                intent.items.forEach { newItem ->
                    val oldVisibility = oldItems[newItem.widget]?.visible
                    if (oldVisibility != null && oldVisibility != newItem.visible) {
                        analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardWidgetToggled)
                    }
                }
                preferenceUseCases.setStringPreference(
                    FinTrackPreferences.PREF_DASHBOARD_WIDGETS,
                    DashboardWidget.serialize(intent.items)
                )
                _state.update { it.copy(dashboardWidgets = intent.items) }
            }

            is DashboardIntent.ToggleBudgetSheet -> _state.update {
                it.copy(
                    showBudgetSheet = !it.showBudgetSheet,
                    selectedBudget = intent.budget
                )
            }

            is DashboardIntent.ToggleNoteSheet -> _state.update {
                it.copy(
                    showNoteSheet = !it.showNoteSheet,
                    selectedNote = intent.note
                )
            }

            is DashboardIntent.ToggleShoppingSheet -> _state.update {
                it.copy(
                    showShoppingSheet = !it.showShoppingSheet,
                    selectedShoppingItem = intent.item
                )
            }

            is DashboardIntent.ToggleFixedExpenseSheet -> _state.update {
                it.copy(
                    showFixedExpenseSheet = !it.showFixedExpenseSheet,
                    selectedFixedExpenseId = intent.fixedExpenseId
                )
            }

            is DashboardIntent.ToggleGoalSheet -> viewModelScope.launch {
                val goal = intent.goalId?.let { goalRepository.getGoalById(it) }
                _state.update {
                    it.copy(
                        showGoalSheet = !it.showGoalSheet,
                        selectedGoal = goal
                    )
                }
            }

            is DashboardIntent.ToggleInstallmentSheet -> _state.update {
                it.copy(
                    showInstallmentSheet = !it.showInstallmentSheet,
                    selectedInstallmentId = intent.installmentId
                )
            }

            is DashboardIntent.ToggleCheckSheet -> _state.update {
                it.copy(
                    showCheckSheet = !it.showCheckSheet,
                    selectedCheckId = intent.checkId
                )
            }
        }
    }
}

data class DashboardState(
    val showAddTransaction: Boolean = false,
    val showDeleteTransaction: Boolean = false,
    val showAddSource: Boolean = false,
    val selectedSource: Source? = null,
    val enableAnimationChart: Boolean = false,
    val transactionWithRelations: TransactionWithRelations? = null,
    val initialTransactionType: TransactionType? = null,
    val isBalanceVisible: Boolean = true,
    val isGlobalBalanceHidden: Boolean = false,
    val isBalanceLocallyRevealed: Boolean = false,
    val userName: String = "",
    val userInitial: String = "",
    val smsDrafts: List<SmsDraft> = emptyList(),
    val categories: List<Category> = emptyList(),
    val sources: List<Source> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val streak: Streak = Streak(),
    val showSmsDetection: Boolean = false,
    val showDeleteSmsConfirmation: Boolean = false,
    val smsDraftToDelete: SmsDraft? = null,
    val smsDraft: SmsDraft? = null,
    val currency: String = "IRR",
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val dashboardWidgets: List<DashboardWidgetItem> = DashboardWidget.defaultConfig(),
    val showCustomizeSheet: Boolean = false,
    val isLoading: Boolean = false,
    val disabledTools: Set<ToolFeature> = emptySet(),

    val showBudgetSheet: Boolean = false,
    val selectedBudget: com.kazemieh.common.model.BudgetWithProgress? = null,

    val showNoteSheet: Boolean = false,
    val selectedNote: com.kazemieh.common.model.Note? = null,

    val showShoppingSheet: Boolean = false,
    val selectedShoppingItem: com.kazemieh.common.model.ShoppingItem? = null,

    val showFixedExpenseSheet: Boolean = false,
    val selectedFixedExpenseId: Long? = null,

    val showGoalSheet: Boolean = false,
    val selectedGoal: com.kazemieh.common.model.Goal? = null,

    val showInstallmentSheet: Boolean = false,
    val selectedInstallmentId: Long? = null,

    val showCheckSheet: Boolean = false,
    val selectedCheckId: Long? = null
)


sealed interface DashboardIntent {
    data class ShowTransactionBottomSheet(
        val transactionWithRelations: TransactionWithRelations? = null,
        val type: TransactionType? = null,
        val smsDraft: SmsDraft? = null
    ) : DashboardIntent

    data class DeleteTransactionBottomSheet(val transactionWithRelations: TransactionWithRelations? = null) :
        DashboardIntent

    data object AnimationEnabled : DashboardIntent
    data class ShowAddSource(val source: Source? = null) : DashboardIntent
    data object ToggleBalanceVisibility : DashboardIntent
    data object ToggleSmsDetectionSheet : DashboardIntent
    data class OpenSmsDraftTransaction(val draftId: Long) : DashboardIntent
    data class IgnoreSmsDraft(val draft: SmsDraft) : DashboardIntent
    data class ShowDeleteSmsConfirmation(val show: Boolean, val draft: SmsDraft? = null) : DashboardIntent
    data class QuickRegisterSms(val draft: SmsDraft) : DashboardIntent
    data class UpdateSmsDraft(val draft: SmsDraft) : DashboardIntent
    data object ToggleCustomizeSheet : DashboardIntent
    data class SetWidgetLayout(val items: List<DashboardWidgetItem>) : DashboardIntent

    data class ToggleBudgetSheet(val budget: com.kazemieh.common.model.BudgetWithProgress? = null) : DashboardIntent
    data class ToggleNoteSheet(val note: com.kazemieh.common.model.Note? = null) : DashboardIntent
    data class ToggleShoppingSheet(val item: com.kazemieh.common.model.ShoppingItem? = null) : DashboardIntent
    data class ToggleFixedExpenseSheet(val fixedExpenseId: Long? = null) : DashboardIntent
    data class ToggleGoalSheet(val goalId: Long? = null) : DashboardIntent
    data class ToggleInstallmentSheet(val installmentId: Long? = null) : DashboardIntent
    data class ToggleCheckSheet(val checkId: Long? = null) : DashboardIntent
}
