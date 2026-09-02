package com.kazemieh.settings

import androidx.lifecycle.ViewModel
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.money.Currency
import com.kazemieh.money.CurrencyProvider
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_CURRENCY
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_CUSTOM_CURRENCIES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CurrencySettingsViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val preferenceUseCases: PreferenceUseCases,
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencySettingsState())
    val state: StateFlow<CurrencySettingsState> = _state

    init {
        loadSettings()
        loadCustomCurrencies()
        filterCurrencies("")
    }

    private fun loadCustomCurrencies() {
        val customJson = preferenceUseCases.getStringPreference(PREF_CUSTOM_CURRENCIES, "")
        val custom = if (customJson.isNotEmpty()) {
            try {
                Json.decodeFromString<List<Currency>>(customJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _state.update { it.copy(customCurrencies = custom) }
    }

    private fun loadSettings() {
        val currencyJson = preferenceUseCases.getStringPreference(PREF_CURRENCY, "")
        val selectedCurrency = if (currencyJson.isNotEmpty()) {
            try {
                Json.decodeFromString<Currency>(currencyJson)
            } catch (e: Exception) {
                Currency.TOMAN
            }
        } else Currency.TOMAN
        
        _state.update { it.copy(selectedCurrency = selectedCurrency) }
    }

    fun onIntent(intent: CurrencySettingsIntent) {
        when (intent) {
                        is CurrencySettingsIntent.SelectCurrency -> {
                _state.update { it.copy(pendingCurrency = intent.currency) }
            }
            is CurrencySettingsIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query) }
                filterCurrencies(intent.query)
            }
            CurrencySettingsIntent.ConfirmSelection -> {
                _state.update { it.copy(showConfirmDialog = true) }
            }
            CurrencySettingsIntent.DismissDialogs -> {
                _state.update { it.copy(showConfirmDialog = false, showRateDialog = false, pendingCurrency = null) }
            }
            is CurrencySettingsIntent.SubmitConfirmDialog -> {
                if (intent.updateOld) {
                    _state.update { it.copy(showConfirmDialog = false, showRateDialog = true) }
                } else {
                    _state.value.pendingCurrency?.let {
                        val json = kotlinx.serialization.json.Json.encodeToString(it)
                        preferenceUseCases.setStringPreference(com.kazemieh.preferences.FinTrackPreferences.PREF_CURRENCY, json)
                        _state.update { s -> s.copy(selectedCurrency = it, showConfirmDialog = false, showRateDialog = false, pendingCurrency = null) }
                    }
                }
            }
            is CurrencySettingsIntent.UpdateRate -> {
                _state.update { it.copy(conversionRate = intent.rateStr) }
            }
            CurrencySettingsIntent.ConfirmRateDialog -> {
                _state.value.pendingCurrency?.let {
                    val rate = _state.value.conversionRate.toDoubleOrNull() ?: 1.0
                    val json = kotlinx.serialization.json.Json.encodeToString(it)
                    preferenceUseCases.setStringPreference(com.kazemieh.preferences.FinTrackPreferences.PREF_CURRENCY, json)
                    
                    // Actually updating transactions is skipped for now, but state gets updated
                    _state.update { s -> s.copy(selectedCurrency = it, showConfirmDialog = false, showRateDialog = false, pendingCurrency = null) }
                }
            }
        }
    }

    private fun filterCurrencies(query: String) {
        val fiat = if (query.isEmpty()) {
            CurrencyProvider.fiatCurrencies
        } else {
            CurrencyProvider.fiatCurrencies.filter { 
                it.code.contains(query, ignoreCase = true) || 
                it.displayName.contains(query, ignoreCase = true)
            }
        }

        val crypto = if (query.isEmpty()) {
            CurrencyProvider.cryptocurrencies
        } else {
            CurrencyProvider.cryptocurrencies.filter { 
                it.code.contains(query, ignoreCase = true) || 
                it.displayName.contains(query, ignoreCase = true)
            }
        }

        _state.update { 
            it.copy(
                fiatCurrencies = fiat,
                cryptoCurrencies = crypto
            ) 
        }
    }
}

data class CurrencySettingsState(
    val selectedCurrency: Currency = Currency.TOMAN,
    val pendingCurrency: Currency? = null,
    val showConfirmDialog: Boolean = false,
    val showRateDialog: Boolean = false,
    val conversionRate: String = "1.0",
    val searchQuery: String = "",
    val fiatCurrencies: List<Currency> = emptyList(),
    val cryptoCurrencies: List<Currency> = emptyList(),
    val customCurrencies: List<Currency> = emptyList()
)

sealed interface CurrencySettingsIntent {
    data class SelectCurrency(val currency: Currency) : CurrencySettingsIntent
    data class UpdateSearchQuery(val query: String) : CurrencySettingsIntent
    object ConfirmSelection : CurrencySettingsIntent
    data class SubmitConfirmDialog(val updateOld: Boolean) : CurrencySettingsIntent
    data class UpdateRate(val rateStr: String) : CurrencySettingsIntent
    object ConfirmRateDialog : CurrencySettingsIntent
    object DismissDialogs : CurrencySettingsIntent
}
