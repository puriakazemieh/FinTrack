package com.kazemieh.debt.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.common.model.*
import com.kazemieh.common.toPersianPrice
import com.kazemieh.debt.ui.add.AddDebtBottomSheet
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.financialsource.ui.list.SourceFilterSelectionContent
import com.kazemieh.person.ui.list.PersonFilterSelectionContent
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    onBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit,
    viewModel: DebtViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(DebtIntent.ObserveAllDebts)
    }

    var selectedDebtForEdit by remember { mutableStateOf<Long?>(null) }
    var selectedDebtForDelete by remember { mutableStateOf<DebtWithRelations?>(null) }
    var showAddDebt by remember { mutableStateOf(false) }
    var debtToSettle by remember { mutableStateOf<DebtWithRelations?>(null) }
    var postSettlementAsTransaction by remember { mutableStateOf(false) }

    FintrackScreen(
        title = stringResource(Res.string.navigation_debts),
        sub = stringResource(Res.string.title_debts_management),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onIntent(DebtIntent.UpdateSearchQuery(it)) },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { viewModel.onIntent(DebtIntent.OnFilterClick) },
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

            SummaryHeader(
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.total_credits),
                        value = state.totalCredits.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = GlassGreen
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.total_debts),
                        value = state.totalDebts.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = GlassRed
                    )
                )
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredDebts.size) { index ->
                    val item = state.filteredDebts[index]
                    val typeColor = if (item.debt.type == DebtType.OWED_TO_ME) GlassGreen else GlassRed
                    val typeLabel = stringResource(
                        if (item.debt.type == DebtType.OWED_TO_ME) Res.string.debt_owed_to_me
                        else Res.string.debt_owed_by_me
                    )

                    EntityRow(
                        item = EntityItem(
                            id = item.debt.id,
                            name = item.person.name,
                            sub = listOfNotNull(typeLabel, item.debt.description?.takeIf { it.isNotBlank() })
                                .joinToString("  |  "),
                            sub2 = if (item.debt.isSettled) {
                                stringResource(
                                    if (item.debt.type == DebtType.OWED_TO_ME) Res.string.credit_collected
                                    else Res.string.debt_paid
                                )
                            } else {
                                null
                            },
                            badge = item.debt.amount.toPersianPrice(),
                            color = typeColor,
                            trailingContent = {
                                if (item.debt.isSettled) {
                                    SettledDebtIndicator(item.debt.type)
                                } else {
                                    SettleDebtAction(
                                        debtType = item.debt.type,
                                        onClick = {
                                            debtToSettle = item
                                            postSettlementAsTransaction = false
                                        }
                                    )
                                }
                            }
                        ),
                        mainColor = typeColor,
                        showActions = true,
                        onEdit = {
                            selectedDebtForEdit = item.debt.id
                            showAddDebt = true
                        },
                        onDelete = { selectedDebtForDelete = item },
                        onClick = { onNavigateToPersonDetail(item.person.id ?: 0L) }
                    )
                }
            }
        }

        Fab(
            label = stringResource(Res.string.add_debt),
            icon = rememberVectorPainter(Icons.Default.Add),
            onClick = {
                selectedDebtForEdit = null
                showAddDebt = true
            },
            modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).padding(bottom = 80.dp)
        )

        if (showAddDebt) {
            AddDebtBottomSheet(
                debtId = selectedDebtForEdit,
                onDismiss = { showAddDebt = false }
            )
        }

        selectedDebtForDelete?.let { item ->
            DeleteBottomSheet(
                itemName = item.person.name + (item.debt.description?.let { " ($it)" } ?: ""),
                dismissClicked = { selectedDebtForDelete = null },
                confirmClicked = {
                    viewModel.onIntent(DebtIntent.DeleteDebt(item.debt.id))
                    selectedDebtForDelete = null
                }
            )
        }

        if (state.showFilterSheet) {
            DebtFilterBottomSheet(
                selectedCategories = state.filterCategories,
                selectedSources = state.filterSources,
                selectedTags = state.filterTags,
                selectedPersons = state.filterPersons,
                onReset = { viewModel.onIntent(DebtIntent.OnFilterReset) },
                onDismiss = { viewModel.onIntent(DebtIntent.OnFilterSheetDismiss) },
                onUpdate = { cats, srcs, tags, pers ->
                    viewModel.onIntent(DebtIntent.OnFilterUpdate(cats, srcs, tags, pers))
                }
            )
        }

        debtToSettle?.let { debt ->
            DebtSettlementBottomSheet(
                debt = debt,
                postAsTransaction = postSettlementAsTransaction,
                onPostAsTransactionChange = { postSettlementAsTransaction = it },
                onConfirm = {
                    viewModel.onIntent(
                        DebtIntent.SettleDebt(
                            debtId = debt.debt.id,
                            postAsTransaction = postSettlementAsTransaction
                        )
                    )
                    debtToSettle = null
                },
                onDismiss = { debtToSettle = null }
            )
        }
    }
}

@Composable
private fun SettleDebtAction(
    debtType: DebtType,
    onClick: () -> Unit
) {
    val color = if (debtType == DebtType.OWED_TO_ME) GlassGreen else GlassRed
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = stringResource(Res.string.settle_debt),
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettledDebtIndicator(debtType: DebtType) {
    val color = if (debtType == DebtType.OWED_TO_ME) GlassGreen else GlassRed
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = stringResource(Res.string.debt_settled),
        tint = color,
        modifier = Modifier.size(22.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtSettlementBottomSheet(
    debt: DebtWithRelations,
    postAsTransaction: Boolean,
    onPostAsTransactionChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val canPostAsTransaction = debt.debt.sourceId != null
    val completionLabel = stringResource(
        if (debt.debt.type == DebtType.OWED_TO_ME) Res.string.credit_collected
        else Res.string.debt_paid
    )
    val glassColors = LocalGlassColors.current

    SheetFrame(
        title = completionLabel,
        sub = stringResource(Res.string.debt_settle_confirm),
        onDismiss = onDismiss,
        isFullScreen = false,
        primaryButtonText = stringResource(Res.string.confirm),
        onPrimaryClick = onConfirm,
        secondaryButtonText = stringResource(Res.string.cancell_),
        onSecondaryClick = onDismiss
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            padding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.label_post_as_transaction),
                            color = if (canPostAsTransaction) glassColors.text else glassColors.text3
                        )
                        if (!canPostAsTransaction) {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.settlement_no_source),
                                color = glassColors.text3
                            )
                        }
                    }
                    Switch(
                        on = postAsTransaction,
                        onToggle = onPostAsTransactionChange,
                        enabled = canPostAsTransaction
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtFilterBottomSheet(
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
