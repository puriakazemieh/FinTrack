package com.kazemieh.designsystem

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * When true, monetary values rendered through shared money components (e.g. [com.kazemieh.designsystem.component.glass.MoneyText])
 * are masked so balances are not visible on screen. Provided at the app root from the
 * user's "hide balance" preference.
 */
val LocalHideBalance = staticCompositionLocalOf { false }
