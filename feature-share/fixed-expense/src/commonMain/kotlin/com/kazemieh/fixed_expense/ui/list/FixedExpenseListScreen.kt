package com.kazemieh.fixed_expense.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRangeLabel
import com.kazemieh.common.Direction
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.Fab
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.PeriodNavigator
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.custom_date
import fintrack.core.designsystem.generated.resources.custom_range
import fintrack.core.designsystem.generated.resources.dp_today
import fintrack.core.designsystem.generated.resources.frequency_daily
import fintrack.core.designsystem.generated.resources.frequency_monthly
import fintrack.core.designsystem.generated.resources.frequency_weekly
import fintrack.core.designsystem.generated.resources.frequency_yearly
import fintrack.core.designsystem.generated.resources.hint_search_in
import fintrack.core.designsystem.generated.resources.label_approx_monthly_total
import fintrack.core.designsystem.generated.resources.label_this_year
import fintrack.core.designsystem.generated.resources.label_unknown_person
import fintrack.core.designsystem.generated.resources.last_month
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.title_add_fixed_expense
import fintrack.core.designsystem.generated.resources.title_fixed_expense_management
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.yesterday
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val SECTION_ORDER = listOf(
    RecurrenceType.DAILY,
    RecurrenceType.WEEKLY,
    RecurrenceType.MONTHLY,
    RecurrenceType.YEARLY,
    RecurrenceType.CUSTOM,
    RecurrenceType.ONCE
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixedExpenseListScreen(
    onBack: () -> Unit,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var selectedExpenseId by remember { mutableStateOf<Long?>(null) }

    val displayLabel = when (val label = state.dateRange?.label) {
        is DateRangeLabel.Filter -> when (label.type) {
            DateFilterType.TODAY -> stringResource(Res.string.today)
            DateFilterType.YESTERDAY -> stringResource(Res.string.yesterday)
            DateFilterType.THIS_WEEK -> stringResource(Res.string.this_week)
            DateFilterType.THIS_MONTH -> stringResource(Res.string.this_month)
            DateFilterType.LAST_MONTH -> stringResource(Res.string.last_month)
            DateFilterType.CUSTOM_RANGE -> stringResource(Res.string.custom_range)
            else -> stringResource(Res.string.all)
        }
        is DateRangeLabel.Text -> label.value
        null -> ""
    }

    FintrackScreen(
        title = stringResource(Res.string.title_fixed_expense_management),
        sub = displayLabel,
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PeriodNavigator(
                currentPeriod = state.dateRange?.filterType ?: DateFilterType.THIS_MONTH,
                periodLabel = displayLabel,
                periodSubLabel = "",
                onPeriodSelected = { viewModel.onIntent(FixedExpenseListIntent.ChangeFilterType(it)) },
                onPrevClick = { viewModel.onIntent(FixedExpenseListIntent.ShiftRange(Direction.PREVIOUS)) },
                onNextClick = { viewModel.onIntent(FixedExpenseListIntent.ShiftRange(Direction.NEXT)) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                excludeCustomRange = true
            )

            SummaryCard(total = state.totalApprox)

            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(FixedExpenseListIntent.UpdateSearchQuery(it)) },
                placeholder = stringResource(Res.string.hint_search_in, stringResource(Res.string.title_fixed_expense_management)),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                SECTION_ORDER.forEach { recurrence ->
                    val expenses = state.grouped[recurrence].orEmpty()
                    val showClone = recurrence == RecurrenceType.ONCE && state.canCloneOnce
                    if (expenses.isNotEmpty() || showClone) {
                        stickyHeader {
                            SectionHeader(
                                title = recurrenceLabel(recurrence),
                                cloneLabel = if (showClone) stringResource(Res.string.fixed_expense_clone_once) else null,
                                onClone = { viewModel.onIntent(FixedExpenseListIntent.CloneOnceFromPrevious) }
                            )
                        }
                        items(expenses, key = { it.id }) { expense ->
                            FixedExpenseRow(
                                expense = expense,
                                onEdit = { selectedExpenseId = expense.id; showAddExpense = true },
                                onDelete = { viewModel.onIntent(FixedExpenseListIntent.OnDeleteClick(expense)) }
                            )
                        }
                    }
                }
            }
        }

        Fab(
            label = stringResource(Res.string.title_add_fixed_expense),
            icon = rememberVectorPainter(Icons.Default.Add),
            onClick = { selectedExpenseId = null; showAddExpense = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )

        if (showAddExpense) {
            AddFixedExpenseBottomSheet(
                expenseId = selectedExpenseId,
                onDismiss = { showAddExpense = false; selectedExpenseId = null }
            )
        }

        if (state.isDeleteShow && state.selectedExpense != null) {
            DeleteBottomSheet(
                itemName = state.selectedExpense?.categoryName ?: state.selectedExpense?.description,
                dismissClicked = { viewModel.onIntent(FixedExpenseListIntent.OnDeleteClick(null)) },
                confirmClicked = { viewModel.onIntent(FixedExpenseListIntent.ConfirmDelete) }
            )
        }
    }
}

@Composable
private fun recurrenceLabel(recurrence: RecurrenceType): String = when (recurrence) {
    RecurrenceType.DAILY -> stringResource(Res.string.frequency_daily)
    RecurrenceType.WEEKLY -> stringResource(Res.string.frequency_weekly)
    RecurrenceType.MONTHLY -> stringResource(Res.string.frequency_monthly)
    RecurrenceType.YEARLY -> stringResource(Res.string.frequency_yearly)
    RecurrenceType.CUSTOM -> stringResource(Res.string.custom_date)
    RecurrenceType.ONCE -> stringResource(Res.string.dp_today)
}

@Composable
private fun SummaryCard(total: Long) {
    val glassColors = LocalGlassColors.current
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        tone = GlassTone.Strong,
        padding = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FintrackLabelSmallText(text = stringResource(Res.string.label_approx_monthly_total), color = glassColors.text3)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FintrackTitleMediumText(
                    text = total.toPersianPrice(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FintrackLabelSmallText(
                    text = stringResource(Res.string.currency_toman),
                    color = glassColors.text3,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
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
            .fillMaxWidth()
            .background(glassColors.bg1)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(text = title, fontWeight = FontWeight.Bold, color = glassColors.text)
        if (cloneLabel != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassGreen.copy(alpha = 0.12f))
                    .clickable(onClick = onClone)
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
                FintrackLabelSmallText(text = cloneLabel, color = GlassGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FixedExpenseRow(
    expense: FixedExpense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val color = if (expense.isActive) MaterialTheme.colorScheme.primary else glassColors.text3
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
                colorId = null,
                iconId = null,
                style = LeadingIconStyle.Badge,
                size = 38.dp,
                iconSize = 16.dp,
                corner = 12.dp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                FintrackTitleSmallText(
                    text = expense.categoryName
                        ?: expense.description?.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.label_unknown_person),
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.text,
                    maxLines = 1
                )
                FintrackBodySmallText(
                    text = expense.amount.toPersianPrice() + " " + stringResource(Res.string.currency_toman),
                    color = color,
                    maxLines = 1
                )
            }
            RowActionIcon(icon = Icons.Default.Edit, tint = glassColors.text2, onClick = onEdit)
            RowActionIcon(icon = Icons.Default.Delete, tint = GlassRed, onClick = onDelete)
        }
    }
}

@Composable
private fun RowActionIcon(icon: ImageVector, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
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
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
    }
}
