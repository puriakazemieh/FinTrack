package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.ThemeMode
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_FOLLOW_SYSTEM_THEME
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME_END_TIME
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME_MODE
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME_START_TIME
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ThemeSettingsViewModel(
    private val preferenceUseCases: PreferenceUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ThemeSettingsState())
    val state: StateFlow<ThemeSettingsState> = _state

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val themeName = preferenceUseCases.getStringPreference(PREF_THEME, AppTheme.GLASS_DARK.name)
        val modeName = preferenceUseCases.getStringPreference(PREF_THEME_MODE, ThemeMode.MANUAL.name)
        val startTime = preferenceUseCases.getStringPreference(PREF_THEME_START_TIME, "20:00")
        val endTime = preferenceUseCases.getStringPreference(PREF_THEME_END_TIME, "07:00")
        val followSystem = preferenceUseCases.getBooleanPreference(PREF_FOLLOW_SYSTEM_THEME, false)

        _state.update {
            it.copy(
                selectedTheme = try {
                    AppTheme.valueOf(themeName)
                } catch (e: Exception) {
                    AppTheme.GLASS_DARK
                },
                selectedMode = try {
                    ThemeMode.valueOf(modeName)
                } catch (e: Exception) {
                    ThemeMode.MANUAL
                },
                startTime = startTime,
                endTime = endTime,
                followSystemTheme = followSystem
            )
        }
    }

    fun onIntent(intent: ThemeSettingsIntent) {
        when (intent) {
            is ThemeSettingsIntent.SelectTheme -> {
                preferenceUseCases.setStringPreference(PREF_THEME, intent.theme.name)
                _state.update { it.copy(selectedTheme = intent.theme) }
            }

            is ThemeSettingsIntent.SelectMode -> {
                preferenceUseCases.setStringPreference(PREF_THEME_MODE, intent.mode.name)
                _state.update { it.copy(selectedMode = intent.mode) }
            }

            is ThemeSettingsIntent.SetStartTime -> {
                preferenceUseCases.setStringPreference(PREF_THEME_START_TIME, intent.time)
                _state.update { it.copy(startTime = intent.time) }
            }

            is ThemeSettingsIntent.SetEndTime -> {
                preferenceUseCases.setStringPreference(PREF_THEME_END_TIME, intent.time)
                _state.update { it.copy(endTime = intent.time) }
            }

            is ThemeSettingsIntent.ToggleFollowSystemTheme -> {
                val newValue = !_state.value.followSystemTheme
                preferenceUseCases.setBooleanPreference(PREF_FOLLOW_SYSTEM_THEME, newValue)
                _state.update { it.copy(followSystemTheme = newValue) }
            }
        }
    }
}

data class ThemeSettingsState(
    val selectedTheme: AppTheme = AppTheme.GLASS_DARK,
    val selectedMode: ThemeMode = ThemeMode.MANUAL,
    val startTime: String = "20:00",
    val endTime: String = "07:00",
    val followSystemTheme: Boolean = false
)

sealed interface ThemeSettingsIntent {
    data class SelectTheme(val theme: AppTheme) : ThemeSettingsIntent
    data class SelectMode(val mode: ThemeMode) : ThemeSettingsIntent
    data class SetStartTime(val time: String) : ThemeSettingsIntent
    data class SetEndTime(val time: String) : ThemeSettingsIntent
    data object ToggleFollowSystemTheme : ThemeSettingsIntent
}
