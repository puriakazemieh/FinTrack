package com.kazemieh.installment.ui.list

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.Direction
import com.kazemieh.common.model.*
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.financialsource.ui.list.SourceFilterSelectionContent
import com.kazemieh.installment.ui.InstallmentIntent
import com.kazemieh.installment.ui.InstallmentViewModel
import com.kazemieh.installment.ui.add.AddInstallmentBottomSheet
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.person.ui.list.PersonFilterSelectionContent
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun InstallmentsScreen(
    viewModel: InstallmentViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddInstallment by remember { mutableStateOf(false) }
    var selectedInstallmentId by remember { mutableStateOf<Long?>(null) }
    var installmentToDelete by remember { mutableStateOf<com.kazemieh.common.model.Installment?>(null) }

    val tabs = listOf(
        stringResource(Res.string.installment_upcoming_month),
        stringResource(Res.string.installment_upcoming),
        stringResource(Res.string.installment_overdue),
        stringResource(Res.string.installment_paid)
    )

    LaunchedEffect(Unit) {
        viewModel.onIntent(InstallmentIntent.Init)
    }

    val displayLabel = dateRangeLabelText(state.dateRange?.label)

    FintrackScreen(
        title = stringResource(Res.string.navigation_installment),
        sub = displayLabel,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PeriodNavigator(
                currentPeriod = state.dateRange?.filterType ?: DateFilterType.THIS_MONTH,
                periodLabel = displayLabel,
                periodSubLabel = "",
                onPeriodSelected = { viewModel.onIntent(InstallmentIntent.ChangeFilterType(it)) },
                onPrevClick = { viewModel.onIntent(InstallmentIntent.ShiftRange(Direction.PREVIOUS)) },
                onNextClick = { viewModel.onIntent(InstallmentIntent.ShiftRange(Direction.NEXT)) },
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
                    onQueryChange = { viewModel.onIntent(InstallmentIntent.UpdateSearchQuery(it)) },
                    modifier = Modifier.weight(1f)
                )

                CompactSummaryCard(total = state.totalAmount)

                IconButton(
                    onClick = { viewModel.onIntent(InstallmentIntent.OnFilterClick) },
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

            Tabs(
                tabs = tabs,
                active = selectedTabIndex,
                onChange = { selectedTabIndex = it },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                counts = listOf(
                    state.upcomingMonth.size,
                    state.future.size,
                    state.overdue.size,
                    state.completed.size
                )
            )

            val items = when (selectedTabIndex) {
                0 -> state.filteredUpcomingMonth
                1 -> state.filteredFuture
                2 -> state.filteredOverdue
                else -> state.filteredCompleted
            }

            EntityList(
                title = tabs[selectedTabIndex],
                addLabel = stringResource(Res.string.add_installment),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(InstallmentIntent.UpdateSearchQuery(it)) },
                onAddClick = { selectedInstallmentId = null; showAddInstallment = true },
                items = items.map { item ->
                    val progress =
                        (item.installment.paidInstallments.toFloat() / item.installment.totalInstallments).coerceIn(
                            0f,
                            1f
                        )
                    val percentage = (progress * 100).toInt()
                    val paymentDesc = stringResource(Res.string.payment_for, item.installment.title)
                    val reminderTitle = stringResource(Res.string.notif_installment_title)
                    val reminderMsg = stringResource(Res.string.notif_installment_desc, item.installment.title)

                    EntityItem(
                        id = item.installment.id,
                        name = item.installment.title,
                        sub = stringResource(
                            Res.string.remaining_installments,
                            (item.installment.totalInstallments - item.installment.paidInstallments)
                        ),
                        badge = "$percentage%",
                        color = if (item.installment.isCompleted) GlassGreen else MaterialTheme.colorScheme.primary,
                        sub2 = item.installment.installmentAmount.toInt()
                            .toSignedPersianPrice() + " " + stringResource(Res.string.currency_toman),
                        trailingContent = {
                            if (!item.installment.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(glassColors.glass)
                                        .border(1.dp, glassColors.glassEdge, RoundedCornerShape(9.dp))
                                        .clickable {
                                            viewModel.onIntent(InstallmentIntent.MarkAsPaid(
                                                installmentId = item.installment.id,
                                                transactionDescription = paymentDesc,
                                                reminderTitle = reminderTitle,
                                                reminderMessage = reminderMsg
                                            ))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GlassGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    )
                },
                onEditClick = { clickedItem -> selectedInstallmentId = clickedItem.id; showAddInstallment = true },
                onDeleteClick = { clickedItem ->
                    installmentToDelete = (state.overdue + state.upcomingMonth + state.future + state.completed)
                        .find { it.installment.id == clickedItem.id }?.installment
                },
                showActions = true
            )
        }

        if (showAddInstallment) {
            AddInstallmentBottomSheet(
                installmentId = selectedInstallmentId,
                onDismiss = { showAddInstallment = false; selectedInstallmentId = null },
                onSuccess = { viewModel.onIntent(InstallmentIntent.Init) }
            )
        }

        installmentToDelete?.let { installment ->
            DeleteBottomSheet(
                itemName = installment.title,
                dismissClicked = { installmentToDelete = null },
                confirmClicked = {
                    viewModel.onIntent(InstallmentIntent.Delete(installment.id))
                    installmentToDelete = null
                }
            )
        }

        if (state.showFilterSheet) {
            InstallmentFilterBottomSheet(
                selectedCategories = state.filterCategories,
                selectedSources = state.filterSources,
                selectedTags = state.filterTags,
                selectedPersons = state.filterPersons,
                onReset = { viewModel.onIntent(InstallmentIntent.OnFilterReset) },
                onDismiss = { viewModel.onIntent(InstallmentIntent.OnFilterSheetDismiss) },
                onUpdate = { cats, srcs, tags, pers ->
                    viewModel.onIntent(InstallmentIntent.OnFilterUpdate(cats, srcs, tags, pers))
                }
            )
        }
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
            FintrackLabelSmallText(text = "مجموع اقساط", fontSize = 8.sp, color = glassColors.text3)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FintrackLabelMediumText(
                    text = total.toSignedPersianPrice(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentFilterBottomSheet(
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
