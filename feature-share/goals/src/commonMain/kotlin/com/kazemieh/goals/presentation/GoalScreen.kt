package com.kazemieh.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.border
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.goals.presentation.add.AddGoalBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalScreen(
    viewModel: GoalViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current
    val haptics = androidx.compose.ui.hapticfeedback.LocalHapticFeedback.current

    var showAddAmountDialog by remember { mutableStateOf(false) }
    var selectedGoalId by remember { mutableLongStateOf(0L) }
    var amountToAdd by remember { mutableStateOf("") }

    FintrackScreen(
        title = stringResource(Res.string.title_savings_goals),
        sub = stringResource(Res.string.label_active_goals, state.goals.size),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(space.medium),
                verticalArrangement = Arrangement.spacedBy(space.medium)
            ) {
                item {
                    GoalSummaryHero(state)
                }

                item {
                    RoundUpSummaryCard(
                        state = state,
                        onClick = { viewModel.onIntent(GoalIntent.ToggleRoundUpSettings) }
                    )
                }

                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { viewModel.onIntent(GoalIntent.UpdateSearchQuery(it)) },
                        placeholder = stringResource(Res.string.hint_search_in, stringResource(Res.string.title_savings_goals))
                    )
                }

                items(state.goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onAddAmount = {
                            selectedGoalId = goal.id
                            amountToAdd = ""
                            showAddAmountDialog = true
                        },
                        onEdit = { viewModel.onIntent(GoalIntent.ShowAddGoal(goal)) }
                    )
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }

        FAB(onClick = { viewModel.onIntent(GoalIntent.ShowAddGoal()) })

        if (state.isAddGoalShow) {
            AddGoalBottomSheet(
                goal = state.selectedGoal,
                onDismiss = { viewModel.onIntent(GoalIntent.ShowAddGoal()) }
            )
        }

        if (showAddAmountDialog) {
            AddAmountDialog(
                onDismiss = { showAddAmountDialog = false },
                onConfirm = {
                    amountToAdd.toLongOrNull()?.let {
                        viewModel.onIntent(GoalIntent.AddAmountToGoal(selectedGoalId, it))
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    showAddAmountDialog = false
                },
                amountValue = amountToAdd,
                onAmountChange = { amountToAdd = it }
            )
        }

        if (state.showRoundUpSettings) {
            RoundUpSettingsSheet(
                state = state,
                onDismiss = { viewModel.onIntent(GoalIntent.ToggleRoundUpSettings) },
                onToggleEnabled = { viewModel.onIntent(GoalIntent.ToggleRoundUpEnabled) },
                onSelectGoal = { viewModel.onIntent(GoalIntent.SetRoundUpGoal(it)) },
                onSelectUnit = { viewModel.onIntent(GoalIntent.SetRoundUpUnit(it)) }
            )
        }
    }
}

@Composable
private fun RoundUpSummaryCard(state: GoalState, onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    val goalName = state.goals.find { it.id == state.roundUpGoalId }?.name
    val subtitle = when {
        !state.isRoundUpEnabled -> stringResource(Res.string.label_roundup_off)
        goalName != null -> stringResource(Res.string.label_roundup_unit_amount, state.roundUpUnit.toString().toPersianDigits()) + " · " + goalName
        else -> stringResource(Res.string.label_roundup_no_goal)
    }

    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Savings,
                contentDescription = null,
                tint = if (state.isRoundUpEnabled) GlassGreen else glassColors.text3
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_title))
                FintrackBodyMediumText(text = subtitle, color = glassColors.text3)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundUpSettingsSheet(
    state: GoalState,
    onDismiss: () -> Unit,
    onToggleEnabled: () -> Unit,
    onSelectGoal: (Long) -> Unit,
    onSelectUnit: (Long) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    val units = listOf(1_000L, 5_000L, 10_000L)

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = glassColors.bg0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = space.large)
                .padding(bottom = space.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_title), fontWeight = FontWeight.Bold)
                    FintrackBodyMediumText(text = stringResource(Res.string.label_roundup_desc), color = glassColors.text3)
                }
                com.kazemieh.designsystem.component.glass.Switch(on = state.isRoundUpEnabled, onToggle = { onToggleEnabled() })
            }

            if (state.isRoundUpEnabled) {
                Spacer(Modifier.height(space.medium))
                FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_target_goal), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (state.goals.isEmpty()) {
                    FintrackBodyMediumText(text = stringResource(Res.string.label_roundup_no_goals_yet), color = glassColors.text3)
                } else {
                    state.goals.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onSelectGoal(goal.id) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = state.roundUpGoalId == goal.id,
                                onClick = { onSelectGoal(goal.id) },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = GlassGreen)
                            )
                            FintrackBodyMediumText(text = goal.name)
                        }
                    }
                }

                Spacer(Modifier.height(space.medium))
                FintrackTitleMediumText(text = stringResource(Res.string.label_roundup_unit), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    units.forEach { unit ->
                        val selected = state.roundUpUnit == unit
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(if (selected) GlassGreen.copy(alpha = 0.16f) else glassColors.glass)
                                .border(
                                    1.dp,
                                    if (selected) GlassGreen else glassColors.glassEdge,
                                    MaterialTheme.shapes.medium
                                )
                                .clickable { onSelectUnit(unit) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.label_roundup_unit_amount, unit.toString().toPersianDigits()),
                                color = if (selected) GlassGreen else glassColors.text2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSummaryHero(state: GoalState) {
    val glassColors = LocalGlassColors.current
    val color = MaterialTheme.colorScheme.primary

    GlassCard(
        tone = GlassTone.Strong,
        padding = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        0f to color.copy(alpha = 0.16f),
                        0.7f to Color.Transparent
                    )
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.label_total_saved),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3
                )
                Text(
                    text = state.totalSavedAmount.toPersianPrice(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.text
                )
                Text(
                    text = stringResource(
                        Res.string.label_goal_summary,
                        state.totalTargetAmount.toPersianPrice(),
                        state.totalPercent
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: com.kazemieh.common.model.Goal,
    onAddAmount: () -> Unit,
    onEdit: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val color = MaterialTheme.colorScheme.primary // Should use goal.colorId mapping if available

    GlassCard(padding = 14.dp) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                FinTrackLeadingIcon(
                    colorId = goal.colorId,
                    iconId = goal.iconId,
                    style = LeadingIconStyle.Badge,
                    size = 40.dp,
                    iconSize = 18.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.text
                    )
                    Text(
                        text = stringResource(Res.string.label_monthly_target_with_val, goal.monthlyTarget.toPersianPrice()) + 
                               (goal.endDate?.let { " · " + stringResource(Res.string.label_goal_eta, it) } ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = glassColors.text3
                    )
                }

                Text(
                    text = "${goal.percent}%".toPersianDigits(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(glassColors.glassHairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.percent / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.savedAmount.toPersianPrice(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = goal.targetAmount.toPersianPrice() + " " + stringResource(Res.string.unit_toman_short),
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.text3
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onAddAmount,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "+ " + stringResource(Res.string.label_add_to_goal),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = glassColors.bg0,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium)
        ) {
            FintrackTitleMediumText(
                text = stringResource(Res.string.label_add_to_goal),
                fontWeight = FontWeight.Bold,
                color = glassColors.text
            )

            TextField(
                value = amountValue,
                onValueChange = onAmountChange,
                placeholder = {
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.amount),
                        color = glassColors.text3
                    )
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = GlassGreen
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = glassColors.text,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(glassColors.bg1)
                    .border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space.medium)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glassColors.glass,
                        contentColor = glassColors.text
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(Res.string.cancell_), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(Res.string.confirm), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(space.medium))
        }
    }
}
