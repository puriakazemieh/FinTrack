package com.kazemieh.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_select_date
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.btn_clear_all
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.custom_range
import fintrack.core.designsystem.generated.resources.date
import fintrack.core.designsystem.generated.resources.end
import fintrack.core.designsystem.generated.resources.label_management
import fintrack.core.designsystem.generated.resources.label_range
import fintrack.core.designsystem.generated.resources.label_this_year
import fintrack.core.designsystem.generated.resources.label_transactions_plural
import fintrack.core.designsystem.generated.resources.last_month
import fintrack.core.designsystem.generated.resources.last_week
import fintrack.core.designsystem.generated.resources.start
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.yesterday
import org.jetbrains.compose.resources.stringResource

@Composable
fun TxHeader(
    isSearchActive: Boolean,
    isFilterActive: Boolean,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            FintrackLabelSmallText(
                text = stringResource(Res.string.label_management),
                color = GlassText3
            )
            FintrackHeadlineSmallText(
                text = stringResource(Res.string.label_transactions_plural),
                fontWeight = FontWeight.Bold,
                color = GlassText
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderButton(
                icon = Icons.Default.Search,
                onClick = onSearchClick,
                isActive = isSearchActive
            )
            HeaderButton(
                icon = Icons.Default.FilterList,
                onClick = onFilterClick,
                isActive = isFilterActive,
                showDot = isFilterActive
            )
        }
    }
}

@Composable
private fun HeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false,
    showDot: Boolean = false
) {
    Box {
        GlassCard(
            modifier = Modifier.size(40.dp),
            padding = 0.dp,
            onClick = onClick,
            tone = if (isActive) GlassTone.Strong else GlassTone.Default
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) GlassGreen else GlassText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(GlassRed)
            )
        }
    }
}

@Composable
fun PeriodSelector(
    currentPeriod: DateFilterType,
    periodLabel: String,
    periodSubLabel: String,
    onPeriodSelected: (DateFilterType) -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        DateFilterType.TODAY to stringResource(Res.string.today),
        DateFilterType.THIS_WEEK to stringResource(Res.string.this_week),
        DateFilterType.THIS_MONTH to stringResource(Res.string.this_month),
        DateFilterType.THIS_YEAR to stringResource(Res.string.label_this_year),
        DateFilterType.CUSTOM_RANGE to stringResource(Res.string.label_range)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 8.dp,
            tone = GlassTone.Default
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // RTL Prev is ArrowBack
                        contentDescription = null,
                        tint = GlassText2,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FintrackBodyLargeText(
                        text = periodLabel,
                        fontWeight = FontWeight.Bold,
                        color = GlassText,
                        textAlign = TextAlign.Center
                    )
                    if (periodSubLabel.isNotEmpty()) {
                        FintrackLabelSmallText(
                            text = periodSubLabel,
                            color = GlassText3,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward, // RTL Next is ArrowForward
                        contentDescription = null,
                        tint = GlassText2,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(periods) { (type, label) ->
                val active = currentPeriod == type
                Chip(
                    active = active,
                    color = GlassGreen,
                    onClick = { onPeriodSelected(type) }
                ) {
                    FintrackLabelMediumText(
                        text = label,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) GlassBg0 else GlassText2
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveFilters(
    filters: List<FilterChipData>,
    onRemove: (FilterChipData) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (filters.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    label = filter.label,
                    color = filter.color,
                    onRemove = { onRemove(filter) }
                )
            }
        }

        FintrackLabelSmallText(
            text = stringResource(Res.string.btn_clear_all),
            color = GlassText3,
            modifier = Modifier
                .clickable(onClick = onClearAll)
                .padding(4.dp)
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    color: Color?,
    onRemove: () -> Unit
) {
    val bgColor = color?.copy(alpha = 0.12f) ?: GlassColor
    val borderColor = color?.copy(alpha = 0.33f) ?: GlassEdge

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onRemove)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FintrackLabelSmallText(
            text = label,
            color = color ?: GlassText2
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = color ?: GlassText2,
            modifier = Modifier.size(10.dp)
        )
    }
}

data class FilterChipData(
    val id: String,
    val label: String,
    val color: Color? = null,
    val type: FilterType
)

enum class FilterType { Category, Source, Tag, Person }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterBottomSheet(
    onDismiss: () -> Unit,
    onDateRange: (DateFilterType) -> Unit,
    onToggleCustomDateSheet: () -> Unit,
    startDate: String?,
    endDate: String?
) {
    SheetFrame(
        title = stringResource(Res.string.date),
        onDismiss = onDismiss,
        isFullScreen = false
    ) {
        val filters = listOf(
            DateFilterType.TODAY,
            DateFilterType.YESTERDAY,
            DateFilterType.THIS_WEEK,
            DateFilterType.LAST_WEEK,
            DateFilterType.THIS_MONTH,
            DateFilterType.LAST_MONTH,
        )

        filters.forEach { type ->
            val label = when (type) {
                DateFilterType.TODAY -> Res.string.today
                DateFilterType.YESTERDAY -> Res.string.yesterday
                DateFilterType.THIS_WEEK -> Res.string.this_week
                DateFilterType.LAST_WEEK -> Res.string.last_week
                DateFilterType.THIS_MONTH -> Res.string.this_month
                DateFilterType.LAST_MONTH -> Res.string.last_month
                else -> Res.string.all
            }
            FintrackBodyLargeText(
                text = stringResource(label).toPersianDigits(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateRange(type) }
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            )
        }

        FintrackBodyLargeText(
            text = stringResource(Res.string.custom_range),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleCustomDateSheet() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateBottomSheet(
    onDismiss: () -> Unit,
    start: Pair<String?, Long?>?,
    end: Pair<String?, Long?>?,
    isError: Boolean,
    onSubmit: (Pair<String?, Long?>?, Pair<String?, Long?>?) -> Unit
) {
    var startDate by remember { mutableStateOf(start) }
    var endDate by remember { mutableStateOf(end) }

    val showDatePicker = remember { mutableStateOf(false) }
    var isSelectingStart by remember { mutableStateOf(true) }

    SheetFrame(
        title = stringResource(Res.string.custom_range),
        onDismiss = onDismiss,
        isFullScreen = false
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateInput(
                    label = stringResource(Res.string.start),
                    value = (startDate?.first ?: "").toPersianDigits(),
                    isError = isError && startDate == null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isSelectingStart = true
                        showDatePicker.value = true
                    }
                )
                DateInput(
                    label = stringResource(Res.string.end),
                    value = (endDate?.first ?: "").toPersianDigits(),
                    isError = isError && endDate == null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isSelectingStart = false
                        showDatePicker.value = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 0.dp,
                onClick = { onSubmit(startDate, endDate) },
                tone = GlassTone.Strong
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FintrackTitleMediumText(
                        text = stringResource(Res.string.confirm),
                        color = GlassGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDatePicker.value) {
        val initial = if (isSelectingStart) startDate?.second else endDate?.second
        val calendar = initial?.let { JalaliCalendar.fromEpochMilliseconds(it) } ?: JalaliCalendar()

        com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet(
            openSheet = showDatePicker,
            initialDate = calendar,
            onConfirm = { jalali ->
                val pair = jalali.toString() to jalali.toTimestamp()
                if (isSelectingStart) startDate = pair else endDate = pair
                showDatePicker.value = false
            }
        )
    }
}

@Composable
private fun DateInput(
    label: String,
    value: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val displayValue = remember(value) {
        if (value.isEmpty()) ""
        else {
            val parts = value.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1].toIntOrNull() ?: 0
                val day = parts[2]
                val pdt = com.kazemieh.common.persiandatetime.domain.PersianDateTime(year.toInt(), month, day.toInt())
                "${day.toPersianDigits()} ${pdt.persianMonth().displayName} ${year.toPersianDigits()}"
            } else value
        }
    }

    Column(modifier = modifier) {
        FintrackLabelMediumText(text = label, color = GlassText3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else GlassColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            FintrackBodyMediumText(
                text = displayValue.ifEmpty { stringResource(Res.string.action_select_date) },
                color = if (displayValue.isEmpty()) GlassText3 else GlassText
            )
        }
    }
}
