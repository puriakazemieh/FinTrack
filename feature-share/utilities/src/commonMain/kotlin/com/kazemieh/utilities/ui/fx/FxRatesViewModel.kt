package com.kazemieh.utilities.ui.fx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.repository.AssetRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        // The cached rates are the source of truth for what's on screen, so a failed live refresh
        // never blanks the list — the last successful snapshot stays visible.
        assetRepository.observeRates()
            .onEach { cached ->
                _state.update {
                    it.copy(
                        rates = cached,
                        lastUpdate = cached.maxOfOrNull { rate -> rate.lastUpdate }
                    )
                }
            }
            .launchIn(viewModelScope)
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
                if (rates.isEmpty()) {
                    _state.update { it.copy(error = "empty") }
                    _effect.send(FxRatesEffect.ShowError("empty"))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                _effect.send(FxRatesEffect.ShowError(e.message ?: "Unknown error"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
