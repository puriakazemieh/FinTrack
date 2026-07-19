package com.kazemieh.utilities.ui.fx

import com.kazemieh.common.model.AssetRate
import kotlin.time.Instant

data class FxRatesState(
    val rates: List<AssetRate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Newest rate timestamp in [rates] — used to show "last updated" and flag stale data. */
    val lastUpdate: Instant? = null
)

sealed interface FxRatesIntent {
    data object RefreshRates : FxRatesIntent
}

sealed interface FxRatesEffect {
    data class ShowError(val message: String) : FxRatesEffect
}
