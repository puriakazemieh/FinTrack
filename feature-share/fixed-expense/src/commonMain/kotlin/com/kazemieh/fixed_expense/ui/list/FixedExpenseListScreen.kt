package com.kazemieh.fixed_expense.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.Direction
import com.kazemieh.common.model.*
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.financialsource.ui.list.SourceFilterSelectionContent
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseBottomSheet
import com.kazemieh.person.ui.list.PersonFilterSelectionContent
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.getString
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FixedExpenseListScreen(
    onBack: () -> Unit,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var selectedExpenseId by remember { mutableStateOf<Long?>(null) }
    val glassColors = LocalGlassColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FixedExpenseListEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(getString(effect.messageRes))
                }
            }
        }
    }

    val displayLabel = dateRangeLabelText(state.dateRange?.label)

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

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onIntent(FixedExpenseListIntent.UpdateSearchQuery(it)) },
                    placeholder = stringResource(Res.string.hint_search_in, ""),
                    modifier = Modifier.weight(1f)
                )

                CompactSummaryCard(total = state.totalApprox)

                IconButton(
                    onClick = { viewModel.onIntent(FixedExpenseListIntent.OnFilterClick) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (state.filterCategories.isNotEmpty() || state.filterTags.isNotEmpty() || state.filterSources.isNotEmpty() || state.filterPersons.isNotEmpty())
                                GlassGreen.copy(alpha = 0.1f) else glassColors.glass
                        )
                        .border(
                            1.dp,
                            if (state.filterCategories.isNotEmpty() || state.filterTags.isNotEmpty() || state.filterSources.isNotEmpty() || state.filterPersons.isNotEmpty())
                                GlassGreen else glassColors.glassEdge,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = if (state.filterCategories.isNotEmpty() || state.filterTags.isNotEmpty() || state.filterSources.isNotEmpty() || state.filterPersons.isNotEmpty())
                            GlassGreen else glassColors.text2
                    )
                }
            }

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
                                onDelete = {
                                    viewModel.onIntent(
                                        FixedExpenseListIntent.OnDeleteClick(
                                            expense
                                        )
                                    )
                                },
                                onRegister = {
                                    viewModel.onIntent(FixedExpenseListIntent.RegisterAsTransaction(expense))
                                }
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
                itemName = state.selectedExpense?.title?.takeIf { it.isNotBlank() }
                    ?: state.selectedExpense?.categoryName
                    ?: state.selectedExpense?.description,
                dismissClicked = { viewModel.onIntent(FixedExpenseListIntent.OnDeleteClick(null)) },
                confirmClicked = { viewModel.onIntent(FixedExpenseListIntent.ConfirmDelete) }
            )
        }

        if (state.showFilterSheet) {
            FixedExpenseFilterBottomSheet(
                selectedCategories = state.filterCategories,
                selectedSources = state.filterSources,
                selectedTags = state.filterTags,
                selectedPersons = state.filterPersons,
                onReset = { viewModel.onIntent(FixedExpenseListIntent.OnFilterReset) },
                onDismiss = { viewModel.onIntent(FixedExpenseListIntent.OnFilterSheetDismiss) },
                onUpdate = { cats: Set<Category>, srcs: Set<Source>, tags: Set<Tag>, pers: Set<Person> ->
                    viewModel.onIntent(FixedExpenseListIntent.OnFilterUpdate(cats, srcs, tags, pers))
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun CompactSummaryCard(total: Long) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FintrackLabelSmallText(text = stringResource(Res.string.label_approx_monthly_total), fontSize = 8.sp, color = glassColors.text3)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FintrackLabelMediumText(
                    text = total.toPersianPrice(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FintrackLabelSmallText(
                    text = stringResource(Res.string.currency_toman),
                    fontSize = 8.sp,
                    color = glassColors.text3,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
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
    else -> stringResource(Res.string.all)
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
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassGreen.copy(alpha = 0.12f))
                    .clickable(onClick = onClone)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = GlassGreen,
                    modifier = Modifier.size(13.dp)
                )
                FintrackLabelSmallText(
                    text = cloneLabel,
                    color = GlassGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FixedExpenseRow(
    expense: FixedExpense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRegister: () -> Unit
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                FintrackTitleSmallText(
                    text = expense.title.takeIf { it.isNotBlank() }
                        ?: expense.categoryName
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

                if (expense.tagNames.isNotEmpty() || expense.personNames.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        expense.tagNames.forEach { name ->
                            FintrackLabelSmallText(
                                text = "#$name",
                                color = GlassGreen,
                                fontSize = 10.sp
                            )
                        }
                        expense.personNames.forEach { name ->
                            FintrackLabelSmallText(
                                text = name,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                val rangeText = expense.endDate?.let {
                    stringResource(
                        Res.string.label_date_range_span,
                        DateUtils.formatDate(expense.startDate),
                        DateUtils.formatDate(it)
                    )
                } ?: stringResource(
                    Res.string.label_date_range_from,
                    DateUtils.formatDate(expense.startDate)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = glassColors.text3,
                        modifier = Modifier.size(12.dp)
                    )
                    FintrackLabelSmallText(
                        text = rangeText,
                        color = glassColors.text3,
                        maxLines = 1
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RowActionIcon(icon = Icons.Default.Edit, tint = glassColors.text2, onClick = onEdit)
                    RowActionIcon(icon = Icons.Default.Delete, tint = GlassRed, onClick = onDelete)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassGreen.copy(alpha = 0.12f))
                        .clickable(onClick = onRegister)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = GlassGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.action_register_transaction),
                        color = GlassGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RowActionIcon(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpenseFilterBottomSheet(
    selectedCategories: Set<Category>,
    selectedSources: Set<Source>,
    selectedTags: Set<Tag>,
    selectedPersons: Set<Person>,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onUpdate: (Set<Category>, Set<Source>, Set<Tag>, Set<Person>) -> Unit
) {
    var cats by remember { mutableStateOf(selectedCategories) }
    var srcs by remember { mutableStateOf(selectedSources) }
    var tags by remember { mutableStateOf(selectedTags) }
    var pers by remember { mutableStateOf(selectedPersons) }

    SheetFrame(
        title = stringResource(Res.string.report),
        sub = stringResource(Res.string.msg_filters_combined),
        onDismiss = onDismiss,
        horizontalPadding = 20.dp,
        trailingContent = {
            TextButton(onClick = onReset) {
                FintrackLabelMediumText(
                    text = stringResource(Res.string.btn_clear_all),
                    color = GlassRed
                )
            }
        },
        primaryButtonText = stringResource(Res.string.save_),
        onPrimaryClick = {
            onUpdate(cats, srcs, tags, pers)
            onDismiss()
        }
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FilterSection(title = stringResource(Res.string.category)) {
                CategoryFilterSelectionContent(
                    selectedCategories = cats,
                    selectedTransactionType = TransactionType.EXPENSE,
                    isAllSelected = cats.isEmpty(),
                    onSelectionChanged = { selected, _ ->
                        cats = selected
                    }
                )
            }

            FilterSection(title = stringResource(Res.string.source)) {
                SourceFilterSelectionContent(
                    selectedSources = srcs,
                    isAllSelected = srcs.isEmpty(),
                    onSelectionChanged = { selected, _ ->
                        srcs = selected
                    }
                )
            }

            FilterSection(title = stringResource(Res.string.tags)) {
                TagFilterSelectionContent(
                    selectedTags = tags,
                    isAllSelected = tags.isEmpty(),
                    onSelectionChanged = { selected, _ ->
                        tags = selected
                    }
                )
            }

            FilterSection(title = stringResource(Res.string.persons)) {
                PersonFilterSelectionContent(
                    selectedPersons = pers,
                    isAllSelected = pers.isEmpty(),
                    onSelectionChanged = { selected, _ ->
                        pers = selected
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    val glassColors = LocalGlassColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GlassGreen)
            )
            FintrackLabelMediumText(
                text = title,
                color = glassColors.text,
                fontWeight = FontWeight.Bold
            )
        }
        content()
    }
}
