package com.kazemieh.category.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    viewModel: AddCategoryViewModel = koinViewModel(),
    transactionType: TransactionType,
    selectedCategory: Category? = null,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Category) -> Unit)? = null,
    setCategory: (Category) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedCategory?.id, transactionType) {
        if (selectedCategory != null) {
            viewModel.onIntent(AddCategoryIntent.StartEdit(selectedCategory))
        } else {
            viewModel.onIntent(AddCategoryIntent.StartAdd(transactionType))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddCategoryEffect.SavedCategory -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setCategory(effect.category)
                        }
                    }
                }
                AddCategoryEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddCategoryIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddCategoryContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToTransactions = onNavigateToTransactions
        )
    }
}

@Composable
fun AddCategoryContent(
    state: AddCategoryState,
    onIntent: (AddCategoryIntent) -> Unit,
    onNavigateToTransactions: ((Category) -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    val rainbowColors = FinTrackPickerColors.rainbow()
    val colors = rainbowColors.map { it.color }
    val selectedColorIndex = remember(state.draft.colorId, rainbowColors) {
        rainbowColors.indexOfFirst { it.id == state.draft.colorId }.coerceAtLeast(0)
    }
    val selectedIconIndex = remember(state.draft.iconId) {
        FinTrackIcons.icons.indexOfFirst { it.id == state.draft.iconId }.coerceAtLeast(0)
    }

    val backgroundBrush = remember(state.draft.type) {
        val topColor: Color = when (state.draft.type) {
            TransactionType.EXPENSE -> GlassRedSoft
            TransactionType.INCOME -> GlassGreenSoft
            TransactionType.TRANSFER -> GlassBlueSoft
            else -> GlassBg1
        }
        Brush.verticalGradient(listOf(topColor, glassColors.bg1, glassColors.bg0))
    }

    AddFrame(
        title = if (state.mode == AddCategoryMode.Add) stringResource(Res.string.title_new_category) else stringResource(Res.string.title_edit_category),
        sub = stringResource(Res.string.title_category_management),
        iconId = state.draft.iconId,
        colorId = state.draft.colorId,
        heroName = state.draft.name,
        primaryLabel = stringResource(Res.string.btn_save_category),
        onPrimaryClick = { onIntent(AddCategoryIntent.Save) },
        onClose = { onIntent(AddCategoryIntent.OnDismiss) },
        onFilterClick = if (state.mode is AddCategoryMode.Edit && onNavigateToTransactions != null) {
            {
                state.mode.categoryId.let { id ->
                    onNavigateToTransactions(
                        Category(
                            id = id,
                            name = state.draft.name,
                            type = state.draft.type,
                            colorId = state.draft.colorId ?: 1,
                            iconId = state.draft.iconId ?: 1
                        )
                    )
                    onIntent(AddCategoryIntent.OnDismiss)
                }
            }
        } else null,
        backgroundBrush = backgroundBrush
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                TypeSelector(
                    selectedType = state.draft.type,
                    onTypeSelected = { onIntent(AddCategoryIntent.UpdateType(it)) }
                )
            }

            item {
                Field(label = stringResource(Res.string.category_name_label), required = true) {
                    TextField(
                        value = state.draft.name,
                        onValueChange = { onIntent(AddCategoryIntent.UpdateName(it)) },
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_enter_category_name), color = glassColors.text3) },
                        colors = glassTextFieldColors(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                ParentCategorySelector(
                    parentCategories = state.parentCategories,
                    selectedParentId = state.draft.parentId,
                    currentCategoryId = (state.mode as? AddCategoryMode.Edit)?.categoryId,
                    onParentSelected = { onIntent(AddCategoryIntent.UpdateParentId(it)) }
                )
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(text = stringResource(Res.string.label_color), color = glassColors.text3)
                        ColorSwatches(
                            colors = colors,
                            pickedIndex = selectedColorIndex,
                            onColorPick = { onIntent(AddCategoryIntent.SetColorIcon(rainbowColors[it].id, state.draft.iconId)) }
                        )
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(text = stringResource(Res.string.label_icon), color = glassColors.text3)
                        IconGrid(
                            icons = FinTrackIcons.icons,
                            pickedIndex = selectedIconIndex,
                            color = rainbowColors[selectedColorIndex].color,
                            onIconPick = { onIntent(AddCategoryIntent.SetColorIcon(state.draft.colorId, FinTrackIcons.icons[it].id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentCategorySelector(
    parentCategories: List<Category>,
    selectedParentId: Long?,
    currentCategoryId: Long?,
    onParentSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedParent = parentCategories.find { it.id == selectedParentId }

    val availableParents = parentCategories.filter { it.id != currentCategoryId }

    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FintrackLabelSmallText(text = stringResource(Res.string.label_parent_category), color = glassColors.text3)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassColors.bg1)
                    .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                    .clickable { expanded = !expanded }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FintrackBodyMediumText(
                        text = selectedParent?.name ?: stringResource(Res.string.label_parent_category_optional),
                        color = if (selectedParent != null) glassColors.text else glassColors.text3
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = glassColors.text3,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option for "None"
                    ParentCategoryItem(
                        name = stringResource(Res.string.label_no_parent_category),
                        isSelected = selectedParentId == null,
                        onClick = {
                            onParentSelected(null)
                            expanded = false
                        }
                    )
                    
                    availableParents.forEach { category ->
                        ParentCategoryItem(
                            name = category.name,
                            isSelected = category.id == selectedParentId,
                            onClick = {
                                onParentSelected(category.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentCategoryItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GlassGreen.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        FintrackBodyMediumText(
            text = name,
            color = if (isSelected) GlassGreen else glassColors.text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
@Composable
private fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val types = listOf(
        TransactionType.EXPENSE to stringResource(Res.string.label_expense),
        TransactionType.INCOME to stringResource(Res.string.label_income)
    )

    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FintrackLabelSmallText(text = stringResource(Res.string.label_type), color = glassColors.text3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { (type, label) ->
                    val active = type == selectedType
                    val color = if (type == TransactionType.INCOME) GlassGreen else GlassRed
                    val bgColor = if (type == TransactionType.INCOME) GlassGreenSoft else GlassRedSoft

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) bgColor else Color.Transparent)
                            .then(
                                if (active) Modifier.border(
                                    1.dp,
                                    color.copy(alpha = 0.33f),
                                    RoundedCornerShape(10.dp)
                                )
                                else Modifier.border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                            )
                            .clickable { onTypeSelected(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FintrackBodyMediumText(
                            text = label,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) color else glassColors.text3
                        )
                    }
                }
            }
        }
    }
}

