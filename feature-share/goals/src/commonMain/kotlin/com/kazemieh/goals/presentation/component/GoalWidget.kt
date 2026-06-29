package com.kazemieh.goals.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.goals.presentation.GoalViewModel
import com.kazemieh.designsystem.component.CircularProgress
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalWidget(
    viewModel: GoalViewModel = koinViewModel(),
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    WidgetCard(
        title = stringResource(Res.string.title_savings_goals),
        onMore = onMore,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgress(progress = state.totalProgress)
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.percentage_format, state.totalPercent),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    FintrackLabelSmallText(text = stringResource(Res.string.label_total_saved))
                    MoneyText(amount = state.totalSavedAmount, size = 16)
                }
            }
        }
    }
}
