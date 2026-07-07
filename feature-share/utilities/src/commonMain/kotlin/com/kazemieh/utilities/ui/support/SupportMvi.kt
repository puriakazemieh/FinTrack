package com.kazemieh.utilities.ui.support

sealed interface SupportIntent {
    data object CallPhone : SupportIntent
    data object SendEmail : SupportIntent
    data object OpenTelegram : SupportIntent
    data object OpenLiveChat : SupportIntent
}
