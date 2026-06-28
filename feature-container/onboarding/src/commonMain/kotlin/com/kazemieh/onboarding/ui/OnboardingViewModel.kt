package com.kazemieh.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.SeedDataUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.kazemieh.common.model.Source
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.getString

class OnboardingViewModel(
    private val seedDataUseCase: SeedDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _effect = Channel<OnboardingEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.NextStep -> {
                if (_state.value.currentStep < 3) {
                    _state.update { it.copy(currentStep = it.currentStep + 1) }
                } else {
                    finishOnboarding()
                }
            }
            OnboardingIntent.PreviousStep -> {
                if (_state.value.currentStep > 1) {
                    _state.update { it.copy(currentStep = it.currentStep - 1) }
                }
            }
            is OnboardingIntent.UpdateSourceDetails -> {
                _state.update { it.copy(sourceName = intent.name, sourceBalance = intent.balance) }
            }
            is OnboardingIntent.UpdateSecurityDetails -> {
                _state.update { it.copy(securityQuestion = intent.question, securityAnswer = intent.answer) }
            }
            OnboardingIntent.Skip -> finishOnboarding(useDefault = true)
            OnboardingIntent.Finish -> finishOnboarding()
        }
    }

    private fun finishOnboarding(useDefault: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val customSource = if (useDefault || _state.value.sourceName.isBlank()) {
                    null
                } else {
                    Source(
                        name = _state.value.sourceName,
                        balance = _state.value.sourceBalance.toIntOrNull() ?: 0,
                        colorId = 34,
                        iconId = 22
                    )
                }
                val localizedNames = mapOf(
                    "source_cash" to getString(Res.string.seed_source_cash),
                    "source_cash_desc" to getString(Res.string.seed_source_cash_desc),
                    "cat_salary" to getString(Res.string.seed_cat_salary),
                    "cat_bonus" to getString(Res.string.seed_cat_bonus),
                    "cat_interest" to getString(Res.string.seed_cat_interest),
                    "cat_gift" to getString(Res.string.seed_cat_gift),
                    "cat_other_income" to getString(Res.string.seed_cat_other_income),
                    "cat_food" to getString(Res.string.seed_cat_food),
                    "cat_transport" to getString(Res.string.seed_cat_transport),
                    "cat_rent" to getString(Res.string.seed_cat_rent),
                    "cat_bills" to getString(Res.string.seed_cat_bills),
                    "cat_shopping" to getString(Res.string.seed_cat_shopping),
                    "cat_health" to getString(Res.string.seed_cat_health),
                    "cat_education" to getString(Res.string.seed_cat_education),
                    "cat_entertainment" to getString(Res.string.seed_cat_entertainment),
                    "cat_other_expense" to getString(Res.string.seed_cat_other_expense),
                    "cat_transfer" to getString(Res.string.seed_cat_transfer)
                )
                seedDataUseCase(
                    customSource = customSource,
                    securityQuestion = _state.value.securityQuestion,
                    securityAnswer = _state.value.securityAnswer,
                    localizedNames = localizedNames
                )
                _effect.send(OnboardingEffect.NavigateToDashboard)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class OnboardingState(
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val sourceName: String = "",
    val sourceBalance: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = ""
)

sealed interface OnboardingIntent {
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data object Skip : OnboardingIntent
    data object Finish : OnboardingIntent
    data class UpdateSourceDetails(val name: String, val balance: String) : OnboardingIntent
    data class UpdateSecurityDetails(val question: String, val answer: String) : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToDashboard : OnboardingEffect
}
