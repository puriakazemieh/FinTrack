package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.money.Currency
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_CURRENCY
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_THEME
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
        val currencyName = preferenceUseCases.getStringPreference(PREF_CURRENCY, Currency.TOMAN.name)

        _state.update {
            it.copy(
                selectedTheme = AppTheme.valueOf(themeName),
                selectedCurrency = Currency.valueOf(currencyName)
            )
        }
    }

    fun onIntent(intent: ThemeAndCurrencyIntent) {
        when (intent) {
            is ThemeAndCurrencyIntent.SelectTheme -> {
                preferenceUseCases.setStringPreference(PREF_THEME, intent.theme.name)
                _state.update { it.copy(selectedTheme = intent.theme) }
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
    val selectedCurrency: Currency = Currency.TOMAN
)

sealed interface ThemeAndCurrencyIntent {
    data class SelectTheme(val theme: AppTheme) : ThemeAndCurrencyIntent
    data class SelectCurrency(val currency: Currency) : ThemeAndCurrencyIntent
}
