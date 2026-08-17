package com.kazemieh.debt.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    var showAddDebt by remember { mutableStateOf(false) }

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
                    
                    EntityRow(
                        item = EntityItem(
                            id = item.debt.id,
                            name = item.person.name,
                            sub = (if (item.debt.type == DebtType.OWED_TO_ME) "طلب" else "بدهی") + "  |  " + (item.debt.description ?: ""),
                            badge = item.debt.amount.toPersianPrice(),
                            color = typeColor
                        ),
                        mainColor = typeColor,
                        showActions = true,
                        onEdit = {
                            selectedDebtForEdit = item.debt.id
                            showAddDebt = true
                        },
                        onDelete = { viewModel.onIntent(DebtIntent.DeleteDebt(item.debt.id)) },
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
