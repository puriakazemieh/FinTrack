package com.kazemieh.utilities.ui.fx

import com.kazemieh.common.model.AssetRate

data class FxRatesState(
    val rates: List<AssetRate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface FxRatesIntent {
    data object RefreshRates : FxRatesIntent
}

sealed interface FxRatesEffect {
    data class ShowError(val message: String) : FxRatesEffect
}
