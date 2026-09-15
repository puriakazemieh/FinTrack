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
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FxRatesState())
    val state = _state.asStateFlow()

    private val _effect = Channel<FxRatesEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("fx_rates"))
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FxRatesViewed)
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
            is FxRatesIntent.SelectRate -> selectRate(intent.rate)
            FxRatesIntent.DismissRateDetails -> _state.update {
                it.copy(selectedRate = null, selectedRateHistory = emptyList())
            }
        }
    }

    private fun selectRate(rate: com.kazemieh.common.model.AssetRate) {
        _state.update { it.copy(selectedRate = rate, selectedRateHistory = emptyList()) }
        assetRepository.observeRateHistory(rate.code)
            .onEach { history ->
                _state.update { current ->
                    if (current.selectedRate?.code == rate.code) current.copy(selectedRateHistory = history) else current
                }
            }
            .launchIn(viewModelScope)
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            analytics.track(com.kazemieh.common.analytics.ProductEvent.FxRatesRefreshRequested)
            try {
                val rates = assetRepository.syncRates()
                if (rates.isEmpty()) {
                    _state.update { it.copy(error = "empty") }
                    _effect.send(FxRatesEffect.ShowError("empty"))
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.FxRatesRefreshFailed)
                } else {
                    analytics.track(com.kazemieh.common.analytics.ProductEvent.FxRatesRefreshCompleted(rates.size))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                _effect.send(FxRatesEffect.ShowError(e.message ?: "Unknown error"))
                analytics.track(com.kazemieh.common.analytics.ProductEvent.FxRatesRefreshFailed)
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
