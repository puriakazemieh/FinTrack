package com.kazemieh.designsystem.component.model

import platform.UIKit.UIViewController
import org.jetbrains.compose.resources.getString

actual fun UiText.asString(context: Any?): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResourceText -> {
            val viewController = context as? UIViewController
            getString(resource, viewController)
        }
    }
}