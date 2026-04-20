package com.kazemieh.designsystem.component.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString


sealed class UiText {
    data class DynamicString(val text: String) : UiText()
    data class StringResourceText(val resource: StringResource) : UiText()
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResourceText -> stringResource(resource)
    }
}

suspend fun UiText.resolveString(): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResourceText -> getString(resource)
    }
}