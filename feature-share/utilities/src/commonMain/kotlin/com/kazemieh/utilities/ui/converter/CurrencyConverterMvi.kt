package com.kazemieh.utilities.ui.converter

import com.kazemieh.common.model.AssetRate

data class CurrencyConverterState(
    val amount: String = "0",
    val fromRate: AssetRate? = null,
    val toRate: AssetRate? = null,
    val availableRates: List<AssetRate> = emptyList(),
    val favoritePairs: List<FavoritePair> = emptyList(),
    val result: Double = 0.0,
    val isLoading: Boolean = false
)

data class FavoritePair(
    val fromCode: String,
    val toCode: String,
    val fromName: String,
    val toName: String,
    val rate: Double,
    val flag: String? = null
)

sealed interface CurrencyConverterIntent {
    data class InputChar(val char: String) : CurrencyConverterIntent
    data class SelectQuickAmount(val amount: String) : CurrencyConverterIntent
    data class SelectFavoritePair(val pair: FavoritePair) : CurrencyConverterIntent
    data object Clear : CurrencyConverterIntent
    data object Delete : CurrencyConverterIntent
    data class SelectFromRate(val rate: AssetRate) : CurrencyConverterIntent
    data class SelectToRate(val rate: AssetRate) : CurrencyConverterIntent
    data object SwapRates : CurrencyConverterIntent
}

sealed interface CurrencyConverterEffect {
    data class ShowError(val message: String) : CurrencyConverterEffect
}
