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
        title = if (state.mode == AddCategoryMode.Add) "دسته‌بندی جدید" else "ویرایش دسته‌بندی",
        sub = "برای گروه‌بندی تراکنش‌ها",
        iconId = state.draft.iconId,
        colorId = state.draft.colorId,
        primaryLabel = "ذخیره دسته",
        onPrimaryClick = { onIntent(AddCategoryIntent.Save) },
        onClose = { onIntent(AddCategoryIntent.OnDismiss) }
    ) {
        TypeSelector(
            selectedType = state.draft.type,
            onTypeSelected = { onIntent(AddCategoryIntent.UpdateType(it)) }
        )

        Field(label = "نام دسته", required = true) {
            TextField(
                value = state.draft.name,
                onValueChange = { onIntent(AddCategoryIntent.UpdateName(it)) },
                placeholder = { Text("نام دسته را وارد کنید...", fontSize = 13.sp, color = GlassText3) },
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

        GlassCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "رنگ", style = MaterialTheme.typography.labelMedium, color = GlassText3)
                ColorSwatches(
                    colors = colors,
                    pickedIndex = selectedColorIndex,
                    onColorPick = { onIntent(AddCategoryIntent.SetColorIcon(rainbowColors[it].id, state.draft.iconId)) }
                )
            }
        }

        GlassCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "آیکن", style = MaterialTheme.typography.labelMedium, color = GlassText3)
                IconGrid(
                    icons = FinTrackIcons.icons,
                    pickedIndex = selectedIconIndex,
                    color = rainbowColors[selectedColorIndex].color,
                    onIconPick = { onIntent(AddCategoryIntent.SetColorIcon(state.draft.colorId, FinTrackIcons.icons[it].id)) },
                    modifier = Modifier.height(200.dp)
                )
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
        TransactionType.EXPENSE to "خرج",
        TransactionType.INCOME to "درآمد",
        TransactionType.TRANSFER to "انتقال"
    )

    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "نوع", style = MaterialTheme.typography.labelSmall, color = GlassText3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { (type, label) ->
                    val active = type == selectedType
                    val color = when (type) {
                        TransactionType.INCOME -> GlassGreen
                        TransactionType.TRANSFER -> GlassBlue
                        else -> GlassRed
                    }
                    val bgColor = when (type) {
                        TransactionType.INCOME -> GlassGreenSoft
                        TransactionType.TRANSFER -> GlassBlueSoft
                        else -> GlassRedSoft
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) bgColor else Color.Transparent)
                            .then(
                                if (active) Modifier.border(1.dp, color.copy(alpha = 0.33f), RoundedCornerShape(10.dp))
                                else Modifier.border(1.dp, GlassEdge, RoundedCornerShape(10.dp))
                            )
                            .clickable { onTypeSelected(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) color else GlassText3
                        )
                    }
                }
            }
        }
    }
}
