package com.kazemieh.utilities.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface SupportEffect {
    data class OpenUri(val uri: String) : SupportEffect
}

class SupportViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService
) : ViewModel() {

    init {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.FeatureOpened("support"))
    }

    private val _effect = MutableSharedFlow<SupportEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: SupportIntent) {
        viewModelScope.launch {
            when (intent) {
                SupportIntent.CallPhone -> _effect.emit(SupportEffect.OpenUri("tel:02188776655"))
                SupportIntent.SendEmail -> _effect.emit(SupportEffect.OpenUri("mailto:support@fintrack.app"))
                SupportIntent.OpenTelegram -> _effect.emit(SupportEffect.OpenUri("https://t.me/fintrack_support"))
                SupportIntent.OpenLiveChat -> _effect.emit(SupportEffect.OpenUri("https://fintrack.app/chat"))
            }
        }
    }
}
