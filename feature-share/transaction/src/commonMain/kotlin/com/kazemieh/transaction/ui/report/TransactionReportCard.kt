package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassHairline
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_amount_with_unit
import fintrack.core.designsystem.generated.resources.label_cashflow_calendar
import fintrack.core.designsystem.generated.resources.weekday_initials
import fintrack.core.designsystem.generated.resources.label_expense
import fintrack.core.designsystem.generated.resources.label_income
import fintrack.core.designsystem.generated.resources.label_monthly_trend
import fintrack.core.designsystem.generated.resources.label_net
import fintrack.core.designsystem.generated.resources.label_summary_period
import fintrack.core.designsystem.generated.resources.type_transfer
import fintrack.core.designsystem.generated.resources.unit_toman_short
import fintrack.core.designsystem.generated.resources.unit_transaction
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SummaryCard(
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val glassColors = LocalGlassColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackLabelMediumText(
                    text = stringResource(Res.string.label_summary_period),
                    color = glassColors.text2
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(glassColors.glass)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    FintrackLabelSmallText(
                        text = stringResource(
                            Res.string.label_amount_with_unit,
                            state.items.size.toLong().toPersianDigits(),
                            stringResource(Res.string.unit_transaction)
                        ),
                        fontSize = 10.sp,
                        color = glassColors.text3
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val filterType = state.filterParams.type
            if (filterType == null || filterType == TransactionType.ALL.count) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MiniDonut(
                        income = state.totalIncome,
                        expense = state.totalExpense,
                        transfer = state.totalTransfer,
                        modifier = Modifier.size(72.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(
                            label = stringResource(Res.string.label_income),
                            value = state.totalIncome,
                            color = GlassGreen
                        )
                        SummaryRow(
                            label = stringResource(Res.string.label_expense),
                            value = state.totalExpense,
                            color = GlassRed
                        )
                        SummaryRow(
                            label = stringResource(Res.string.type_transfer),
                            value = state.totalTransfer,
                            color = GlassBlue
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp),
                            color = GlassHairline
                        )
                        SummaryRow(
                            label = stringResource(Res.string.label_net),
                            valueStr = state.balance + " " + stringResource(Res.string.unit_toman_short),
                            color = if (state.isPositiveBalance) GlassGreen else GlassRed,
                            isBold = true
                        )
                    }
                }
            } else {
                val (label, value, color) = when (filterType) {
                    TransactionType.INCOME.count -> Triple(
                        stringResource(Res.string.label_income),
                        state.totalIncome,
                        GlassGreen
                    )

                    TransactionType.EXPENSE.count -> Triple(
                        stringResource(Res.string.label_expense),
                        state.totalExpense,
                        GlassRed
                    )

                    TransactionType.TRANSFER.count -> Triple(
                        stringResource(Res.string.type_transfer),
                        state.totalTransfer,
                        GlassBlue
                    )

                    else -> Triple("", 0L, glassColors.text)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        FintrackTitleSmallText(text = label, color = glassColors.text)
                    }
                    FintrackTitleLargeText(
                        text = stringResource(
                            Res.string.label_amount_with_unit,
                            value.toPersianPrice(),
                            stringResource(Res.string.unit_toman_short)
                        ),
                        color = color,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Long = 0,
    valueStr: String? = null,
    color: Color,
    isBold: Boolean = false
) {
    val glassColors = LocalGlassColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        FintrackLabelSmallText(
            text = label,
            color = glassColors.text2,
            modifier = Modifier.weight(1f)
        )
        val text = valueStr ?: stringResource(
            Res.string.label_amount_with_unit,
            value.toPersianPrice(),
            stringResource(Res.string.unit_toman_short)
        )
        FintrackTitleSmallText(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.W700 else FontWeight.W600,
            color = color
        )
    }
}

@Composable
private fun MiniDonut(income: Long, expense: Long, transfer: Long, modifier: Modifier = Modifier) {
    val glassColors = LocalGlassColors.current
    val total = (income + expense + transfer).coerceAtLeast(1)
    val incomePct = (income.toFloat() / total).coerceIn(0f, 1f)
    val expensePct = (expense.toFloat() / total).coerceIn(0f, 1f)
    val transferPct = (transfer.toFloat() / total).coerceIn(0f, 1f)

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 12f
        drawArc(
            color = glassColors.glassHairline,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        var currentAngle = -90f

        // Income arc
        if (incomePct > 0) {
            drawArc(
                color = GlassGreen,
                startAngle = currentAngle,
                sweepAngle = incomePct * 360f * 0.95f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += (incomePct * 360f * 0.95f) + (if (expensePct > 0 || transferPct > 0) 5f else 0f)
        }

        // Expense arc
        if (expensePct > 0) {
            drawArc(
                color = GlassRed,
                startAngle = currentAngle,
                sweepAngle = expensePct * 360f * 0.95f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += (expensePct * 360f * 0.95f) + (if (transferPct > 0) 5f else 0f)
        }

        // Transfer arc
        if (transferPct > 0) {
            drawArc(
                color = GlassBlue,
                startAngle = currentAngle,
                sweepAngle = transferPct * 360f * 0.95f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun CategoryStrip(
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val glassColors = LocalGlassColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val topCats = state.categorySums.sortedByDescending { it.totalAmount }
    if (topCats.isEmpty()) return

    val maxAmount = topCats.first().totalAmount.coerceAtLeast(1)

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            topCats.forEach { cat ->
                val sign = if (cat.type == TransactionType.INCOME) "+" else "-"
                val isIncome = cat.type == TransactionType.INCOME
                val isTransfer = cat.type == TransactionType.TRANSFER
                val amountColor = when {
                    isIncome -> GlassGreen
                    isTransfer -> GlassBlue
                    else -> GlassRed
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FinTrackLeadingIcon(
                            colorId = cat.colorId,
                            iconId = cat.iconId,
                            style = LeadingIconStyle.Badge,
                            size = 24.dp,
                            iconSize = 12.dp,
                            corner = 7.dp
                        )
                        FintrackLabelSmallText(
                            text = cat.name,
                            color = glassColors.text,
                            modifier = Modifier.weight(1f)
                        )
                        FintrackLabelSmallText(
                            text = stringResource(
                                Res.string.label_amount_with_unit,
                                cat.totalAmount.toPersianPrice(),
                                stringResource(Res.string.unit_toman_short)
                            ),
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    }

                    val progress = (cat.totalAmount.toFloat() / maxAmount).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(glassColors.glassHairline)
                    ) {
                        val colors = com.kazemieh.designsystem.picker.FinTrackPickerColors.rainbow()
                        val color = colors.firstOrNull { it.id == cat.colorId }?.color ?: GlassGreen

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            color,
                                            color.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Grouped income/expense bars for the last few Persian months, so the user can see the trend at a
 * glance. Hidden until there is at least one month with activity. Built with plain layout (bar
 * heights via fractional fillMaxHeight) rather than a Canvas, so it stays theme-aware and cheap.
 */
@Composable
fun MonthlyTrendCard(
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val glassColors = LocalGlassColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val trend = state.monthlyTrend
    if (trend.isEmpty() || trend.all { it.income == 0L && it.expense == 0L }) return

    val maxValue = trend.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1L)

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FintrackLabelMediumText(
                text = stringResource(Res.string.label_monthly_trend),
                color = glassColors.text2
            )

            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEach { point ->
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            TrendBar(
                                fraction = point.income.toFloat() / maxValue,
                                color = GlassGreen,
                                modifier = Modifier.weight(1f)
                            )
                            TrendBar(
                                fraction = point.expense.toFloat() / maxValue,
                                color = GlassRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        FintrackLabelSmallText(
                            text = point.label,
                            fontSize = 10.sp,
                            color = glassColors.text3
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TrendLegend(color = GlassGreen, label = stringResource(Res.string.label_income))
                TrendLegend(color = GlassRed, label = stringResource(Res.string.label_expense))
            }
        }
    }
}

@Composable
private fun TrendBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    // Give any non-zero month a visible sliver so a tiny month isn't indistinguishable from empty.
    val f = fraction.coerceIn(0f, 1f)
    val height = if (f > 0f) f.coerceAtLeast(0.03f) else 0f
    Box(
        modifier = modifier
            .fillMaxHeight(height)
            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            .background(color)
    )
}

@Composable
private fun TrendLegend(color: Color, label: String) {
    val glassColors = LocalGlassColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        FintrackLabelSmallText(text = label, fontSize = 11.sp, color = glassColors.text3)
    }
}

/**
 * A calendar of the current Persian month where each day is tinted by its net cashflow — green when
 * income beat expense, red when it didn't. Gives an at-a-glance read of which days cost the user.
 */
@Composable
fun CashflowCalendarCard(
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val glassColors = LocalGlassColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cashflow = state.monthlyCashflow ?: return
    if (cashflow.netByDay.isEmpty()) return

    val weekdays = stringResource(Res.string.weekday_initials).split(",")
    // Leading blanks for the offset, then one cell per day, padded to a full final week.
    val totalCells = cashflow.firstWeekdayOffset + cashflow.daysInMonth
    val rows = ((totalCells + 6) / 7)

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FintrackLabelMediumText(
                text = stringResource(Res.string.label_cashflow_calendar, cashflow.monthLabel),
                color = glassColors.text2
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdays.forEach { wd ->
                    FintrackLabelSmallText(
                        text = wd,
                        fontSize = 10.sp,
                        color = glassColors.text3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - cashflow.firstWeekdayOffset + 1
                        if (day in 1..cashflow.daysInMonth) {
                            CashflowDayCell(
                                day = day,
                                net = cashflow.netByDay[day],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CashflowDayCell(day: Int, net: Long?, modifier: Modifier = Modifier) {
    val glassColors = LocalGlassColors.current
    val (bg, fg) = when {
        net == null || net == 0L -> glassColors.glassHairline.copy(alpha = 0.4f) to glassColors.text3
        net > 0L -> GlassGreen.copy(alpha = 0.18f) to GlassGreen
        else -> GlassRed.copy(alpha = 0.18f) to GlassRed
    }
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        FintrackLabelSmallText(
            text = day.toString().toPersianDigits(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

