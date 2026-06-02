package com.kazemieh.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.DateFilterType
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.jalali.JalaliCalendar
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerDialog
import fintrack.core.designsystem.generated.resources.*
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
            Text(
                text = "مدیریت",
                style = MaterialTheme.typography.labelSmall,
                color = GlassText3
            )
            Text(
                text = "تراکنش‌ها",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GlassText,
                letterSpacing = (-0.5).sp
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
                    .background(GlassGreen)
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
        DateFilterType.TODAY to "روز",
        DateFilterType.THIS_WEEK to "هفته",
        DateFilterType.THIS_MONTH to "ماه",
        DateFilterType.NEXT_MONTH to "سال", // Simplified mapping for now
        DateFilterType.CUSTOM_RANGE to "بازه"
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward, // RTL Arrow
                        contentDescription = null,
                        tint = GlassText2,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = GlassText
                    )
                    Text(
                        text = periodSubLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassText3
                    )
                }

                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // RTL Arrow
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
                    Text(
                        text = label,
                        fontSize = 12.sp,
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
        
        Text(
            text = "پاک کردن همه",
            style = MaterialTheme.typography.labelSmall,
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
        Text(
            text = label,
            fontSize = 11.sp,
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
        onDismiss = onDismiss
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
            Text(
                text = stringResource(label),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateRange(type) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Text(
            text = stringResource(Res.string.custom_range),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleCustomDateSheet() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
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
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateInput(
                    label = stringResource(Res.string.start),
                    value = startDate?.first ?: "",
                    isError = isError && startDate == null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isSelectingStart = true
                        showDatePicker.value = true
                    }
                )
                DateInput(
                    label = stringResource(Res.string.end),
                    value = endDate?.first ?: "",
                    isError = isError && endDate == null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isSelectingStart = false
                        showDatePicker.value = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSubmit(startDate, endDate) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.confirm))
            }
        }
    }

    if (showDatePicker.value) {
        val initial = if (isSelectingStart) startDate?.second else endDate?.second
        val calendar = initial?.let { JalaliCalendar.fromEpochMilliseconds(it) } ?: JalaliCalendar()
        
        JalaliDatePickerDialog(
            openDialog = showDatePicker,
            initialDate = calendar,
            onSelectDay = { jalali ->
                val pair = jalali.toString() to jalali.toTimestamp()
                if (isSelectingStart) startDate = pair else endDate = pair
                showDatePicker.value = false
            },
            onConfirm = { showDatePicker.value = false }
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
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = GlassText3)
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
            Text(
                text = value.ifEmpty { "انتخاب تاریخ" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isEmpty()) GlassText3 else GlassText
            )
        }
    }
}
