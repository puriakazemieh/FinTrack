package com.kazemieh.designsystem.component.glass

import androidx.compose.runtime.Composable
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRangeLabel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.custom_range
import fintrack.core.designsystem.generated.resources.label_this_year
import fintrack.core.designsystem.generated.resources.last_month
import fintrack.core.designsystem.generated.resources.last_week
import fintrack.core.designsystem.generated.resources.last_year
import fintrack.core.designsystem.generated.resources.next_month
import fintrack.core.designsystem.generated.resources.next_week
import fintrack.core.designsystem.generated.resources.next_year
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.tomorrow
import fintrack.core.designsystem.generated.resources.yesterday
import org.jetbrains.compose.resources.stringResource

/**
 * Resolves a [DateRangeLabel] to a user-facing string. Covers EVERY [DateFilterType] so that
 * navigating to an adjacent period (e.g. tapping "previous" from "این هفته") shows the correct
 * label ("هفته قبل") instead of falling back to "همه".
 */
@Composable
fun dateRangeLabelText(label: DateRangeLabel?): String = when (label) {
    is DateRangeLabel.Text -> label.value
    is DateRangeLabel.Filter -> when (label.type) {
        DateFilterType.TODAY -> stringResource(Res.string.today)
        DateFilterType.YESTERDAY -> stringResource(Res.string.yesterday)
        DateFilterType.TOMORROW -> stringResource(Res.string.tomorrow)
        DateFilterType.THIS_WEEK -> stringResource(Res.string.this_week)
        DateFilterType.LAST_WEEK -> stringResource(Res.string.last_week)
        DateFilterType.NEXT_WEEK -> stringResource(Res.string.next_week)
        DateFilterType.THIS_MONTH -> stringResource(Res.string.this_month)
        DateFilterType.LAST_MONTH -> stringResource(Res.string.last_month)
        DateFilterType.NEXT_MONTH -> stringResource(Res.string.next_month)
        DateFilterType.THIS_YEAR -> stringResource(Res.string.label_this_year)
        DateFilterType.LAST_YEAR -> stringResource(Res.string.last_year)
        DateFilterType.NEXT_YEAR -> stringResource(Res.string.next_year)
        DateFilterType.CUSTOM_RANGE -> stringResource(Res.string.custom_range)
    }
    null -> ""
}
