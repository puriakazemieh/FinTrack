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
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyConverterState())
    val state = _state.asStateFlow()

    init {
        loadRates()
    }

    private fun loadRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val rates = assetRepository.syncRates().toMutableList()
            
            // Add IRR (Toman) as base rate if not present
            if (rates.none { it.code == "irr" }) {
                rates.add(0, AssetRate(AssetType.FX, "irr", "تومان", 1, Clock.System.now()))
            }
            
            _state.update { 
                it.copy(
                    availableRates = rates,
                    fromRate = rates.find { r -> r.code == "usd" } ?: rates.firstOrNull(),
                    toRate = rates.find { r -> r.code == "irr" } ?: rates.lastOrNull(),
                    isLoading = false
                )
            }
            calculate()
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
                calculate()
            }
            is CurrencyConverterIntent.SelectToRate -> {
                _state.update { it.copy(toRate = intent.rate) }
                calculate()
            }
            CurrencyConverterIntent.SwapRates -> {
                _state.update { it.copy(fromRate = it.toRate, toRate = it.fromRate) }
                calculate()
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
