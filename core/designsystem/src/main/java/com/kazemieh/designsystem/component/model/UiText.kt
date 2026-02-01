package com.kazemieh.designsystem.component.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource


sealed class UiText {
    data class DynamicString(val text: String) : UiText()
    data class StringResource(@StringRes val resId: Int) : UiText()
}

@Composable
fun UiText.asString(): String =
    when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> stringResource(resId)
    }

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> context.getString(resId)
    }
}