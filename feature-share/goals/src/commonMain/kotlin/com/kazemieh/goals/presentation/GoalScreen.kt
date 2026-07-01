package com.kazemieh.goals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
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

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { viewModel.onIntent(GoalIntent.ShowAddGoal()) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(space.large),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(99.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.label_new_goal), fontWeight = FontWeight.Bold)
                }
            }
        }

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
                    }
                    showAddAmountDialog = false
                },
                amountValue = amountToAdd,
                onAmountChange = { amountToAdd = it }
            )
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

@Composable
private fun AddAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.label_add_to_goal)) },
        text = {
            OutlinedTextField(
                value = amountValue,
                onValueChange = onAmountChange,
                label = { Text(stringResource(Res.string.amount)) },
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancell_))
            }
        }
    )
}
