package com.kazemieh.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.repository.SmsDraftRepository
import com.kazemieh.domain.usecase.PreferenceUseCases
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
    private val preferenceUseCases: PreferenceUseCases,
    private val smsDraftRepository: SmsDraftRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        observeUserName()
        observeSmsDrafts()
    }

    private fun observeSmsDrafts() {
        smsDraftRepository.observeUnusedSmsDrafts()
            .onEach { drafts ->
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
            val displayInitial = fullName.ifBlank { getString(Res.string.placeholder_user_initial) }.first().toString()

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
            is DashboardIntent.ShowTransactionBottomSheet -> _state.update {
                it.copy(
                    showAddTransaction = !_state.value.showAddTransaction,
                    transactionWithRelations = intent.transactionWithRelations,
                    initialTransactionType = intent.type,
                    smsDraft = intent.smsDraft
                )
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

            DashboardIntent.ToggleBalanceVisibility -> _state.update {
                it.copy(isBalanceVisible = !it.isBalanceVisible)
            }

            DashboardIntent.ToggleSmsDetectionSheet -> _state.update {
                it.copy(showSmsDetection = !it.showSmsDetection)
            }

            is DashboardIntent.IgnoreSmsDraft -> viewModelScope.launch {
                smsDraftRepository.markSmsDraftAsUsed(intent.draft.id)
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
    val growthPercentage: String = "+2.5%", // Placeholder
    val userName: String = "",
    val userInitial: String = "",
    val smsDrafts: List<SmsDraft> = emptyList(),
    val showSmsDetection: Boolean = false,
    val smsDraft: SmsDraft? = null
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
    data class IgnoreSmsDraft(val draft: SmsDraft) : DashboardIntent
}
