package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.ToolFeature
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class ManageToolsViewModel(
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(ManageToolsState())
    val state: StateFlow<ManageToolsState> = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.ToolsHubViewed)
        preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_DISABLED_TOOLS, "")
            .onEach { csv ->
                _state.update { it.copy(disabledTools = ToolFeature.parseDisabled(csv)) }
            }.launchIn(viewModelScope)

        _state.update {
            it.copy(
                smsReadingEnabled = preferenceUseCases.getBooleanPreference(
                    FinTrackPreferences.PREF_SMS_READING_ENABLED,
                    true
                )
            )
        }
    }

    fun onIntent(intent: ManageToolsIntent) {
        when (intent) {
            is ManageToolsIntent.ToggleTool -> {
                val current = _state.value.disabledTools
                val updated = if (intent.feature in current) current - intent.feature else current + intent.feature
                preferenceUseCases.setStringPreference(
                    FinTrackPreferences.PREF_DISABLED_TOOLS,
                    ToolFeature.serializeDisabled(updated)
                )
                _state.update { it.copy(disabledTools = updated) }
            }

            is ManageToolsIntent.SetSmsReading -> {
                preferenceUseCases.setBooleanPreference(
                    FinTrackPreferences.PREF_SMS_READING_ENABLED,
                    intent.enabled
                )
                _state.update { it.copy(smsReadingEnabled = intent.enabled) }
            }
        }
    }
}

data class ManageToolsState(
    val disabledTools: Set<ToolFeature> = emptySet(),
    val smsReadingEnabled: Boolean = false
)

sealed interface ManageToolsIntent {
    data class ToggleTool(val feature: ToolFeature) : ManageToolsIntent
    data class SetSmsReading(val enabled: Boolean) : ManageToolsIntent
}
