package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssetWidget(
    viewModel: AssetViewModel = koinViewModel(),
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    WidgetCard(
        title = "دارایی‌ها",
        onMore = onMoreClick,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    FintrackLabelSmallText(text = "ارزش کل")
                    MoneyText(amount = state.totalValue, size = 18)
                }
            }

            // Mini Composition Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(LocalGlassColors.current.text.copy(alpha = 0.05f))
            ) {
                state.composition.forEach { (type, percentage) ->
                    Box(
                        modifier = Modifier
                            .weight(percentage.toFloat().coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color((type.hashCode() * 0xFFFFFF) or 0xFF000000.toInt()))
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.composition.entries.toList().take(3).forEach { entry ->
                    val type = entry.key
                    val percentage = entry.value
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color((type.hashCode() * 0xFFFFFF) or 0xFF000000.toInt()))
                        )
                        Spacer(Modifier.width(4.dp))
                        FintrackLabelSmallText(
                            text = "${type.name}: ${(percentage * 100).toInt()}%"
                        )
                    }
                }
            }
        }
    }
}
