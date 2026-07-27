package com.kazemieh.budget.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.persiandatetime.extensions.dayOfWeekIndex
import com.kazemieh.common.persiandatetime.extensions.isLeapYear
import com.kazemieh.common.persiandatetime.extensions.monthLength
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.*
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun BudgetHero(
    totalBudget: Long,
    usedBudget: Long,
    period: BudgetPeriod,
    modifier: Modifier = Modifier
) {
    val progress = if (totalBudget > 0) usedBudget.toFloat() / totalBudget else 0f
    val remaining = (totalBudget - usedBudget).coerceAtLeast(0)
    val daysRemaining = remember(period) { calculateDaysRemaining(period) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        padding = 18.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(116.dp)) {
                CircularProgress(progress = progress)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FintrackTitleLargeText(
                        text = (progress * 100).toInt().toLong().toPersianDigits(),
                        fontWeight = FontWeight.Bold
                    )
                    FintrackLabelSmallText(text = stringResource(Res.string.label_budget_consumed))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                FintrackLabelSmallText(text = stringResource(Res.string.label_total_budget))
                MoneyText(amount = totalBudget, size = 20)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FintrackLabelSmallText(text = stringResource(Res.string.label_remaining))
                MoneyText(amount = remaining, size = 16, color = GlassGreen)
                
                Spacer(modifier = Modifier.height(6.dp))
                FintrackLabelSmallText(
                    text = stringResource(Res.string.label_days_remaining, daysRemaining.toLong().toPersianDigits()),
                    color = GlassAmber
                )
            }
        }
    }
}

private fun calculateDaysRemaining(period: BudgetPeriod): Int {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val pNow = now.toPersianDateTime(timeZone)

    return when (period) {
        BudgetPeriod.DAILY -> 1
        BudgetPeriod.WEEKLY -> {
            val daysToSaturday = (pNow.dayOfWeekIndex) % 7
            7 - daysToSaturday
        }
        BudgetPeriod.MONTHLY -> {
            pNow.monthLength() - pNow.day + 1
        }
        BudgetPeriod.YEARLY -> {
            // Simplified: calculate days until end of year
            val daysInMonths = if (pNow.isLeapYear()) {
                listOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 30)
            } else {
                listOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
            }
            var remaining = daysInMonths[pNow.month - 1] - pNow.day + 1
            for (m in pNow.month until 12) {
                remaining += daysInMonths[m]
            }
            remaining
        }
    }
}

@Composable
fun BudgetPeriodSelector(
    selectedPeriod: BudgetPeriod,
    onPeriodSelected: (BudgetPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(padding = 14.dp, modifier = modifier) {
        Column {
            FintrackLabelMediumText(text = stringResource(Res.string.label_budget_period))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BudgetPeriod.entries.forEach { period ->
                    val labelRes = when (period) {
                        BudgetPeriod.DAILY -> Res.string.label_budget_period_daily
                        BudgetPeriod.WEEKLY -> Res.string.label_budget_period_weekly
                        BudgetPeriod.MONTHLY -> Res.string.label_budget_period_monthly
                        BudgetPeriod.YEARLY -> Res.string.label_budget_period_yearly
                    }
                    Chip(
                        active = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        modifier = Modifier.weight(1f)
                    ) {
                        FintrackLabelSmallText(text = stringResource(labelRes))
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetRow(
    budgetProgress: BudgetWithProgress,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val color = FinTrackPickerColors.getColorById(budgetProgress.category?.colorId ?: 1, isDark)
    val icon = FinTrackIcons.findIcon(budgetProgress.category?.iconId ?: 1).resource
    val progress = budgetProgress.progress.coerceIn(0f, 1f)
    val isOver = budgetProgress.progress > 1f

    GlassCard(
        modifier = modifier.fillMaxWidth().clickable { onEdit() },
        padding = 8.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Row 1: Icon + Name ... Spent of Total
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(color.copy(alpha = 0.15f))
                        .border(0.5.dp, color.copy(alpha = 0.25f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                }

                FintrackLabelMediumText(
                    text = budgetProgress.category?.name ?: stringResource(Res.string.label_unknown),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp
                )

                FintrackLabelSmallText(
                    text = stringResource(
                        Res.string.label_spent_of_budget,
                        budgetProgress.spentAmount.toPersianPrice(),
                        budgetProgress.budget.amount.toPersianPrice()
                    ),
                    color = if (isOver) GlassRed else LocalGlassColors.current.text3,
                    fontSize = 10.sp
                )
            }

            // Row 2: Progress Bar + Percentage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(LocalGlassColors.current.text.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f))))
                    )
                }

                FintrackLabelSmallText(
                    text = (budgetProgress.progress * 100).toInt().toLong().toPersianDigits() + "٪",
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) GlassRed else LocalGlassColors.current.text,
                    fontSize = 9.sp
                )
            }

            if (isOver) {
                Row(
                    modifier = Modifier.padding(top = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(GlassRed))
                    FintrackLabelSmallText(text = stringResource(Res.string.label_budget_over), color = GlassRed, fontSize = 8.sp)
                }
            }
        }
    }
}
