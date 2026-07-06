package com.kazemieh.utilities.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.dp_dow_fri
import fintrack.core.designsystem.generated.resources.dp_dow_mon
import fintrack.core.designsystem.generated.resources.dp_dow_sat
import fintrack.core.designsystem.generated.resources.dp_dow_sun
import fintrack.core.designsystem.generated.resources.dp_dow_thu
import fintrack.core.designsystem.generated.resources.dp_dow_tue
import fintrack.core.designsystem.generated.resources.dp_dow_wed
import fintrack.core.designsystem.generated.resources.financial_calendar_day_empty
import fintrack.core.designsystem.generated.resources.financial_calendar_expense
import fintrack.core.designsystem.generated.resources.financial_calendar_hint
import fintrack.core.designsystem.generated.resources.financial_calendar_income
import fintrack.core.designsystem.generated.resources.financial_calendar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FinancialCalendarScreen(
    onBack: () -> Unit,
    viewModel: FinancialCalendarViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    FintrackScreen(
        title = stringResource(Res.string.financial_calendar_title),
        sub = stringResource(Res.string.financial_calendar_hint),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = space.large)) {

            MonthHeader(
                label = "${state.monthLabel} ${state.year.toString().toPersianDigits()}",
                onPrevious = { viewModel.onIntent(FinancialCalendarIntent.PreviousMonth) },
                onNext = { viewModel.onIntent(FinancialCalendarIntent.NextMonth) }
            )

            Spacer(Modifier.height(space.medium))

            val totalIncome = state.dayTotals.values.sumOf { it.income }
            val totalExpense = state.dayTotals.values.sumOf { it.expense }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space.small)
            ) {
                MonthSummaryChip(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.financial_calendar_income),
                    amount = totalIncome,
                    color = GlassGreen
                )
                MonthSummaryChip(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.financial_calendar_expense),
                    amount = totalExpense,
                    color = GlassRed
                )
            }

            Spacer(Modifier.height(space.large))

            WeekdayHeader()
            Spacer(Modifier.height(space.small))

            if (state.isLoading && state.dayTotals.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GlassGreen)
                }
            } else {
                CalendarGrid(
                    year = state.year,
                    month = state.month,
                    dayTotals = state.dayTotals,
                    selectedDay = state.selectedDay,
                    onDayClick = { viewModel.onIntent(FinancialCalendarIntent.SelectDay(it)) }
                )
            }

            Spacer(Modifier.height(space.large))

            if (state.selectedDay != null) {
                FintrackLabelMediumText(
                    text = "${state.selectedDay.toString().toPersianDigits()} ${state.monthLabel}",
                    fontWeight = FontWeight.Bold,
                    color = glassColors.text
                )
                Spacer(Modifier.height(space.small))

                if (state.selectedDayTransactions.isEmpty()) {
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.financial_calendar_day_empty),
                        color = glassColors.text3
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(space.small)
                    ) {
                        items(state.selectedDayTransactions, key = { it.transaction.id }) { item ->
                            DayTransactionRow(item = item)
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun MonthHeader(label: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = LocalGlassColors.current.text2)
        }
        FintrackBodyLargeText(text = label, fontWeight = FontWeight.Bold, color = LocalGlassColors.current.text)
        IconButton(onClick = onNext) {
            Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = null, tint = LocalGlassColors.current.text2)
        }
    }
}

@Composable
private fun MonthSummaryChip(modifier: Modifier = Modifier, label: String, amount: Long, color: androidx.compose.ui.graphics.Color) {
    GlassCard(modifier = modifier) {
        Column {
            FintrackLabelSmallText(text = label, color = LocalGlassColors.current.text3)
            Spacer(Modifier.height(4.dp))
            FintrackBodyLargeText(text = amount.toPersianPrice(), fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf(
        Res.string.dp_dow_sat, Res.string.dp_dow_sun, Res.string.dp_dow_mon, Res.string.dp_dow_tue,
        Res.string.dp_dow_wed, Res.string.dp_dow_thu, Res.string.dp_dow_fri
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { res ->
            FintrackLabelSmallText(
                text = stringResource(res),
                color = LocalGlassColors.current.text3,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    dayTotals: Map<Int, DayTotal>,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit
) {
    val firstDay = remember(year, month) { JalaliCalendar(year, month, 1) }
    val today = remember { JalaliCalendar.today() }
    val leadingBlanks = firstDay.dayOfWeek
    val cells = remember(year, month, leadingBlanks) {
        buildList {
            repeat(leadingBlanks) { add(null) }
            for (day in 1..firstDay.monthLength) add(day)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                total = dayTotals[day],
                                isToday = today.year == year && today.month == month && today.day == day,
                                isSelected = selectedDay == day,
                                onClick = { onDayClick(day) }
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    total: DayTotal?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val dotColor = when {
        total == null || !total.hasActivity -> null
        total.net > 0 -> GlassGreen
        total.net < 0 -> GlassRed
        else -> glassColors.text3
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> glassColors.accent.copy(alpha = 0.22f)
                    isToday -> glassColors.glassStrong
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FintrackLabelSmallText(
            text = day.toString().toPersianDigits(),
            color = if (isSelected) glassColors.accent else glassColors.text,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor ?: androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}

@Composable
private fun DayTransactionRow(item: com.kazemieh.common.model.TransactionWithRelations) {
    val glassColors = LocalGlassColors.current
    val trx = item.transaction
    val amountColor = when (trx.type) {
        TransactionType.EXPENSE -> GlassRed
        TransactionType.INCOME -> GlassGreen
        else -> glassColors.text2
    }
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FintrackBodyMediumText(text = item.category.name, color = glassColors.text, fontWeight = FontWeight.Bold)
                FintrackLabelSmallText(text = item.source.name, color = glassColors.text3)
            }
            FintrackBodyMediumText(
                text = trx.amount.toLong().toPersianPrice(),
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
