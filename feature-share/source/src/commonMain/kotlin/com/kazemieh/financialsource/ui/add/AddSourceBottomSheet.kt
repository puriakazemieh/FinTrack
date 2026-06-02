package com.kazemieh.financialsource.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.CardItem
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.ColorSwatches
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.IconGrid
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.designsystem.model.Bank
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.picker.FinTrackSourceIcons
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceBottomSheet(
    viewModel: AddSourceViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedSource: Source? = null,
    onDismiss: () -> Unit,
    setSource: (Source) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedSource?.id) {
        if (selectedSource != null) viewModel.onIntent(AddSourceIntent.StartEdit(selectedSource))
        else viewModel.onIntent(AddSourceIntent.StartAdd)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddSourceEffect.SavedSource -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setSource(effect.source)
                            onDismiss()
                        }
                    }
                }

                AddSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddSourceIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddSourceContent(
            state = state,
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
fun AddSourceContent(
    state: AddSourceState,
    onIntent: (AddSourceIntent) -> Unit
) {
    val rainbowColors = FinTrackPickerColors.rainbow()
    val colors = rainbowColors.map { it.color }
    val selectedColorIndex = remember(state.draft.colorId, rainbowColors) {
        rainbowColors.indexOfFirst { it.id == state.draft.colorId }.coerceAtLeast(0)
    }
    val selectedIconIndex = remember(state.draft.iconId) {
        FinTrackSourceIcons.icons.indexOfFirst { it.id == state.draft.iconId }.coerceAtLeast(0)
    }

    AddFrame(
        title = if (state.mode == AddSourceMode.Add) "منبع مالی جدید" else "ویرایش منبع مالی",
        sub = "مدیریت موجودی و حساب‌ها",
        primaryLabel = "ذخیره منبع",
        onPrimaryClick = { onIntent(AddSourceIntent.Save) },
        onClose = { onIntent(AddSourceIntent.OnDismiss) },
        hero = {
            if (state.draft.type == TypeSource.CREDIT) {
                val bank = remember(state.draft.cardNumber) {
                    Bank.fromCardNumber(state.draft.cardNumber.orEmpty())
                }
                CardItem(
                    name = state.draft.name,
                    cardNumber = state.draft.cardNumber.orEmpty(),
                    bank = bank,
                    iconId = state.draft.iconId
                )
            } else {
                FinTrackLeadingIcon(
                    colorId = state.draft.colorId ?: 1,
                    iconId = state.draft.iconId ?: 1,
                    style = LeadingIconStyle.Badge,
                    size = 64.dp,
                    iconSize = 32.dp
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SourceTypeSelector(
                    selectedType = state.draft.type,
                    onTypeSelected = { onIntent(AddSourceIntent.UpdateType(it)) }
                )
            }

            item {
                Field(label = "نام منبع", required = true) {
                    TextField(
                        value = state.draft.name,
                        onValueChange = { onIntent(AddSourceIntent.UpdateName(it)) },
                        placeholder = {
                            Text(
                                "مثلاً کارت اصلی یا کیف پول",
                                fontSize = 13.sp,
                                color = GlassText3
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = GlassText,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Field(label = "موجودی اولیه") {
                    TextField(
                        value = if (state.draft.balance == 0) "" else state.draft.balance.toString(),
                        onValueChange = { input ->
                            val newValue = input.toIntOrNull() ?: 0
                            onIntent(AddSourceIntent.UpdateBalance(newValue))
                        },
                        placeholder = { Text("0", fontSize = 13.sp, color = GlassText3) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = GlassGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("تومان", fontSize = 12.sp, color = GlassText3) }
                    )
                }
            }

            if (state.draft.type == TypeSource.CREDIT) {
                item {
                    GlassCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "اطلاعات کارت",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassText3
                            )

                            TextField(
                                value = state.draft.cardNumber.orEmpty(),
                                onValueChange = { onIntent(AddSourceIntent.UpdateCardNumber(it)) },
                                placeholder = {
                                    Text(
                                        "شماره ۱۶ رقمی کارت",
                                        fontSize = 13.sp,
                                        color = GlassText3
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = state.draft.cvv2.orEmpty(),
                                    onValueChange = { onIntent(AddSourceIntent.UpdateCvv2(it)) },
                                    placeholder = {
                                        Text(
                                            "CVV2",
                                            fontSize = 13.sp,
                                            color = GlassText3
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                TextField(
                                    value = state.draft.expirationMonth.orEmpty(),
                                    onValueChange = {
                                        onIntent(
                                            AddSourceIntent.UpdateExpirationMonth(
                                                it
                                            )
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            "ماه",
                                            fontSize = 13.sp,
                                            color = GlassText3
                                        )
                                    },
                                    modifier = Modifier.weight(0.5f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                TextField(
                                    value = state.draft.expirationYear.orEmpty(),
                                    onValueChange = {
                                        onIntent(
                                            AddSourceIntent.UpdateExpirationYear(
                                                it
                                            )
                                        )
                                    },
                                    placeholder = {
                                        Text(
                                            "سال",
                                            fontSize = 13.sp,
                                            color = GlassText3
                                        )
                                    },
                                    modifier = Modifier.weight(0.5f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    GlassCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "حساب و شبا",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassText3
                            )
                            TextField(
                                value = state.draft.shabaNumber.orEmpty(),
                                onValueChange = { onIntent(AddSourceIntent.UpdateShabaNumber(it)) },
                                placeholder = {
                                    Text(
                                        "شماره شبا (بدون IR)",
                                        fontSize = 13.sp,
                                        color = GlassText3
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                            TextField(
                                value = state.draft.accountNumber.orEmpty(),
                                onValueChange = { onIntent(AddSourceIntent.UpdateAccountNumber(it)) },
                                placeholder = {
                                    Text(
                                        "شماره حساب",
                                        fontSize = 13.sp,
                                        color = GlassText3
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                item {
                    GlassCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "شعبه",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassText3
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = state.draft.branchCode.orEmpty(),
                                    onValueChange = { onIntent(AddSourceIntent.UpdateBranchCode(it)) },
                                    placeholder = {
                                        Text(
                                            "کد شعبه",
                                            fontSize = 13.sp,
                                            color = GlassText3
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                TextField(
                                    value = state.draft.branchName.orEmpty(),
                                    onValueChange = { onIntent(AddSourceIntent.UpdateBranchName(it)) },
                                    placeholder = {
                                        Text(
                                            "نام شعبه",
                                            fontSize = 13.sp,
                                            color = GlassText3
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "رنگ",
                            style = MaterialTheme.typography.labelMedium,
                            color = GlassText3
                        )
                        ColorSwatches(
                            colors = colors,
                            pickedIndex = selectedColorIndex,
                            onColorPick = {
                                onIntent(
                                    AddSourceIntent.SetColorIcon(
                                        rainbowColors[it].id,
                                        state.draft.iconId
                                    )
                                )
                            }
                        )
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "آیکن",
                            style = MaterialTheme.typography.labelMedium,
                            color = GlassText3
                        )
                        IconGrid(
                            icons = if (state.draft.type == TypeSource.CREDIT) FinTrackSourceIcons.icons else FinTrackIcons.icons,
                            pickedIndex = selectedIconIndex,
                            color = rainbowColors[selectedColorIndex].color,
                            onIconPick = {
                                onIntent(
                                    AddSourceIntent.SetColorIcon(
                                        state.draft.colorId,
                                        if (state.draft.type == TypeSource.CREDIT) FinTrackSourceIcons.icons[it].id else FinTrackIcons.icons[it].id
                                    )
                                )
                            },
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceTypeSelector(
    selectedType: TypeSource,
    onTypeSelected: (TypeSource) -> Unit
) {
    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "نوع منبع", style = MaterialTheme.typography.labelSmall, color = GlassText3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TypeSource.entries.forEach { option ->
                    val active = option == selectedType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) GlassGreenSoft else Color.Transparent)
                            .then(
                                if (active) Modifier.border(
                                    1.dp,
                                    GlassGreen.copy(alpha = 0.33f),
                                    RoundedCornerShape(10.dp)
                                )
                                else Modifier.border(1.dp, GlassEdge, RoundedCornerShape(10.dp))
                            )
                            .clickable { onTypeSelected(option) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.value.asString(),
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) GlassGreen else GlassText3
                        )
                    }
                }
            }
        }
    }
}

