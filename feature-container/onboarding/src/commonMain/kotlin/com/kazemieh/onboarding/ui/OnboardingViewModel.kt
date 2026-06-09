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
                seedDataUseCase(customSource)
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
    val sourceBalance: String = ""
)

sealed interface OnboardingIntent {
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data object Skip : OnboardingIntent
    data object Finish : OnboardingIntent
    data class UpdateSourceDetails(val name: String, val balance: String) : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToDashboard : OnboardingEffect
}
