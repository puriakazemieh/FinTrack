package com.kazemieh.utilities.ui.fx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.repository.AssetRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FxRatesViewModel(
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FxRatesState())
    val state = _state.asStateFlow()

    private val _effect = Channel<FxRatesEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(FxRatesIntent.RefreshRates)
    }

    fun onIntent(intent: FxRatesIntent) {
        when (intent) {
            FxRatesIntent.RefreshRates -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val rates = assetRepository.syncRates()
                _state.update { it.copy(rates = rates, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.send(FxRatesEffect.ShowError(e.message ?: "Unknown error"))
            }
        }
    }
}
