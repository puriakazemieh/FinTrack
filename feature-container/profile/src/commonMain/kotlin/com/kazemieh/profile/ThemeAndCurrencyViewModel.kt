package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.ThemeMode
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.money.Currency
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_CURRENCY
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME_END_TIME
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME_MODE
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME_START_TIME
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ThemeAndCurrencyViewModel(
    private val preferenceUseCases: PreferenceUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(ThemeAndCurrencyState())
    val state: StateFlow<ThemeAndCurrencyState> = _state

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val themeName = preferenceUseCases.getStringPreference(PREF_THEME, AppTheme.GLASS_DARK.name)
        val modeName = preferenceUseCases.getStringPreference(PREF_THEME_MODE, ThemeMode.MANUAL.name)
        val startTime = preferenceUseCases.getStringPreference(PREF_THEME_START_TIME, "20:00")
        val endTime = preferenceUseCases.getStringPreference(PREF_THEME_END_TIME, "07:00")
        val currencyName = preferenceUseCases.getStringPreference(PREF_CURRENCY, Currency.TOMAN.name)

        _state.update {
            it.copy(
                selectedTheme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.GLASS_DARK },
                selectedMode = try { ThemeMode.valueOf(modeName) } catch (e: Exception) { ThemeMode.MANUAL },
                startTime = startTime,
                endTime = endTime,
                selectedCurrency = try { Currency.valueOf(currencyName) } catch (e: Exception) { Currency.TOMAN }
            )
        }
    }

    fun onIntent(intent: ThemeAndCurrencyIntent) {
        when (intent) {
            is ThemeAndCurrencyIntent.SelectTheme -> {
                preferenceUseCases.setStringPreference(PREF_THEME, intent.theme.name)
                _state.update { it.copy(selectedTheme = intent.theme) }
            }
            is ThemeAndCurrencyIntent.SelectMode -> {
                preferenceUseCases.setStringPreference(PREF_THEME_MODE, intent.mode.name)
                _state.update { it.copy(selectedMode = intent.mode) }
            }
            is ThemeAndCurrencyIntent.SetStartTime -> {
                preferenceUseCases.setStringPreference(PREF_THEME_START_TIME, intent.time)
                _state.update { it.copy(startTime = intent.time) }
            }
            is ThemeAndCurrencyIntent.SetEndTime -> {
                preferenceUseCases.setStringPreference(PREF_THEME_END_TIME, intent.time)
                _state.update { it.copy(endTime = intent.time) }
            }
            is ThemeAndCurrencyIntent.SelectCurrency -> {
                preferenceUseCases.setStringPreference(PREF_CURRENCY, intent.currency.name)
                _state.update { it.copy(selectedCurrency = intent.currency) }
            }
        }
    }
}

data class ThemeAndCurrencyState(
    val selectedTheme: AppTheme = AppTheme.GLASS_DARK,
    val selectedMode: ThemeMode = ThemeMode.MANUAL,
    val startTime: String = "20:00",
    val endTime: String = "07:00",
    val selectedCurrency: Currency = Currency.TOMAN
)

sealed interface ThemeAndCurrencyIntent {
    data class SelectTheme(val theme: AppTheme) : ThemeAndCurrencyIntent
    data class SelectMode(val mode: ThemeMode) : ThemeAndCurrencyIntent
    data class SetStartTime(val time: String) : ThemeAndCurrencyIntent
    data class SetEndTime(val time: String) : ThemeAndCurrencyIntent
    data class SelectCurrency(val currency: Currency) : ThemeAndCurrencyIntent
}
