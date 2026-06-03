package com.kazemieh.category.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    snackbarHostState: SnackbarHostState,
    transactionType: TransactionType,
    selectedCategory: Category? = null,
    onDismiss: () -> Unit,
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
                is AddCategoryEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message.resolveString(),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is AddCategoryEffect.SavedCategory -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setCategory(effect.category)
                            onDismiss()
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
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
fun AddCategoryContent(
    state: AddCategoryState,
    onIntent: (AddCategoryIntent) -> Unit
) {
    val space = LocalSpacing.current
    val rainbowColors = FinTrackPickerColors.rainbow()
    val colors = rainbowColors.map { it.color }
    val selectedColorIndex = remember(state.draft.colorId, rainbowColors) {
        rainbowColors.indexOfFirst { it.id == state.draft.colorId }.coerceAtLeast(0)
    }
    val selectedIconIndex = remember(state.draft.iconId) {
        FinTrackIcons.icons.indexOfFirst { it.id == state.draft.iconId }.coerceAtLeast(0)
    }

    AddFrame(
        title = if (state.mode == AddCategoryMode.Add) stringResource(Res.string.title_new_category) else stringResource(Res.string.title_edit_category),
        sub = stringResource(Res.string.title_category_management),
        iconId = state.draft.iconId,
        colorId = state.draft.colorId,
        heroName = state.draft.name,
        primaryLabel = stringResource(Res.string.btn_save_category),
        onPrimaryClick = { onIntent(AddCategoryIntent.Save) },
        onClose = { onIntent(AddCategoryIntent.OnDismiss) }
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
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_enter_category_name), color = GlassText3) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = GlassText, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(text = stringResource(Res.string.label_color), color = GlassText3)
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
                        FintrackLabelMediumText(text = stringResource(Res.string.label_icon), color = GlassText3)
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
private fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    val types = listOf(
        TransactionType.EXPENSE to stringResource(Res.string.label_expense),
        TransactionType.INCOME to stringResource(Res.string.label_income)
    )

    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FintrackLabelSmallText(text = stringResource(Res.string.label_type), color = GlassText3)
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
                                else Modifier.border(1.dp, GlassEdge, RoundedCornerShape(10.dp))
                            )
                            .clickable { onTypeSelected(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FintrackBodyMediumText(
                            text = label,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) color else GlassText3
                        )
                    }
                }
            }
        }
    }
}

