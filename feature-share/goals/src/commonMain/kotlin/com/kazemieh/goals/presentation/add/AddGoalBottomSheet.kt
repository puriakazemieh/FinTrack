package com.kazemieh.goals.presentation.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Goal
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    viewModel: AddGoalViewModel = koinViewModel(),
    goal: Goal? = null,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(goal) {
        viewModel.onIntent(AddGoalIntent.InitialData(goal))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is AddGoalEffect.GoalSaved) {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddGoalContent(
            state = state,
            onIntent = viewModel::onIntent,
            onClose = onDismiss
        )
    }
}

@Composable
fun AddGoalContent(
    state: AddGoalState,
    onIntent: (AddGoalIntent) -> Unit,
    onClose: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    val rainbowColors = FinTrackPickerColors.rainbow()
    val colors = rainbowColors.map { it.color }
    val selectedColorIndex = remember(state.colorId, rainbowColors) {
        rainbowColors.indexOfFirst { it.id == state.colorId }.coerceAtLeast(0)
    }
    val selectedIconIndex = remember(state.iconId) {
        FinTrackIcons.icons.indexOfFirst { it.id == state.iconId }.coerceAtLeast(0)
    }

    val openDatePicker = remember { mutableStateOf(false) }

    AddFrame(
        title = if (state.id == 0L) stringResource(Res.string.label_new_goal) else stringResource(Res.string.edit),
        sub = stringResource(Res.string.title_savings_goals),
        iconId = state.iconId,
        colorId = state.colorId,
        heroName = state.name,
        primaryLabel = stringResource(Res.string.btn_save_goal),
        onPrimaryClick = { onIntent(AddGoalIntent.SaveGoal) },
        onClose = onClose
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Field(label = stringResource(Res.string.hint_enter_goal_name), required = true) {
                    TextField(
                        value = state.name,
                        onValueChange = { onIntent(AddGoalIntent.UpdateName(it)) },
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_enter_goal_name), color = glassColors.text3) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(glassColors.bg1)
                            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space.medium)) {
                    Field(label = stringResource(Res.string.label_target_amount), required = true, modifier = Modifier.weight(1f)) {
                        TextField(
                            value = state.targetAmount,
                            onValueChange = { onIntent(AddGoalIntent.UpdateTargetAmount(it)) },
                            placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.placeholder_zero), color = glassColors.text3) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = GlassGreen
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(glassColors.bg1)
                                .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                        )
                    }

                    Field(label = stringResource(Res.string.label_saved_amount), modifier = Modifier.weight(1f)) {
                        TextField(
                            value = state.savedAmount,
                            onValueChange = { onIntent(AddGoalIntent.UpdateSavedAmount(it)) },
                            placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.placeholder_zero), color = glassColors.text3) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = GlassGreen
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(glassColors.bg1)
                                .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space.medium)) {
                    Field(label = stringResource(Res.string.label_monthly_target), modifier = Modifier.weight(1f)) {
                        TextField(
                            value = state.monthlyTarget,
                            onValueChange = { onIntent(AddGoalIntent.UpdateMonthlyTarget(it)) },
                            placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.placeholder_zero), color = glassColors.text3) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = GlassGreen
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(glassColors.bg1)
                                .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                        )
                    }

                    Field(label = stringResource(Res.string.label_end_date), modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(glassColors.bg1)
                                .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                                .clickable { openDatePicker.value = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = androidx.compose.ui.Alignment.CenterStart
                        ) {
                            FintrackBodyMediumText(
                                text = state.endDate.ifBlank { stringResource(Res.string.action_select_date) },
                                color = if (state.endDate.isNotBlank()) glassColors.text else glassColors.text3
                            )
                        }
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(text = stringResource(Res.string.label_color), color = glassColors.text3)
                        ColorSwatches(
                            colors = colors,
                            pickedIndex = selectedColorIndex,
                            onColorPick = { onIntent(AddGoalIntent.UpdateColorIcon(rainbowColors[it].id, state.iconId)) }
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
                            onIconPick = { onIntent(AddGoalIntent.UpdateColorIcon(state.colorId, FinTrackIcons.icons[it].id)) }
                        )
                    }
                }
            }
        }
    }

    JalaliDatePickerBottomSheet(
        openSheet = openDatePicker,
        onConfirm = { onIntent(AddGoalIntent.UpdateEndDate(it.toString())) }
    )
}
