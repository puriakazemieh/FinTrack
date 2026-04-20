package com.kazemieh.designsystem.component.model

import org.jetbrains.compose.resources.getString


actual fun UiText.asString(context: Any?): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResourceText -> {
            // در دسکتاپ از stringResource استفاده کنید
            getString(resource)
        }
    }
}