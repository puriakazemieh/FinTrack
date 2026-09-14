package com.kazemieh.utilities.ui.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.domain.repository.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class CurrencyConverterViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyConverterState())
    val state = _state.asStateFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyConverterUsed)
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("currency_converter"))
        loadRates()
    }

    private fun loadRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyRatesRefreshRequested)
            try {
                val rates = assetRepository.syncRates().toMutableList()
                val hasMarketRates = rates.isNotEmpty()
                // All market prices are normalized to toman, so IRT is the converter base unit.
                if (rates.none { it.code == "irt" }) {
                    rates.add(0, AssetRate(AssetType.FX, "irt", "تومان", 1, Clock.System.now()))
                }
                _state.update {
                    it.copy(
                        availableRates = rates,
                        fromRate = rates.find { r -> r.code == "usd" } ?: rates.firstOrNull(),
                        toRate = rates.find { r -> r.code == "irt" } ?: rates.lastOrNull(),
                        favoritePairs = listOf(
                            FavoritePair("usd", "irt", "دلار", "تومان", 0.0, "🇺🇸"),
                            FavoritePair("eur", "irt", "یورو", "تومان", 0.0, "🇪🇺"),
                            FavoritePair("btc", "usd", "بیت‌کوین", "دلار", 0.0, "₿")
                        ),
                        isLoading = false
                    )
                }
                if (hasMarketRates) {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyRatesRefreshCompleted(rates.size))
                } else {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyRatesRefreshFailed)
                }
                calculate()
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyRatesRefreshFailed)
            }
        }
    }

    fun onIntent(intent: CurrencyConverterIntent) {
        when (intent) {
            is CurrencyConverterIntent.InputChar -> {
                _state.update { 
                    val newAmount = if (it.amount == "0") intent.char else it.amount + intent.char
                    it.copy(amount = newAmount)
                }
                calculate()
            }
            CurrencyConverterIntent.Clear -> {
                _state.update { it.copy(amount = "0") }
                calculate()
            }
            CurrencyConverterIntent.Delete -> {
                _state.update { 
                    val newAmount = if (it.amount.length > 1) it.amount.dropLast(1) else "0"
                    it.copy(amount = newAmount)
                }
                calculate()
            }
            is CurrencyConverterIntent.SelectFromRate -> {
                _state.update { it.copy(fromRate = intent.rate) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyPairSelected("from", intent.rate.code))
                calculate()
            }
            is CurrencyConverterIntent.SelectToRate -> {
                _state.update { it.copy(toRate = intent.rate) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyPairSelected("to", intent.rate.code))
                calculate()
            }
            CurrencyConverterIntent.SwapRates -> {
                _state.update { it.copy(fromRate = it.toRate, toRate = it.fromRate) }
                analytics.track(com.kazemieh.common.analytics.ProductEvent.CurrencyPairSwapped)
                calculate()
            }
            CurrencyConverterIntent.RefreshRates -> loadRates()
            is CurrencyConverterIntent.SelectQuickAmount -> {
                _state.update { it.copy(amount = intent.amount) }
                calculate()
            }
            is CurrencyConverterIntent.SelectFavoritePair -> {
                val from = _state.value.availableRates.find { it.code == intent.pair.fromCode }
                val to = _state.value.availableRates.find { it.code == intent.pair.toCode }
                if (from != null && to != null) {
                    _state.update { it.copy(fromRate = from, toRate = to) }
                    analytics.track(
                        com.kazemieh.common.analytics.ProductEvent.CurrencyFavoritePairSelected(
                            intent.pair.fromCode,
                            intent.pair.toCode
                        )
                    )
                    calculate()
                }
            }
        }
    }

    private fun calculate() {
        val amount = _state.value.amount.toDoubleOrNull() ?: 0.0
        val fromRate = _state.value.fromRate?.price ?: 1L
        val toRate = _state.value.toRate?.price ?: 1L
        
        // Simple logic: convert everything to IRR first then to target
        val result = if (toRate != 0L) (amount * fromRate) / toRate else 0.0
        _state.update { it.copy(result = result) }
    }
}
