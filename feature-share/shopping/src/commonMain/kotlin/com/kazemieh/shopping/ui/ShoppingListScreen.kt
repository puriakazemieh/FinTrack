package com.kazemieh.shopping.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Fab
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.LargeAmountCard
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_and_next
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.btn_clear_all
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.filter
import fintrack.core.designsystem.generated.resources.hint_optional_note
import fintrack.core.designsystem.generated.resources.label_most_used
import fintrack.core.designsystem.generated.resources.label_note
import fintrack.core.designsystem.generated.resources.msg_filters_combined
import fintrack.core.designsystem.generated.resources.priority_high
import fintrack.core.designsystem.generated.resources.priority_normal
import fintrack.core.designsystem.generated.resources.reminder
import fintrack.core.designsystem.generated.resources.report
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.shopping_add_title
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.shopping_purchased
import fintrack.core.designsystem.generated.resources.shopping_title_label
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.total_sum
import fintrack.core.designsystem.generated.resources.shopping_list_empty
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val glassColors = LocalGlassColors.current

    val active = state.filteredItems.filter { !it.isChecked }
    val purchased = state.filteredItems.filter { it.isChecked }
    val total = state.items.filter { !it.isChecked }.sumOf { it.estimatedPrice }

    FintrackScreen(
        title = stringResource(Res.string.shopping_list),
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
                    onQueryChange = { viewModel.onIntent(ShoppingIntent.UpdateSearchQuery(it)) },
                    modifier = Modifier.weight(1f)
                )

                CompactSummaryCard(total = total.toLong())

                IconButton(
                    onClick = { viewModel.onIntent(ShoppingIntent.OnFilterClick) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.filterCategories.isNotEmpty() || state.filterTags.isNotEmpty()) GlassGreen.copy(alpha = 0.1f) else glassColors.glass)
                        .border(1.dp, if (state.filterCategories.isNotEmpty() || state.filterTags.isNotEmpty()) GlassGreen else glassColors.glassEdge, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = if (state.filterCategories.isNotEmpty() || state.filterTags.isNotEmpty()) GlassGreen else glassColors.text2
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(active, key = { "a_${it.id}" }) { item ->
                    ShoppingRow(
                        item = item,
                        onToggle = { viewModel.onIntent(ShoppingIntent.OnToggleItem(item)) },
                        onEdit = { viewModel.onIntent(ShoppingIntent.OnEditItem(item)) },
                        onDelete = { viewModel.onIntent(ShoppingIntent.OnDeleteClick(item.id)) }
                    )
                }

                if (purchased.isNotEmpty()) {
                    item(key = "purchased_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 2.dp)
                                .clickable { viewModel.onIntent(ShoppingIntent.OnTogglePurchasedVisibility) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FintrackLabelMediumText(
                                text = stringResource(Res.string.shopping_purchased),
                                color = glassColors.text3,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (state.showPurchased) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = glassColors.text3,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (state.showPurchased) {
                        items(purchased, key = { "p_${it.id}" }) { item ->
                            ShoppingRow(
                                item = item,
                                isPurchased = true,
                                onToggle = { viewModel.onIntent(ShoppingIntent.OnToggleItem(item)) },
                                onEdit = { viewModel.onIntent(ShoppingIntent.OnEditItem(item)) },
                                onDelete = { viewModel.onIntent(ShoppingIntent.OnDeleteClick(item.id)) }
                            )
                        }
                    }
                }
            }
        }

        Fab(
            label = stringResource(Res.string.shopping_add_title),
            icon = rememberVectorPainter(Icons.Default.Add),
            onClick = { viewModel.onIntent(ShoppingIntent.OnAddClick) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )
    }

    if (state.showAddSheet) {
        ShoppingItemSheet(
            item = null,
            initialCategory = null,
            mostUsedCategories = state.mostUsedCategories,
            mostUsedTags = state.mostUsedTags,
            onDismiss = { viewModel.onIntent(ShoppingIntent.OnAddSheetDismiss) },
            onConfirm = { viewModel.onIntent(ShoppingIntent.OnSaveNewItem(it)) },
            onSaveAndNext = { viewModel.onIntent(ShoppingIntent.OnSaveAndNext(it)) }
        )
    }

    state.editingItem?.let { editing ->
        ShoppingItemSheet(
            item = editing,
            initialCategory = state.editingCategory,
            mostUsedCategories = state.mostUsedCategories,
            mostUsedTags = state.mostUsedTags,
            onDismiss = { viewModel.onIntent(ShoppingIntent.OnEditItem(null)) },
            onConfirm = { viewModel.onIntent(ShoppingIntent.OnUpdateItem(it)) }
        )
    }

    if (state.showFilterSheet) {
        ShoppingFilterBottomSheet(
            selectedCategories = state.filterCategories,
            selectedTags = state.filterTags,
            onReset = { viewModel.onIntent(ShoppingIntent.OnFilterReset) },
            onDismiss = { viewModel.onIntent(ShoppingIntent.OnFilterSheetDismiss) },
            onUpdate = { cats, tags ->
                viewModel.onIntent(ShoppingIntent.OnFilterUpdate(cats, tags))
            }
        )
    }

    state.itemToDelete?.let { id ->
        val item = state.items.find { it.id == id }
        DeleteBottomSheet(
            itemName = item?.name,
            itemType = stringResource(Res.string.shopping_list),
            dismissClicked = { viewModel.onIntent(ShoppingIntent.OnDeleteCancel) },
            confirmClicked = { viewModel.onIntent(ShoppingIntent.OnDeleteItem(id)) }
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
            FintrackLabelSmallText(text = stringResource(Res.string.total_sum), fontSize = 8.sp, color = glassColors.text3)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    isPurchased: Boolean = false,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val nameColor = if (item.isChecked) glassColors.text3 else glassColors.text

    val contentAlpha = if (isPurchased) 0.5f else 1f

    GlassCard(
        padding = 10.dp,
        modifier = Modifier.alpha(contentAlpha)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CheckCircle(checked = item.isChecked, onClick = onToggle)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FintrackTitleSmallText(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        color = nameColor,
                        maxLines = 1,
                        style = if (isPurchased) MaterialTheme.typography.titleSmall.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.titleSmall
                    )
                    if (item.priority > 0 && !item.isChecked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GlassRed.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            FintrackLabelSmallText(text = "!", color = GlassRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (item.tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.tags.forEach { tag ->
                            FintrackLabelSmallText(
                                text = "#${tag.name}",
                                color = GlassGreen,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (item.estimatedPrice > 0) {
                    FintrackBodySmallText(
                        text = item.estimatedPrice.toLong().toPersianPrice() + " " + stringResource(Res.string.currency_toman),
                        color = glassColors.text3,
                        maxLines = 1
                    )
                }
                item.note?.takeIf { it.isNotBlank() }?.let {
                    FintrackBodySmallText(text = it, color = glassColors.text3, maxLines = 1)
                }
            }

            if (!isPurchased) {
                RowActionIcon(icon = Icons.Default.Edit, tint = glassColors.text2, onClick = onEdit)
            }
            RowActionIcon(icon = Icons.Default.Delete, tint = GlassRed, onClick = onDelete)
        }
    }
}

@Composable
private fun CheckCircle(checked: Boolean, onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (checked) GlassGreen else Color.Transparent)
            .border(1.5.dp, if (checked) GlassGreen else glassColors.glassEdge, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RowActionIcon(icon: ImageVector, tint: Color, onClick: () -> Unit) {
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
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShoppingItemSheet(
    item: ShoppingItem?,
    initialCategory: Category?,
    mostUsedCategories: List<Category>,
    mostUsedTags: List<Tag>,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingItem) -> Unit,
    onSaveAndNext: ((ShoppingItem) -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current
    var name by remember { mutableStateOf(item?.name ?: "") }
    var price by remember {
        mutableStateOf(item?.estimatedPrice?.takeIf { it > 0 }?.toLong()?.toString() ?: "")
    }
    var note by remember { mutableStateOf(item?.note ?: "") }
    var priority by remember { mutableStateOf(item?.priority ?: 0) }
    var reminderTime by remember { mutableStateOf(item?.reminderTime) }
    var category by remember { mutableStateOf(initialCategory) }
    var tags by remember { mutableStateOf(item?.tags?.toSet() ?: emptySet()) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    var pendingReminderDate by remember { mutableStateOf<Long?>(null) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(initialCategory) {
        if (category == null) category = initialCategory
    }

    fun buildItem(): ShoppingItem {
        val base = item ?: ShoppingItem(name = name)
        return base.copy(
            name = name.trim(),
            estimatedPrice = price.toDoubleOrNull() ?: 0.0,
            note = note.ifBlank { null },
            priority = priority,
            reminderTime = reminderTime,
            categoryId = category?.id,
            tags = tags.toList()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = if (item == null) stringResource(Res.string.shopping_add_title) else stringResource(Res.string.edit),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = { if (name.isNotBlank()) onConfirm(buildItem()) },
            onClose = onDismiss,
            showHero = false,
            tertiaryLabel = if (item == null) stringResource(Res.string.add_and_next) else null,
            onTertiaryClick = {
                if (name.isNotBlank()) {
                    onSaveAndNext?.invoke(buildItem())
                    name = ""
                    price = ""
                    note = ""
                    priority = 0
                    reminderTime = null
                    tags = emptySet()
                    focusRequester.requestFocus()
                }
            },
            preventSwipeDismiss = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    TitledInput(
                        label = stringResource(Res.string.shopping_title_label),
                        value = name,
                        onValueChange = { name = it },
                        placeholder = stringResource(Res.string.shopping_list),
                        singleLine = true,
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }

                item(key = "price") {
                    LargeAmountCard(
                        amount = price,
                        onAmountChange = { new -> price = new.filter { it.isDigit() } },
                        onCalcClick = { showCalculator = true },
                        required = false,
                        autoFocus = false
                    )
                }

                item {
                    Field(
                        label = stringResource(Res.string.category),
                        required = false,
                        onClick = { showCategoryPicker = true }
                    ) {
                        FintrackBodyMediumText(
                            text = category?.name ?: stringResource(Res.string.select_category),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    MostUsedCategoryChips(
                        items = mostUsedCategories,
                        onItemClick = { category = it }
                    )
                }

                item {
                    Field(
                        label = stringResource(Res.string.tags),
                        required = false,
                        onClick = { showTagPicker = true }
                    ) {
                        if (tags.isEmpty()) {
                            FintrackBodyMediumText(text = stringResource(Res.string.tags), color = glassColors.text3)
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tags.forEach { tag ->
                                    Chip(color = GlassGreen, onClick = { showTagPicker = true }) {
                                        FintrackLabelSmallText(text = tag.name, color = GlassGreen)
                                    }
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        MostUsedTagChips(
                            items = mostUsedTags,
                            onItemClick = { tag ->
                                tags = if (tags.contains(tag)) tags - tag else tags + tag
                            }
                        )
                    }
                }

                item {
                    TitledInput(
                        label = stringResource(Res.string.label_note),
                        value = note,
                        onValueChange = { note = it },
                        placeholder = stringResource(Res.string.hint_optional_note),
                        singleLine = false
                    )
                }

                item {
                    GlassCard(padding = 14.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Chip(
                                active = priority == 1,
                                color = GlassRed,
                                onClick = { priority = if (priority == 1) 0 else 1 }
                            ) {
                                FintrackLabelSmallText(
                                    text = if (priority == 1) stringResource(Res.string.priority_high) else stringResource(Res.string.priority_normal),
                                    color = if (priority == 1) Color.White else GlassRed
                                )
                            }
                            Box(modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDatePicker.value = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (reminderTime != null) GlassGreen else glassColors.text2,
                                    modifier = Modifier.size(18.dp)
                                )
                                FintrackLabelSmallText(
                                    text = reminderTime?.let { com.kazemieh.common.util.DateUtils.formatTimestamp(it) }
                                        ?: stringResource(Res.string.reminder),
                                    color = if (reminderTime != null) GlassGreen else glassColors.text2
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerBottomSheet(
            transactionType = TransactionType.EXPENSE,
            onCategoryClick = {
                category = it
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }

    if (showTagPicker) {
        TagPickerBottomSheet(
            selectedTags = tags,
            onSubmitClick = {
                tags = it ?: emptySet()
                showTagPicker = false
            },
            onDismiss = { showTagPicker = false }
        )
    }

    if (showCalculator) {
        CalculatorBottomSheet(
            initialAmount = price,
            onConfirm = {
                price = it
                showCalculator = false
            },
            onDismiss = { showCalculator = false }
        )
    }

    JalaliDatePickerBottomSheet(
        openSheet = showDatePicker,
        onConfirm = { calendar ->
            pendingReminderDate = calendar.toTimestamp()
            showDatePicker.value = false
            showTimePicker.value = true
        }
    )

    val tz = TimeZone.currentSystemDefault()
    val initialTime = reminderTime?.let {
        val dt = Instant.fromEpochMilliseconds(it).toLocalDateTime(tz)
        "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
    } ?: "09:00"
    FintrackTimePickerBottomSheet(
        openSheet = showTimePicker,
        initialTime = initialTime,
        onConfirm = { time ->
            val parts = time.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val base = pendingReminderDate ?: reminderTime
            if (base != null) {
                reminderTime = base + hour * 3_600_000L + minute * 60_000L
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingFilterBottomSheet(
    selectedCategories: Set<Category>,
    selectedTags: Set<Tag>,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    onUpdate: (Set<Category>, Set<Tag>) -> Unit
) {
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
                    selectedCategories = selectedCategories,
                    selectedTransactionType = TransactionType.EXPENSE,
                    isAllSelected = selectedCategories.isEmpty(),
                    onSelectionChanged = { cats, _ ->
                        // The component uses emptySet() for isAllSelected=true.
                        onUpdate(cats, selectedTags)
                    }
                )
            }

            FilterSection(title = stringResource(Res.string.tags)) {
                TagFilterSelectionContent(
                    selectedTags = selectedTags,
                    isAllSelected = selectedTags.isEmpty(),
                    onSelectionChanged = { tags, _ ->
                        onUpdate(selectedCategories, tags)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedCategoryChips(
    items: List<Category>,
    onItemClick: (Category) -> Unit
) {
    if (items.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { category ->
                Chip(color = GlassGreen, onClick = { onItemClick(category) }) {
                    FintrackLabelSmallText(text = category.name, color = GlassGreen, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedTagChips(
    items: List<Tag>,
    onItemClick: (Tag) -> Unit
) {
    if (items.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { tag ->
                Chip(color = GlassGreen, onClick = { onItemClick(tag) }) {
                    FintrackLabelSmallText(text = tag.name, color = GlassGreen, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun TitledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FintrackLabelSmallText(text = label, color = glassColors.text3)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = glassColors.text),
                keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Next else ImeAction.Default, keyboardType = keyboardType),
                keyboardActions = KeyboardActions.Default,
                cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            FintrackBodyMediumText(text = placeholder, color = glassColors.text3)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
