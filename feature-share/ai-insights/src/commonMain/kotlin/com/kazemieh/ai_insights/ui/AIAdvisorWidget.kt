package com.kazemieh.ai_insights.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.kazemieh.common.toPersianDigits

@Composable
fun AIAdvisorWidget(
    viewModel: AIAdvisorViewModel = koinViewModel(),
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val glassColors = LocalGlassColors.current

    WidgetCard(
        title = stringResource(Res.string.ai_advisor_title),
        onMore = onMore,
        modifier = modifier
    ) {
        if (state.isEmpty) {
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = stringResource(Res.string.ai_empty_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = glassColors.text3
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.ai_saving_potential_label),
                        color = glassColors.text2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MoneyText(amount = state.savingPotentialAmount, size = 18)
                }

                Box(
                    modifier = Modifier
                        .size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "٪${state.savingPotentialPercentage.toPersianDigits()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
