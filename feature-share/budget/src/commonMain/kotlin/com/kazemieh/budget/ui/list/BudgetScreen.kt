package com.kazemieh.budget.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.add.AddBudgetBottomSheet
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Category
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.automirrored.filled.ReceiptLong

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToTransactions: ((Category) -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val totalBudget = (state.dailyBudgets + state.weeklyBudgets + state.monthlyBudgets + state.yearlyBudgets).sumOf { it.budget.amount }
    val totalSpent = (state.dailyBudgets + state.weeklyBudgets + state.monthlyBudgets + state.yearlyBudgets).sumOf { it.spentAmount }
    val totalRemaining = (totalBudget - totalSpent).coerceAtLeast(0)

    val displayLabel = dateRangeLabelText(state.dateRange?.label)

    FintrackScreen(
        title = stringResource(Res.string.title_my_budgets),
        sub = displayLabel,
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            PeriodNavigator(
                currentPeriod = state.dateRange?.filterType ?: DateFilterType.THIS_MONTH,
                periodLabel = displayLabel,
                periodSubLabel = "",
                onPeriodSelected = { viewModel.onIntent(BudgetIntent.ChangeFilterType(it)) },
                onPrevClick = { viewModel.onIntent(BudgetIntent.ShiftRange(com.kazemieh.common.Direction.PREVIOUS)) },
                onNextClick = { viewModel.onIntent(BudgetIntent.ShiftRange(com.kazemieh.common.Direction.NEXT)) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                excludeCustomRange = true
            )

            SummaryHeader(
                totalBudget = totalBudget,
                totalSpent = totalSpent,
                totalRemaining = totalRemaining
            )

            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(BudgetIntent.UpdateSearchQuery(it)) },
                placeholder = stringResource(Res.string.hint_search_in, stringResource(Res.string.title_my_budgets)),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // DAILY SECTION
                if (state.dailyBudgets.isNotEmpty() || state.canCloneDaily) {
                    stickyHeader {
                        SectionHeader(
                            title = stringResource(Res.string.label_budget_period_daily),
                            cloneLabel = if (state.canCloneDaily) stringResource(Res.string.budget_clone_daily) else null,
                            onClone = { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.DAILY)) }
                        )
                    }
                    items(state.dailyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }

                // WEEKLY SECTION
                if (state.weeklyBudgets.isNotEmpty() || state.canCloneWeekly) {
                    stickyHeader {
                        SectionHeader(
                            title = stringResource(Res.string.label_budget_period_weekly),
                            cloneLabel = if (state.canCloneWeekly) stringResource(Res.string.budget_clone_weekly) else null,
                            onClone = { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.WEEKLY)) }
                        )
                    }
                    items(state.weeklyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }

                // MONTHLY SECTION
                if (state.monthlyBudgets.isNotEmpty() || state.canCloneMonthly) {
                    stickyHeader {
                        SectionHeader(
                            title = stringResource(Res.string.label_budget_period_monthly),
                            cloneLabel = if (state.canCloneMonthly) stringResource(Res.string.budget_clone_monthly) else null,
                            onClone = { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.MONTHLY)) }
                        )
                    }
                    items(state.monthlyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }

                // YEARLY SECTION
                if (state.yearlyBudgets.isNotEmpty() || state.canCloneYearly) {
                    stickyHeader {
                        SectionHeader(
                            title = stringResource(Res.string.label_budget_period_yearly),
                            cloneLabel = if (state.canCloneYearly) stringResource(Res.string.budget_clone_yearly) else null,
                            onClone = { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.YEARLY)) }
                        )
                    }
                    items(state.yearlyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }
            }
        }

        Fab(
            label = stringResource(Res.string.label_add_specific_new_entity, stringResource(Res.string.label_new_budget)),
            icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Add),
            onClick = { viewModel.onIntent(BudgetIntent.ShowAddBudget()) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )

        if (state.isAddBudgetShow) {
            AddBudgetBottomSheet(
                budgetWithProgress = state.selectedBudget,
                defaultStartAt = state.dateRange?.start,
                defaultRangeEnd = state.dateRange?.end,
                onDismiss = { viewModel.onIntent(BudgetIntent.ShowAddBudget()) }
            )
        }

        state.pendingDeleteBudget?.let { pending ->
            DeleteBottomSheet(
                itemName = pending.category?.name ?: "",
                dismissClicked = { viewModel.onIntent(BudgetIntent.CancelDeleteBudget) },
                confirmClicked = { viewModel.onIntent(BudgetIntent.ConfirmDeleteBudget) }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    cloneLabel: String? = null,
    onClone: () -> Unit = {}
) {
    val glassColors = LocalGlassColors.current
    Row(
        modifier = Modifier
            .stickyHeaderSurface()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(text = title, fontWeight = FontWeight.Bold, color = glassColors.text)
        if (cloneLabel != null) {
            CloneChip(label = cloneLabel, onClick = onClone)
        }
    }
}

@Composable
private fun CloneChip(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GlassGreen.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            tint = GlassGreen,
            modifier = Modifier.size(13.dp)
        )
        FintrackLabelSmallText(
            text = label,
            color = GlassGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BudgetRow(
    item: BudgetWithProgress,
    viewModel: BudgetViewModel,
    onNavigateToTransactions: ((Category) -> Unit)?
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val glassColors = LocalGlassColors.current
    val color = FinTrackPickerColors.getColorById(item.category?.colorId ?: 1, isDark)
    val icon = FinTrackIcons.findIcon(item.category?.iconId ?: 1).resource
    val progress = (item.spentAmount.toFloat() / item.budget.amount).coerceIn(0f, 1f)
    val isOver = item.progress > 1f

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        padding = 8.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Icon + Name ... Spent of Total
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(color.copy(alpha = 0.15f))
                        .border(0.5.dp, color.copy(alpha = 0.25f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                }

                FintrackLabelMediumText(
                    text = item.category?.name ?: stringResource(Res.string.label_unknown),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                FintrackLabelSmallText(
                    text = stringResource(
                        Res.string.label_spent_of_budget,
                        item.spentAmount.toPersianPrice(),
                        item.budget.amount.toPersianPrice()
                    ),
                    color = if (isOver) GlassRed else LocalGlassColors.current.text3,
                    fontSize = 11.sp
                )
            }

            // Row 2: Progress Bar + Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
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
                    text = (item.progress * 100).toInt().toLong().toPersianDigits() + "٪",
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) GlassRed else LocalGlassColors.current.text,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    onNavigateToTransactions?.let { callback ->
                        ActionIcon(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            onClick = { item.category?.let { callback(it) } },
                            color = GlassGreen
                        )
                    }
                    ActionIcon(
                        icon = Icons.Default.Edit,
                        onClick = { viewModel.onIntent(BudgetIntent.ShowAddBudget(item)) },
                        color = glassColors.text2
                    )
                    ActionIcon(
                        icon = Icons.Default.Delete,
                        onClick = { viewModel.onIntent(BudgetIntent.DeleteBudget(item)) },
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryHeader(totalBudget: Long, totalSpent: Long, totalRemaining: Long) {
    val glassColors = LocalGlassColors.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        tone = GlassTone.Strong,
        padding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem(stringResource(Res.string.title_my_budgets), totalBudget.toPersianPrice())
            SummaryItem(stringResource(Res.string.label_budget_consumed), totalSpent.toPersianPrice(), GlassGreen)
            SummaryItem(stringResource(Res.string.label_remaining), totalRemaining.toPersianPrice(), MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: androidx.compose.ui.graphics.Color? = null) {
    val glassColors = LocalGlassColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FintrackLabelSmallText(text = label, color = glassColors.text3)
        FintrackBodyLargeText(text = value, fontWeight = FontWeight.Bold, color = color ?: glassColors.text)
    }
}

@Composable
private fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, color: androidx.compose.ui.graphics.Color) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
    }
}
