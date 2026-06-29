package com.kazemieh.goals.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.goals.presentation.add.AddGoalBottomSheet
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_active_goals
import fintrack.core.designsystem.generated.resources.label_goal_summary
import fintrack.core.designsystem.generated.resources.label_monthly_target
import fintrack.core.designsystem.generated.resources.label_total_saved
import fintrack.core.designsystem.generated.resources.title_savings_goals
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalScreen(
    viewModel: GoalViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FintrackScreen(
        title = stringResource(Res.string.title_savings_goals),
        sub = stringResource(Res.string.label_active_goals, state.goals.size),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.title_savings_goals),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(GoalIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(GoalIntent.ShowAddGoal()) },
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_total_saved),
                        value = state.totalSavedAmount.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman)
                    ),
                    EntitySummary(
                        label = UiText.DynamicString(
                            stringResource(
                                Res.string.label_goal_summary,
                                state.totalTargetAmount.toPersianPrice(),
                                state.totalPercent
                            )
                        ),
                        value = "${state.totalPercent}%",
                        color = GlassGreen
                    )
                ),
                items = state.goals.map { goal ->
                    EntityItem(
                        id = goal.id,
                        name = goal.name,
                        sub = stringResource(
                            Res.string.label_monthly_target,
                            goal.monthlyTarget.toPersianPrice()
                        ) + (goal.endDate?.let { " · تا $it" } ?: ""),
                        badge = "${goal.percent}%",
                        colorId = goal.colorId,
                        iconId = goal.iconId,
                    )
                },
                onEditClick = { item ->
                    state.goals.find { it.id == item.id }?.let {
                        viewModel.onIntent(GoalIntent.ShowAddGoal(it))
                    }
                },
                onDeleteClick = { viewModel.onIntent(GoalIntent.DeleteGoal(it.id)) },
                showActions = true
            )
        }

        if (state.isAddGoalShow) {
            AddGoalBottomSheet(
                goal = state.selectedGoal,
                onDismiss = { viewModel.onIntent(GoalIntent.ShowAddGoal()) }
            )
        }
    }
}
