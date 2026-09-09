package com.kazemieh.common

/**
 * Process-wide presentation flag for legacy amount formatters.
 *
 * Compose screens update it from the persisted privacy preference. Keeping it
 * beside the formatters ensures an amount cannot accidentally be disclosed by
 * an older screen that still calls [toPersianPrice] directly.
 */
object MoneyPrivacy {
    var maskAmounts: Boolean = false
}
