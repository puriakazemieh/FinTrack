package com.kazemieh.dashboard

import androidx.lifecycle.ViewModel
import com.kazemieh.domain.usecase.resolveSmsSource
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.common.model.Achievement
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Streak
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.repository.GoalRepository
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.usecase.ObserveAchievementsUseCase
import com.kazemieh.domain.usecase.ObserveCategoriesFlatUseCase
import com.kazemieh.domain.usecase.RegisterSmsDraftsUseCase
import com.kazemieh.domain.usecase.SmsBatchProgress
import com.kazemieh.domain.usecase.SmsDraftPreparation
import com.kazemieh.domain.usecase.SmsPreparationIssue
import com.kazemieh.domain.usecase.SmsRegistrationResult
import com.kazemieh.domain.usecase.SmsRegistrationStatus
import com.kazemieh.domain.usecase.prepareSmsDraftTransaction
import com.kazemieh.domain.usecase.ObserveSourcesUseCase
import com.kazemieh.domain.usecase.ObserveStreakUseCase
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import com.kazemieh.money.Currency
import com.kazemieh.preferences.FinTrackPreferences
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import fintrack.core.designsystem.generated.resources.placeholder_user_initial
import fintrack.core.designsystem.generated.resources.placeholder_user_name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.getString


class DashboardViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val preferenceUseCases: PreferenceUseCases,
    private val smsDraftRepository: SmsDraftRepository,
    private val observeCategories: ObserveCategoriesFlatUseCase,
    private val observeSources: ObserveSourcesUseCase,
    private val observeAchievements: ObserveAchievementsUseCase,
    private val observeStreak: ObserveStreakUseCase,
    private val transactionUseCaseGroup: TransactionUseCaseGroup,
    private val goalRepository: GoalRepository,
    private val registerSmsDrafts: RegisterSmsDraftsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardViewed)
        observeUserName()
        observeSmsDrafts()
        loadAchievements()
        loadStreak()
        observeWidgetLayout()
        observeDisabledTools()
        observeHideBalance()
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
                            ToolFeature.ASSETS,
                            ToolFeature.FX_RATES,
                            ToolFeature.CONVERTER,
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

    private fun observeSmsDrafts() {
        combine(smsDraftRepository.observeUnusedSmsDrafts(), flow { emitAll(observeSources()) },
            observeCategories(), preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_CURRENCY, "")) {
                drafts, sources, categories, preference ->
            val currency = Currency.valueOf(preference).code
            val ready = drafts.filter {
                prepareSmsDraftTransaction(it, categories, sources, currency) is SmsDraftPreparation.Ready
            }.map { it.id }.toSet()
            val presented = drafts.map { draft ->
                if (draft.sourceId == null) draft.copy(sourceId = resolveSmsSource(draft, sources)?.id) else draft
            }
            SmsSnapshot(presented, categories, sources, currency, ready)
        }.flowOn(Dispatchers.Default)
            .onEach { snapshot ->
                _state.update { it.copy(smsDrafts = snapshot.drafts, categories = snapshot.categories,
                    sources = snapshot.sources, currency = snapshot.currency, readySmsDraftIds = snapshot.ready) }
            }.launchIn(viewModelScope)
    }

    private data class SmsSnapshot(val drafts: List<SmsDraft>, val categories: List<Category>,
        val sources: List<Source>, val currency: String, val ready: Set<Long>)

    private fun smsError(result: SmsRegistrationResult): UiText? {
        val resource = when (result.status) {
            SmsRegistrationStatus.POSTED -> return null
            SmsRegistrationStatus.FAILED -> Res.string.sms_registration_failed
            SmsRegistrationStatus.UNAVAILABLE -> Res.string.sms_unavailable
            SmsRegistrationStatus.INCOMPLETE -> when (result.issue) {
                SmsPreparationIssue.CATEGORY -> Res.string.sms_missing_category
                SmsPreparationIssue.SOURCE -> Res.string.sms_missing_source
                SmsPreparationIssue.CURRENCY -> Res.string.sms_source_currency_incompatible
                SmsPreparationIssue.UNAVAILABLE -> Res.string.sms_unavailable
                else -> Res.string.sms_review_details
            }
        }
        return UiText.StringResourceText(resource)
    }

    private fun registerSms(id: Long? = null) {
        val current = _state.value
        if (current.isSmsRegistering || current.isSmsDeleting) return
        val ids = if (id == null) current.smsDrafts.map { it.id } else listOf(id)
        if (ids.isEmpty() || (id == null && current.readySmsDraftIds.isEmpty())) return
        _state.update { it.copy(isSmsRegistering = true, smsBatchProgress = null,
            smsRegistrationErrors = if (id == null) emptyMap() else it.smsRegistrationErrors - id,
            smsGeneralError = null, smsDeletedCount = null) }
        viewModelScope.launch {
            try {
                if (id == null) {
                    registerSmsDrafts(ids, current.currency) { progress ->
                        val errors = progress.results.mapNotNull { result ->
                            smsError(result)?.let { result.draftId to it }
                        }.toMap()
                        _state.update { it.copy(smsBatchProgress = progress, smsRegistrationErrors = errors) }
                    }
                } else {
                    val result = registerSmsDrafts.registerOne(id, current.currency)
                    val error = smsError(result)
                    _state.update { it.copy(smsRegistrationErrors = if (error == null)
                        it.smsRegistrationErrors - id else it.smsRegistrationErrors + (id to error),
                        smsGeneralError = if (result.status == SmsRegistrationStatus.UNAVAILABLE) error else null) }
                    if (result.status == SmsRegistrationStatus.POSTED) runCatching {
                        analytics.track(com.kazemieh.common.analytics.ProductEvent.SmsDraftQuickRegistered)
                    }
                    if (result.status == SmsRegistrationStatus.INCOMPLETE) {
                        val draft = smsDraftRepository.getSmsDraftById(id)
                        if (draft != null && !draft.isUsed) _state.update { it.copy(showAddTransaction = true,
                            transactionWithRelations = null, initialTransactionType = draft.type, smsDraft = draft) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(smsGeneralError = UiText.StringResourceText(Res.string.sms_registration_failed)) }
            } finally {
                _state.update { it.copy(isSmsRegistering = false) }
            }
        }
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
                // The dashboard eye is temporary: it never modifies the privacy preference.
                // When privacy is enabled it reveals the value locally; otherwise it simply
                // toggles the currently visible value, as users expect from the eye button.
                val nextVisible = if (_state.value.isGlobalBalanceHidden) {
                    !_state.value.isBalanceLocallyRevealed
                } else {
                    !_state.value.isBalanceVisible
                }
                _state.update {
                    it.copy(
                        isBalanceLocallyRevealed = if (it.isGlobalBalanceHidden) nextVisible else false,
                        isBalanceVisible = nextVisible
                    )
                }
                if (nextVisible) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.DashboardWalletSummaryViewed)
                }
            }

            DashboardIntent.ResetTemporaryBalanceReveal -> _state.update {
                if (it.isGlobalBalanceHidden) {
                    it.copy(isBalanceVisible = false, isBalanceLocallyRevealed = false)
                } else it
            }

            DashboardIntent.ToggleSmsDetectionSheet -> _state.update {
                it.copy(showSmsDetection = !it.showSmsDetection)
            }

            is DashboardIntent.OpenSmsDraftTransaction -> viewModelScope.launch {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("sms_draft_opened"))
                // Tapped a bank-SMS notification: load that specific draft and open the add sheet
                // pre-filled with its amount / type / source, instead of a blank sheet.
                val draft = smsDraftRepository.getSmsDraftById(intent.draftId)
                if (draft != null && !draft.isUsed) {
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

            is DashboardIntent.IgnoreSmsDraft -> {
                if (_state.value.isSmsDeleting || _state.value.isSmsRegistering) return
                _state.update { it.copy(isSmsDeleting = true, smsDeleteError = null) }
                viewModelScope.launch {
                    try {
                        smsDraftRepository.markSmsDraftAsUsed(intent.draft.id)
                        _state.update { it.copy(showDeleteSmsConfirmation = false, smsDraftToDelete = null) }
                        runCatching { analytics.track(com.kazemieh.common.analytics.ProductEvent.SmsDraftIgnored) }
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) {
                        _state.update { it.copy(smsDeleteError = UiText.StringResourceText(Res.string.sms_delete_failed)) }
                    } finally { _state.update { it.copy(isSmsDeleting = false) } }
                }
            }

            is DashboardIntent.IgnoreAllSmsDrafts -> {
                if (_state.value.isSmsDeleting || _state.value.isSmsRegistering || !_state.value.showDeleteAllSmsConfirmation) return
                val ids = _state.value.smsDeleteIds
                _state.update { it.copy(isSmsDeleting = true, smsDeleteError = null) }
                viewModelScope.launch {
                    try {
                        val deleted = smsDraftRepository.deleteUnusedSmsDrafts(ids)
                        _state.update { it.copy(showDeleteAllSmsConfirmation = false, smsDeleteIds = emptyList(),
                            smsDeletedCount = deleted.size, smsBatchProgress = null, smsGeneralError = null,
                            smsRegistrationErrors = it.smsRegistrationErrors - deleted.toSet()) }
                        runCatching { analytics.track(com.kazemieh.common.analytics.ProductEvent.SmsDraftsBulkDeleted) }
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) {
                        _state.update { it.copy(smsDeleteError = UiText.StringResourceText(Res.string.sms_delete_failed)) }
                    } finally { _state.update { it.copy(isSmsDeleting = false) } }
                }
            }

            is DashboardIntent.ShowDeleteSmsConfirmation -> _state.update {
                if (it.isSmsDeleting || it.isSmsRegistering) it else
                    it.copy(showDeleteSmsConfirmation = intent.show, smsDraftToDelete = intent.draft, smsDeleteError = null)
            }

            is DashboardIntent.ShowDeleteAllSmsConfirmation -> _state.update {
                if (it.isSmsDeleting || it.isSmsRegistering) it else
                    it.copy(showDeleteAllSmsConfirmation = intent.show,
                        smsDeleteIds = if (intent.show) it.smsDrafts.map { draft -> draft.id } else emptyList(), smsDeleteError = null)
            }

            is DashboardIntent.UpdateSmsDraft -> {
                if (_state.value.isSmsRegistering || _state.value.isSmsDeleting) return
                viewModelScope.launch {
                    try {
                        smsDraftRepository.updateSmsDraft(intent.draft)
                        _state.update { it.copy(smsRegistrationErrors = it.smsRegistrationErrors - intent.draft.id) }
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) {
                        _state.update { it.copy(smsRegistrationErrors = it.smsRegistrationErrors +
                            (intent.draft.id to UiText.StringResourceText(Res.string.sms_registration_failed))) }
                    }
                }
            }

            is DashboardIntent.QuickRegisterSms -> registerSms(intent.draft.id)
            DashboardIntent.RegisterAllSmsDrafts -> registerSms()
            DashboardIntent.DismissSmsSummary -> _state.update { it.copy(smsBatchProgress = null,
                smsDeletedCount = null, smsGeneralError = null) }

            DashboardIntent.ToggleCustomizeSheet -> _state.update {
                it.copy(showCustomizeSheet = !it.showCustomizeSheet)
            }

            is DashboardIntent.ShowWidgetMenu -> {
                if (intent.widget != null) {
                    analytics.track(
                        com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted(
                            "dashboard_widget_menu_opened"
                        )
                    )
                }
                _state.update { it.copy(widgetMenu = intent.widget) }
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
    val showDeleteAllSmsConfirmation: Boolean = false,
    val isSmsDeleting: Boolean = false,
    val isSmsRegistering: Boolean = false,
    val readySmsDraftIds: Set<Long> = emptySet(),
    val smsBatchProgress: SmsBatchProgress? = null,
    val smsRegistrationErrors: Map<Long, UiText> = emptyMap(),
    val smsDeleteIds: List<Long> = emptyList(),
    val smsDeleteError: UiText? = null,
    val smsGeneralError: UiText? = null,
    val smsDeletedCount: Int? = null,
    val smsDraftToDelete: SmsDraft? = null,
    val smsDraft: SmsDraft? = null,
    val currency: String = "IRR",
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val dashboardWidgets: List<DashboardWidgetItem> = DashboardWidget.defaultConfig(),
    val showCustomizeSheet: Boolean = false,
    val widgetMenu: DashboardWidget? = null,
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
    data object ResetTemporaryBalanceReveal : DashboardIntent
    data object ToggleSmsDetectionSheet : DashboardIntent
    data class OpenSmsDraftTransaction(val draftId: Long) : DashboardIntent
    data class IgnoreSmsDraft(val draft: SmsDraft) : DashboardIntent
    data object IgnoreAllSmsDrafts : DashboardIntent
    data object RegisterAllSmsDrafts : DashboardIntent
    data object DismissSmsSummary : DashboardIntent
    data class ShowDeleteSmsConfirmation(val show: Boolean, val draft: SmsDraft? = null) :
        DashboardIntent

    data class ShowDeleteAllSmsConfirmation(val show: Boolean) : DashboardIntent
    data class QuickRegisterSms(val draft: SmsDraft) : DashboardIntent
    data class UpdateSmsDraft(val draft: SmsDraft) : DashboardIntent
    data object ToggleCustomizeSheet : DashboardIntent
    data class ShowWidgetMenu(val widget: DashboardWidget? = null) : DashboardIntent
    data class SetWidgetLayout(val items: List<DashboardWidgetItem>) : DashboardIntent

    data class ToggleBudgetSheet(val budget: com.kazemieh.common.model.BudgetWithProgress? = null) :
        DashboardIntent

    data class ToggleNoteSheet(val note: com.kazemieh.common.model.Note? = null) : DashboardIntent
    data class ToggleShoppingSheet(val item: com.kazemieh.common.model.ShoppingItem? = null) :
        DashboardIntent

    data class ToggleFixedExpenseSheet(val fixedExpenseId: Long? = null) : DashboardIntent
    data class ToggleGoalSheet(val goalId: Long? = null) : DashboardIntent
    data class ToggleInstallmentSheet(val installmentId: Long? = null) : DashboardIntent
    data class ToggleCheckSheet(val checkId: Long? = null) : DashboardIntent
}
