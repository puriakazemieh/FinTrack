package com.kazemieh.utilities.ui.fx

import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.MarketRateHistory
import kotlin.time.Instant

data class FxRatesState(
    val rates: List<AssetRate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRate: AssetRate? = null,
    val selectedRateHistory: List<MarketRateHistory> = emptyList(),
    /** Newest rate timestamp in [rates] — used to show "last updated" and flag stale data. */
    val lastUpdate: Instant? = null
)

sealed interface FxRatesIntent {
    data object RefreshRates : FxRatesIntent
    data class SelectRate(val rate: AssetRate) : FxRatesIntent
    data object DismissRateDetails : FxRatesIntent
}

sealed interface FxRatesEffect {
    data class ShowError(val message: String) : FxRatesEffect
}
