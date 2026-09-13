package com.kazemieh.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.domain.usecase.SeedDataUseCase
import com.kazemieh.preferences.FinTrackPreferences
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
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val crashReporter: com.kazemieh.common.analytics.CrashReporter,
    private val seedDataUseCase: SeedDataUseCase,
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    private val _effect = Channel<OnboardingEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.OnboardingStarted)
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.NextStep -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("onboarding_step_completed"))
                if (_state.value.currentStep < 4) {
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
            is OnboardingIntent.SetSmsReading -> {
                preferenceUseCases.setBooleanPreference(
                    FinTrackPreferences.PREF_SMS_READING_ENABLED,
                    intent.enabled
                )
                analytics.track(com.kazemieh.common.analytics.ProductEvent.SmsPermissionResult(intent.enabled))
            }
            is OnboardingIntent.SelectTheme -> {
                _state.update { it.copy(selectedTheme = intent.theme) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.ThemeChanged(intent.theme))
            }
            is OnboardingIntent.SelectAccent -> {
                _state.update { it.copy(selectedAccent = intent.accent) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionCompleted("onboarding_accent_selected"))
            }
            is OnboardingIntent.NotificationPermissionResult -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationPermissionResult(intent.granted))
            }
            OnboardingIntent.NotificationPermissionRequested -> {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.NotificationPermissionRequested)
            }
        }
    }

    private fun finishOnboarding(useDefault: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Save theme preference
                preferenceUseCases.setStringPreference(
                    FinTrackPreferences.PREF_THEME,
                    _state.value.selectedTheme
                )
                preferenceUseCases.setStringPreference(
                    FinTrackPreferences.PREF_ACCENT,
                    _state.value.selectedAccent
                )

                // ... logic to build customSource and localizedNames ...
                val customSource = if (useDefault || _state.value.sourceName.isBlank()) {
                    null
                } else {
                    Source(
                        name = _state.value.sourceName,
                        balance = _state.value.sourceBalance.toLongOrNull() ?: 0L,
                        colorId = 34,
                        iconId = 22
                    )
                }
                val localizedNames = mapOf(
                    "source_cash" to getString(Res.string.seed_source_cash),
                    // ... (rest of the map is the same)
                    "tag_gift_desc" to getString(Res.string.seed_tag_gift_desc)
                )
                seedDataUseCase(
                    customSource = customSource,
                    securityQuestion = _state.value.securityQuestion,
                    securityAnswer = _state.value.securityAnswer,
                    localizedNames = localizedNames
                )
                analytics.track(com.kazemieh.common.analytics.ProductEvent.OnboardingCompleted)
                _effect.send(OnboardingEffect.NavigateToDashboard)
            } catch (e: Exception) {
                crashReporter.recordException(e, "onboarding_seed_failed")
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureActionFailed("onboarding", "seed_failed"))
                _effect.send(OnboardingEffect.ShowError(e.message ?: "Unknown Error"))
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
    val securityAnswer: String = "",
    val selectedTheme: String = "GLASS_DARK",
    val selectedAccent: String = "Default"
)

sealed interface OnboardingIntent {
    data object NextStep : OnboardingIntent
    data object PreviousStep : OnboardingIntent
    data object Skip : OnboardingIntent
    data object Finish : OnboardingIntent
    data class UpdateSourceDetails(val name: String, val balance: String) : OnboardingIntent
    data class UpdateSecurityDetails(val question: String, val answer: String) : OnboardingIntent
    data class SetSmsReading(val enabled: Boolean) : OnboardingIntent
    data object NotificationPermissionRequested : OnboardingIntent
    data class NotificationPermissionResult(val granted: Boolean) : OnboardingIntent
    data class SelectTheme(val theme: String) : OnboardingIntent
    data class SelectAccent(val accent: String) : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object NavigateToDashboard : OnboardingEffect
    data class ShowError(val message: String) : OnboardingEffect
}
