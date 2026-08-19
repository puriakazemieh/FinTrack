package com.kazemieh.check.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.check.ui.add.AddCheckBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.persianMonth
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityItemGroup
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.glass.Tabs
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.financialsource.ui.list.SourceFilterSelectionContent
import com.kazemieh.person.ui.list.PersonFilterSelectionContent
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckListScreen(
    onBack: () -> Unit,
    viewModel: CheckListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddCheck by remember { mutableStateOf(false) }
    var selectedCheckId by remember { mutableStateOf<Long?>(null) }
    val filtersActive = state.filterCategories.isNotEmpty() || state.filterSources.isNotEmpty() ||
        state.filterTags.isNotEmpty() || state.filterPersons.isNotEmpty()

    val tabs = listOf(
        stringResource(Res.string.label_check_status_ongoing),
        stringResource(Res.string.label_check_status_passed),
        stringResource(Res.string.label_check_status_returned),
        stringResource(Res.string.label_check_status_cancelled)
    )
    var selectedCheckForStatus by remember { mutableStateOf<Check?>(null) }
    val currentStatus = CheckStatus.entries[selectedTab]
    val filteredChecks = state.filteredChecks.filter { it.status == currentStatus }
    val totalAmount = filteredChecks.sumOf { it.amount }
    val checkGroups = filteredChecks
        .sortedByDescending { it.dueDate }
        .groupBy { it.dueDate.monthHeader() }
        .map { (month, checks) ->
            EntityItemGroup(
                title = month,
                items = checks.map {
                    EntityItem(
                        id = it.id,
                        name = it.personName ?: stringResource(Res.string.label_unknown_person),
                        sub = it.description,
                        badge = it.amount.toPersianPrice(),
                        color = if (it.isIncoming) GlassGreen else GlassRed,
                        trailingContent = {
                            CheckStatusAction(status = it.status, onClick = { selectedCheckForStatus = it })
                        }
                    )
                }
            )
        }

    FintrackScreen(
        title = stringResource(Res.string.title_check_management),
        sub = stringResource(Res.string.sub_check_list_management_desc),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Tabs(
                tabs = tabs,
                active = selectedTab,
                onChange = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                counts = CheckStatus.entries.map { status -> state.filteredChecks.count { it.status == status } }
            )
            EntityList(
                title = stringResource(Res.string.title_check_management),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(CheckListIntent.UpdateSearchQuery(it)) },
                onAddClick = { selectedCheckId = null; showAddCheck = true },
                items = checkGroups.flatMap { it.items },
                onEditClick = { selectedCheckId = it.id; showAddCheck = true },
                onDeleteClick = { viewModel.onIntent(CheckListIntent.DeleteCheck(it.id)) },
                showActions = true,
                itemGroups = checkGroups,
                searchTrailing = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CompactSummaryCard(total = totalAmount)

                        IconButton(
                            onClick = { viewModel.onIntent(CheckListIntent.OpenFilters) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (filtersActive) GlassGreen.copy(alpha = .1f) else LocalGlassColors.current.glass)
                                .border(1.dp, if (filtersActive) GlassGreen else LocalGlassColors.current.glassEdge, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (filtersActive) GlassGreen else LocalGlassColors.current.text2
                            )
                        }
                    }
                }
            )
        }
        if (showAddCheck) {
            AddCheckBottomSheet(
                checkId = selectedCheckId,
                onDismiss = { showAddCheck = false; selectedCheckId = null }
            )
        }
        if (state.showFilterSheet) {
            CheckFilterBottomSheet(
                categories = state.filterCategories,
                sources = state.filterSources,
                tags = state.filterTags,
                persons = state.filterPersons,
                onReset = { viewModel.onIntent(CheckListIntent.ResetFilters) },
                onDismiss = { viewModel.onIntent(CheckListIntent.DismissFilters) },
                onUpdate = { categories, sources, tags, persons ->
                    viewModel.onIntent(CheckListIntent.UpdateFilters(categories, sources, tags, persons))
                }
            )
        }
        selectedCheckForStatus?.let { check ->
            CheckStatusBottomSheet(
                currentStatus = check.status,
                onStatusSelected = {
                    viewModel.onIntent(CheckListIntent.UpdateStatus(check.id, it))
                    selectedCheckForStatus = null
                },
                onDismiss = { selectedCheckForStatus = null }
            )
        }
        if (state.confirmTransactionForCheckId != null && state.confirmTransactionForNewStatus != null) {
            CheckTransactionConfirmBottomSheet(
                onDismiss = { viewModel.onIntent(CheckListIntent.CancelStatusUpdate) },
                onConfirm = { createTransaction ->
                    viewModel.onIntent(CheckListIntent.SubmitStatusUpdate(state.confirmTransactionForCheckId!!, state.confirmTransactionForNewStatus!!, createTransaction))
                }
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CheckTransactionConfirmBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    SheetFrame(
        title = "ثبت تراکنش",
        sub = "آیا می‌خواهید پاس شدن این چک به عنوان یک تراکنش در برنامه ثبت شود؟",
        onDismiss = onDismiss,
        isFullScreen = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = { onConfirm(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                FintrackLabelMediumText("بله، به عنوان تراکنش ثبت شود", color = MaterialTheme.colorScheme.onPrimary)
            }
            androidx.compose.material3.TextButton(
                onClick = { onConfirm(false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                FintrackLabelMediumText("خیر، فقط وضعیت تغییر کند", color = LocalGlassColors.current.text)
            }
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
            FintrackLabelSmallText(text = stringResource(Res.string.label_total_amount), fontSize = 8.sp, color = glassColors.text3)
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
private fun CheckStatusAction(status: CheckStatus, onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    val color = when (status) {
        CheckStatus.PENDING -> GlassBlue
        CheckStatus.PASSED -> GlassGreen
        CheckStatus.REJECTED -> GlassRed
        CheckStatus.CANCELLED -> glassColors.text3
    }
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
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckStatusBottomSheet(
    currentStatus: CheckStatus,
    onStatusSelected: (CheckStatus) -> Unit,
    onDismiss: () -> Unit
) {
    SheetFrame(
        title = stringResource(Res.string.label_check_status_title),
        sub = stringResource(Res.string.sub_check_list_management_desc),
        onDismiss = onDismiss,
        isFullScreen = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CheckStatus.entries.forEach { status ->
                val label = when (status) {
                    CheckStatus.PENDING -> Res.string.label_check_status_ongoing
                    CheckStatus.PASSED -> Res.string.label_check_status_passed
                    CheckStatus.REJECTED -> Res.string.label_check_status_returned
                    CheckStatus.CANCELLED -> Res.string.label_check_status_cancelled
                }
                val color = when (status) {
                    CheckStatus.PENDING -> GlassBlue
                    CheckStatus.PASSED -> GlassGreen
                    CheckStatus.REJECTED -> GlassRed
                    CheckStatus.CANCELLED -> LocalGlassColors.current.text3
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (currentStatus == status) color.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onStatusSelected(status) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FintrackBodyLargeText(
                        text = stringResource(label),
                        color = if (currentStatus == status) color else LocalGlassColors.current.text,
                        fontWeight = if (currentStatus == status) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun Long.monthHeader(): String {
    val date = Instant.fromEpochMilliseconds(this).toPersianDateTime(TimeZone.currentSystemDefault())
    return "${date.persianMonth().displayName} ${date.year.toString().toPersianDigits()}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckFilterBottomSheet(
    categories: Set<Category>,
    sources: Set<Source>,
    tags: Set<Tag>,
    persons: Set<Person>,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onUpdate: (Set<Category>, Set<Source>, Set<Tag>, Set<Person>) -> Unit
) {
    var selectedCategories by remember(categories) { mutableStateOf(categories) }
    var selectedSources by remember(sources) { mutableStateOf(sources) }
    var selectedTags by remember(tags) { mutableStateOf(tags) }
    var selectedPersons by remember(persons) { mutableStateOf(persons) }
    SheetFrame(
        title = stringResource(Res.string.report),
        sub = stringResource(Res.string.msg_filters_combined),
        onDismiss = onDismiss,
        primaryButtonText = stringResource(Res.string.save_),
        onPrimaryClick = {
            onUpdate(selectedCategories, selectedSources, selectedTags, selectedPersons)
            onDismiss()
        },
        trailingContent = {
            TextButton(onClick = {
                selectedCategories = emptySet()
                selectedSources = emptySet()
                selectedTags = emptySet()
                selectedPersons = emptySet()
                onReset()
            }) {
                FintrackLabelMediumText(stringResource(Res.string.btn_clear_all), color = GlassRed)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CheckFilterSection(stringResource(Res.string.category)) {
                CategoryFilterSelectionContent(
                    selectedCategories = selectedCategories,
                    selectedTransactionType = TransactionType.ALL,
                    isAllSelected = selectedCategories.isEmpty(),
                    onSelectionChanged = { selected, _ -> selectedCategories = selected }
                )
            }
            CheckFilterSection(stringResource(Res.string.source)) {
                SourceFilterSelectionContent(
                    selectedSources = selectedSources,
                    isAllSelected = selectedSources.isEmpty(),
                    onSelectionChanged = { selected, _ -> selectedSources = selected }
                )
            }
            CheckFilterSection(stringResource(Res.string.tags)) {
                TagFilterSelectionContent(
                    selectedTags = selectedTags,
                    isAllSelected = selectedTags.isEmpty(),
                    onSelectionChanged = { selected, _ -> selectedTags = selected }
                )
            }
            CheckFilterSection(stringResource(Res.string.persons)) {
                PersonFilterSelectionContent(
                    selectedPersons = selectedPersons,
                    isAllSelected = selectedPersons.isEmpty(),
                    onSelectionChanged = { selected, _ -> selectedPersons = selected }
                )
            }
        }
    }
}

@Composable
private fun CheckFilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelMediumText(title, color = LocalGlassColors.current.text)
        content()
    }
}
