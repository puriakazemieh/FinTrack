package com.kazemieh.shopping.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.ShoppingItem
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Fab
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.estimated_price
import fintrack.core.designsystem.generated.resources.hint_optional_note
import fintrack.core.designsystem.generated.resources.label_note
import fintrack.core.designsystem.generated.resources.priority_high
import fintrack.core.designsystem.generated.resources.priority_normal
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.shopping_add_title
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.shopping_purchased
import fintrack.core.designsystem.generated.resources.shopping_title_label
import fintrack.core.designsystem.generated.resources.total_sum
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val active = state.filteredItems.filter { !it.isChecked }
    val purchased = state.filteredItems.filter { it.isChecked }
    val total = state.items.filter { !it.isChecked }.sumOf { it.estimatedPrice }

    FintrackScreen(
        title = stringResource(Res.string.shopping_list),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SummaryCard(total = total.toLong())

            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(ShoppingIntent.UpdateSearchQuery(it)) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )

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
                        onDelete = { viewModel.onIntent(ShoppingIntent.OnDeleteItem(item.id)) }
                    )
                }

                if (purchased.isNotEmpty()) {
                    item(key = "purchased_header") {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.shopping_purchased),
                            color = LocalGlassColors.current.text3,
                            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
                        )
                    }
                    items(purchased, key = { "p_${it.id}" }) { item ->
                        ShoppingRow(
                            item = item,
                            onToggle = { viewModel.onIntent(ShoppingIntent.OnToggleItem(item)) },
                            onEdit = { viewModel.onIntent(ShoppingIntent.OnEditItem(item)) },
                            onDelete = { viewModel.onIntent(ShoppingIntent.OnDeleteItem(item.id)) }
                        )
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
            onDismiss = { viewModel.onIntent(ShoppingIntent.OnAddSheetDismiss) },
            onConfirm = { viewModel.onIntent(ShoppingIntent.OnSaveNewItem(it)) }
        )
    }

    state.editingItem?.let { editing ->
        ShoppingItemSheet(
            item = editing,
            initialCategory = state.editingCategory,
            onDismiss = { viewModel.onIntent(ShoppingIntent.OnEditItem(null)) },
            onConfirm = { viewModel.onIntent(ShoppingIntent.OnUpdateItem(it)) }
        )
    }
}

@Composable
private fun SummaryCard(total: Long) {
    val glassColors = LocalGlassColors.current
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        padding = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FintrackLabelSmallText(text = stringResource(Res.string.total_sum), color = glassColors.text3)
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
private fun ShoppingRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val nameColor = if (item.isChecked) glassColors.text3 else glassColors.text
    GlassCard(padding = 10.dp) {
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
                        maxLines = 1
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

            RowActionIcon(icon = Icons.Default.Edit, tint = glassColors.text2, onClick = onEdit)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingItemSheet(
    item: ShoppingItem?,
    initialCategory: Category?,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var price by remember {
        mutableStateOf(item?.estimatedPrice?.takeIf { it > 0 }?.toLong()?.toString() ?: "")
    }
    var note by remember { mutableStateOf(item?.note ?: "") }
    var priority by remember { mutableStateOf(item?.priority ?: 0) }
    var reminderTime by remember { mutableStateOf(item?.reminderTime) }
    var category by remember { mutableStateOf(initialCategory) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }
    var pendingReminderDate by remember { mutableStateOf<Long?>(null) }

    // Keep the selected category in sync once it resolves for an existing item.
    androidx.compose.runtime.LaunchedEffect(initialCategory) {
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
            categoryId = category?.id
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
            showHero = false
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
                        singleLine = true
                    )
                }

                item(key = "price") {
                    TitledInput(
                        label = stringResource(Res.string.estimated_price),
                        value = price,
                        onValueChange = { new -> price = new.filter { it.isDigit() } },
                        placeholder = stringResource(Res.string.estimated_price),
                        singleLine = true,
                        keyboardType = KeyboardType.Number
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
                            RowActionIcon(
                                icon = Icons.Default.Notifications,
                                tint = if (reminderTime != null) GlassGreen else LocalGlassColors.current.text2,
                                onClick = { showDatePicker.value = true }
                            )
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

    // Reminder: pick a date, then a time, then combine both into the reminder timestamp.
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

@Composable
private fun TitledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp) {
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
