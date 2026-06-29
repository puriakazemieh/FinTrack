package com.kazemieh.backup_export.ui

import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class DateRange(val labelRes: StringResource) {
    THIS_MONTH(Res.string.range_this_month),
    THREE_MONTHS(Res.string.range_3_months),
    SIX_MONTHS(Res.string.range_6_months),
    THIS_YEAR(Res.string.range_this_year),
    ALL(Res.string.range_all);

    companion object {
        fun fromLabel(label: String): DateRange {
            return when (label) {
                "این ماه" -> THIS_MONTH
                "۳ ماه" -> THREE_MONTHS
                "۶ ماه" -> SIX_MONTHS
                "امسال" -> THIS_YEAR
                else -> ALL
            }
        }
    }
}
