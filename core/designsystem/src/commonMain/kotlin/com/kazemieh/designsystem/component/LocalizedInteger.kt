package com.kazemieh.designsystem.component

import androidx.compose.runtime.Composable
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.number_digits
import org.jetbrains.compose.resources.stringResource

/** Counts follow the same resource locale as their surrounding labels. */
@Composable
fun localizedInteger(value: Int): String {
    val digits = stringResource(Res.string.number_digits).takeIf { it.length == 10 } ?: "0123456789"
    return value.toString().map { if (it in '0'..'9') digits[it - '0'] else it }.joinToString("")
}
