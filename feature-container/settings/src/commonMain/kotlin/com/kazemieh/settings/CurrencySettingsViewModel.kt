package com.kazemieh.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.repository.AssetRepository
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.money.Currency
import com.kazemieh.money.CurrencyProvider
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_CURRENCY
import com.kazemieh.preferences.FinTrackPreferences.Companion.PREF_CUSTOM_CURRENCIES
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CurrencySettingsViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val preferenceUseCases: PreferenceUseCases,
    private val assetRepository: AssetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencySettingsState())
    val state: StateFlow<CurrencySettingsState> = _state

    private val _effect = Channel<CurrencySettingsEffect>()
    val effect = _effect.receiveAsFlow()

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
                _state.update { it.copy(pendingCurrency = intent.currency, showConfirmSheet = true) }
            }
            is CurrencySettingsIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query) }
                filterCurrencies(intent.query)
            }
            CurrencySettingsIntent.ConfirmSelection -> {
                _state.update { it.copy(showConfirmSheet = true) }
            }
            CurrencySettingsIntent.DismissDialogs -> {
                _state.update { it.copy(showConfirmSheet = false, showRateSheet = false, rateError = null) }
            }
            is CurrencySettingsIntent.SubmitConfirmDialog -> {
                if (intent.updateOld) {
                    _state.update { it.copy(showConfirmSheet = false, showRateSheet = true) }
                } else {
                    applyNewCurrencyWithoutConversion()
                }
            }
            is CurrencySettingsIntent.UpdateRate -> {
                _state.update { it.copy(conversionRate = intent.rateStr, rateError = null) }
            }
            CurrencySettingsIntent.FetchRateFromServer -> fetchRateFromServer()
            CurrencySettingsIntent.ConfirmRateDialog -> confirmAndConvert()
        }
    }

    private fun applyNewCurrencyWithoutConversion() {
        _state.value.pendingCurrency?.let { newCurrency ->
            val json = Json.encodeToString(newCurrency)
            preferenceUseCases.setStringPreference(PREF_CURRENCY, json)
            _state.update {
                it.copy(selectedCurrency = newCurrency, pendingCurrency = null, showConfirmSheet = false, showRateSheet = false)
            }
            viewModelScope.launch { _effect.send(CurrencySettingsEffect.CurrencyChanged) }
        }
    }

    private fun fetchRateFromServer() {
        val oldCurrency = _state.value.selectedCurrency
        val newCurrency = _state.value.pendingCurrency ?: return
        viewModelScope.launch {
            _state.update { it.copy(isFetchingRate = true, rateError = null) }
            try {
                val rates = assetRepository.syncRates()
                if (rates.isEmpty()) {
                    _state.update { it.copy(isFetchingRate = false, rateError = "دریافت نرخ ارز ناموفق بود. لطفاً نرخ را دستی وارد کنید.") }
                    return@launch
                }
                val oldPrice = findPriceInToman(oldCurrency.code, rates)
                val newPrice = findPriceInToman(newCurrency.code, rates)
                if (oldPrice == null || newPrice == null) {
                    val missing = if (oldPrice == null) oldCurrency.code else newCurrency.code
                    _state.update { it.copy(isFetchingRate = false, rateError = "نرخ $missing در سرور موجود نیست. لطفاً نرخ را دستی وارد کنید.") }
                    return@launch
                }
                val rate = oldPrice.toDouble() / newPrice.toDouble()
                _state.update { it.copy(conversionRate = String.format("%.6f", rate), isFetchingRate = false, rateError = null) }
            } catch (e: Exception) {
                _state.update { it.copy(isFetchingRate = false, rateError = "خطا در دریافت نرخ: ${e.message}") }
            }
        }
    }

    private fun findPriceInToman(currencyCode: String, rates: List<com.kazemieh.common.model.AssetRate>): Long? {
        val code = currencyCode.lowercase()
        if (code == "irt" || code == "irr") return 1L
        return rates.find { it.code.lowercase() == code }?.price
    }

    private fun confirmAndConvert() {
        val rate = _state.value.conversionRate.toDoubleOrNull()
        if (rate == null || rate <= 0) {
            _state.update { it.copy(rateError = "نرخ تبدیل نامعتبر است.") }
            return
        }
        val newCurrency = _state.value.pendingCurrency ?: return
        val oldCurrency = _state.value.selectedCurrency
        viewModelScope.launch {
            _state.update { it.copy(isConverting = true) }
            try {
                currencyRepository.batchConvertCurrency(rate, oldCurrency.code, newCurrency.code)
                val json = Json.encodeToString(newCurrency)
                preferenceUseCases.setStringPreference(PREF_CURRENCY, json)
                _state.update {
                    it.copy(selectedCurrency = newCurrency, pendingCurrency = null, showConfirmSheet = false, showRateSheet = false, isConverting = false)
                }
                _effect.send(CurrencySettingsEffect.CurrencyChanged)
            } catch (e: Exception) {
                _state.update { it.copy(isConverting = false, rateError = "خطا در تبدیل مبالغ: ${e.message}") }
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
