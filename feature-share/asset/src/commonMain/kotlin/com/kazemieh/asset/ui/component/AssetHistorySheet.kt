package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetHistorySheet(
    asset: Asset,
    onDismiss: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(asset.id) {
        asset.id?.let {
            viewModel.onIntent(AssetIntent.LoadHistory(it, asset.type))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                FintrackTitleLargeText(stringResource(Res.string.title_price_history, asset.name))
                Spacer(modifier = Modifier.height(16.dp))

                AssetProfitLossChart(asset = asset, history = state.history)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.history) { historyItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FintrackBodyMediumText(DateUtils.formatTimestamp(historyItem.date.toEpochMilliseconds()))
                            FintrackBodyMediumText(historyItem.price.toSignedPersianPrice())
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    
                    if (state.history.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                FintrackBodyMediumText(stringResource(Res.string.msg_empty_list))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Profit/loss is calculated from the owner's actual quantity and acquisition cost, not from the
 * unit-price movement alone.  This keeps gold, FX and fractional crypto positions comparable.
 */
@Composable
private fun AssetProfitLossChart(asset: Asset, history: List<com.kazemieh.common.model.AssetHistory>) {
    val points = remember(asset, history) {
        buildList {
            add(0L)
            history.sortedBy { it.date }.forEach { snapshot ->
                add((asset.quantity * snapshot.price).toLong() - asset.totalPurchaseValue)
            }
            val current = asset.profitOrLoss
            if (lastOrNull() != current) add(current)
        }
    }
    val positive = asset.profitOrLoss >= 0
    val lineColor = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val textColor = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FintrackBodyMediumText(stringResource(Res.string.label_asset_profit_loss))
            FintrackBodyMediumText(
                text = asset.profitOrLoss.toSignedPersianPrice(),
                color = textColor
            )
        }
        Spacer(Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val min = minOf(0L, points.minOrNull() ?: 0L).toFloat()
            val max = maxOf(0L, points.maxOrNull() ?: 0L).toFloat()
            val range = (max - min).takeIf { it > 0f } ?: 1f
            fun y(value: Long) = size.height - ((value - min) / range) * size.height
            val baselineY = y(0)
            drawLine(
                color = Color.Gray.copy(alpha = 0.35f),
                start = Offset(0f, baselineY),
                end = Offset(size.width, baselineY),
                strokeWidth = 1.dp.toPx()
            )
            if (points.size > 1) {
                val lastIndex = points.lastIndex.toFloat()
                points.zipWithNext().forEachIndexed { index, (from, to) ->
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width * index / lastIndex, y(from)),
                        end = Offset(size.width * (index + 1) / lastIndex, y(to)),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(size.width, y(points.last()))
            )
        }
    }
}
