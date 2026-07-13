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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.add.AddBudgetBottomSheet
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRangeLabel
import com.kazemieh.common.model.BudgetPeriod
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.Category
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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

    val displayLabel = when (val label = state.dateRange?.label) {
        is DateRangeLabel.Filter -> {
            when (label.type) {
                DateFilterType.TODAY -> stringResource(Res.string.today)
                DateFilterType.YESTERDAY -> stringResource(Res.string.yesterday)
                DateFilterType.THIS_WEEK -> stringResource(Res.string.this_week)
                DateFilterType.THIS_MONTH -> stringResource(Res.string.this_month)
                DateFilterType.LAST_MONTH -> stringResource(Res.string.last_month)
                DateFilterType.CUSTOM_RANGE -> stringResource(Res.string.custom_range)
                else -> stringResource(Res.string.all)
            }
        }
        is DateRangeLabel.Text -> label.value
        null -> ""
    }

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
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // DAILY SECTION
                if (state.dailyBudgets.isNotEmpty() || state.canCloneDaily) {
                    stickyHeader { SectionHeader(stringResource(Res.string.label_budget_period_daily)) }
                    if (state.canCloneDaily) {
                        item { ClonePrompt(BudgetPeriod.DAILY) { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.DAILY)) } }
                    }
                    items(state.dailyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }

                // WEEKLY SECTION
                if (state.weeklyBudgets.isNotEmpty() || state.canCloneWeekly) {
                    stickyHeader { SectionHeader(stringResource(Res.string.label_budget_period_weekly)) }
                    if (state.canCloneWeekly) {
                        item { ClonePrompt(BudgetPeriod.WEEKLY) { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.WEEKLY)) } }
                    }
                    items(state.weeklyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }

                // MONTHLY SECTION
                if (state.monthlyBudgets.isNotEmpty() || state.canCloneMonthly) {
                    stickyHeader { SectionHeader(stringResource(Res.string.label_budget_period_monthly)) }
                    if (state.canCloneMonthly) {
                        item { ClonePrompt(BudgetPeriod.MONTHLY) { viewModel.onIntent(BudgetIntent.CloneFromPrevious(BudgetPeriod.MONTHLY)) } }
                    }
                    items(state.monthlyBudgets) { BudgetRow(it, viewModel, onNavigateToTransactions) }
                }

                // YEARLY SECTION
                if (state.yearlyBudgets.isNotEmpty()) {
                    stickyHeader { SectionHeader(stringResource(Res.string.label_budget_period_yearly)) }
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
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        FintrackBodyLargeText(text = title, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ClonePrompt(period: BudgetPeriod, onClone: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        onClick = onClone,
        tone = GlassTone.Strong
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = GlassGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            FintrackBodyMediumText(
                text = "کپی از دوره قبل",
                color = GlassGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BudgetRow(
    item: BudgetWithProgress,
    viewModel: BudgetViewModel,
    onNavigateToTransactions: ((Category) -> Unit)?
) {
    val progress = (item.spentAmount.toFloat() / item.budget.amount).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    
    EntityRowWrapper(
        item = EntityItem(
            id = item.budget.id ?: 0L,
            name = item.category?.name ?: "",
            sub = "${item.spentAmount.toPersianPrice()} / ${item.budget.amount.toPersianPrice()}",
            badge = "$percentage%",
            color = if (progress >= 0.8f) MaterialTheme.colorScheme.error else GlassGreen,
            iconId = item.category?.iconId,
            colorId = item.category?.colorId
        ),
        onEdit = { viewModel.onIntent(BudgetIntent.ShowAddBudget(item)) },
        onDelete = { viewModel.onIntent(BudgetIntent.DeleteBudget(item)) },
        onFilter = onNavigateToTransactions?.let { callback ->
            { item.category?.let { callback(it) } }
        }
    )
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

// Minimal EntityRow implementation for Budget
@Composable
private fun EntityRowWrapper(
    item: EntityItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFilter: (() -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current
    val color = item.color ?: GlassGreen
    GlassCard(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        padding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FinTrackLeadingIcon(
                colorId = item.colorId,
                iconId = item.iconId,
                style = LeadingIconStyle.Badge,
                size = 38.dp,
                iconSize = 16.dp,
                corner = 12.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                FintrackBodyMediumText(text = item.name, fontWeight = FontWeight.SemiBold)
                item.sub?.let { FintrackLabelSmallText(text = it, color = glassColors.text3) }
            }

            Box(
                modifier = Modifier
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                FintrackLabelSmallText(text = item.badge ?: "", fontWeight = FontWeight.Bold, color = color)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onFilter?.let {
                    ActionIcon(icon = androidx.compose.material.icons.Icons.Default.FilterList, onClick = it, color = GlassGreen)
                }
                ActionIcon(icon = androidx.compose.material.icons.Icons.Default.Edit, onClick = onEdit, color = glassColors.text2)
                ActionIcon(icon = androidx.compose.material.icons.Icons.Default.Delete, onClick = onDelete, color = androidx.compose.ui.graphics.Color.Red)
            }
        }
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
