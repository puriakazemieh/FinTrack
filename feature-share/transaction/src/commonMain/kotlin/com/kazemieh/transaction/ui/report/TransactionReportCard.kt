package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SummaryCard(
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        tone = GlassTone.Strong,
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
                    color = GlassText2
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.label_amount_with_unit, state.items.size.toLong().toFa(), stringResource(Res.string.unit_transaction)),
                        fontSize = 10.sp,
                        color = GlassText3
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniDonut(
                    income = state.totalIncome,
                    expense = state.totalExpense,
                    modifier = Modifier.size(72.dp)
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryRow(label = stringResource(Res.string.label_income), value = state.totalIncome, color = GlassGreen)
                    SummaryRow(label = stringResource(Res.string.label_expense), value = state.totalExpense, color = GlassRed)
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = GlassHairline)
                    SummaryRow(label = stringResource(Res.string.label_net), value = state.totalIncome - state.totalExpense, color = GlassText, isBold = true)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Long, color: Color, isBold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        FintrackLabelSmallText(text = label, color = GlassText2, modifier = Modifier.weight(1f))
        FintrackTitleSmallText(
            text = stringResource(Res.string.label_amount_with_unit, value.toFa(), stringResource(Res.string.unit_toman_short)),
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun MiniDonut(income: Long, expense: Long, modifier: Modifier = Modifier) {
    val total = (income + expense).coerceAtLeast(1)
    val incomePct = (income.toFloat() / total).coerceIn(0f, 1f)
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 12f
        drawArc(
            color = GlassHairline,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        
        // Income arc
        drawArc(
            color = GlassGreen,
            startAngle = -90f,
            sweepAngle = incomePct * 360f * 0.95f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Expense arc
        drawArc(
            color = GlassRed,
            startAngle = -90f + (incomePct * 360f * 0.95f) + 5f,
            sweepAngle = (1f - incomePct) * 360f * 0.95f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CategoryStrip(
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val topCats = state.categorySums.sortedByDescending { it.totalAmount }.take(4)
    if (topCats.isEmpty()) return
    
    val maxAmount = topCats.first().totalAmount.coerceAtLeast(1)

    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FintrackLabelMediumText(
                text = stringResource(Res.string.label_category_split),
                fontWeight = FontWeight.Bold,
                color = GlassText
            )
            
            topCats.forEach { cat ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FinTrackLeadingIcon(
                            colorId = cat.colorId,
                            iconId = cat.iconId,
                            style = LeadingIconStyle.Badge,
                            size = 24.dp,
                            iconSize = 12.dp,
                            corner = 7.dp
                        )
                        FintrackLabelSmallText(text = cat.name, color = GlassText, modifier = Modifier.weight(1f))
                        FintrackLabelSmallText(text = stringResource(Res.string.label_amount_with_unit, cat.totalAmount.toFa(), stringResource(Res.string.unit_toman_short)), fontWeight = FontWeight.Bold, color = GlassText)
                    }
                    
                    val progress = (cat.totalAmount.toFloat() / maxAmount).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(GlassHairline)
                    ) {
                        val colors = com.kazemieh.designsystem.picker.FinTrackPickerColors.rainbow()
                        val color = colors.firstOrNull { it.id == cat.colorId }?.color ?: GlassGreen
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.4f))))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShowTransactionReportCard(
    viewModel: TransactionReportViewModel = koinViewModel(),
    enableAnimationChart: Boolean = true,
) {
    // Keep this for compatibility if needed, but we prefer SummaryCard + CategoryStrip
    SummaryCard(viewModel)
}
