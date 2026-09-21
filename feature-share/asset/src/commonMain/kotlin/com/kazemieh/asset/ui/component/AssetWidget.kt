package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.aggregateByMarket
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassPurple
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AssetWidget(
    viewModel: AssetViewModel = koinViewModel(),
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val analytics = koinInject<com.kazemieh.common.analytics.AnalyticsService>()
    LaunchedEffect(Unit) {
        analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetDashboardPerformanceViewed)
    }
    val positions = state.assets.aggregateByMarket()
    val totalPurchaseValue = positions.sumOf { it.totalPurchaseValue }
    val profitOrLoss = state.totalValue - totalPurchaseValue
    val profitOrLossPercentage = if (totalPurchaseValue == 0L) 0.0 else {
        profitOrLoss.toDouble() / totalPurchaseValue * 100
    }
    val performanceColor = if (profitOrLoss >= 0L) GlassGreen else com.kazemieh.designsystem.GlassRed

    WidgetCard(
        title = stringResource(Res.string.title_assets_management),
        onMore = {
            analytics.track(com.kazemieh.common.analytics.ProductEvent.AssetDashboardMoreClicked)
            onMore()
        },
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    FintrackLabelSmallText(text = stringResource(Res.string.label_total_value))
                    MoneyText(amount = state.totalValue, size = 18)
                }
                Column(horizontalAlignment = Alignment.End) {
                    FintrackLabelSmallText(text = stringResource(Res.string.label_asset_return))
                    FintrackTitleSmallText(
                        text = stringResource(
                            Res.string.label_percentage,
                            String.format("%.1f", profitOrLossPercentage)
                        ),
                        color = performanceColor
                    )
                }
            }

            Column {
                FintrackLabelSmallText(text = stringResource(Res.string.label_asset_profit_loss))
                MoneyText(amount = profitOrLoss, size = 15, color = performanceColor)
            }

            PortfolioProfitLossChart(assets = positions, color = performanceColor)

            // Mini Composition Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(LocalGlassColors.current.text.copy(alpha = 0.05f))
            ) {
                val gold = state.composition[AssetType.GOLD] ?: 0.0
                val fx = state.composition[AssetType.FX] ?: 0.0
                val stock = state.composition[AssetType.STOCK] ?: 0.0
                val crypto = state.composition[AssetType.CRYPTO] ?: 0.0
                val custom = state.composition[AssetType.CUSTOM] ?: 0.0

                if (gold > 0) Box(Modifier.weight(gold.toFloat()).fillMaxHeight().background(GlassAmber))
                if (fx > 0) Box(Modifier.weight(fx.toFloat()).fillMaxHeight().background(GlassGreen))
                if (crypto > 0) Box(Modifier.weight(crypto.toFloat()).fillMaxHeight().background(com.kazemieh.designsystem.GlassGold))
                if (stock > 0) Box(Modifier.weight(stock.toFloat()).fillMaxHeight().background(GlassBlue))
                if (custom > 0) Box(Modifier.weight(custom.toFloat()).fillMaxHeight().background(GlassPurple))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.composition.entries.toList().take(3).forEach { entry ->
                    val type = entry.key
                    val percentage = entry.value
                    if (percentage > 0) {
                        val color = when (type) {
                            AssetType.GOLD -> GlassAmber
                            AssetType.FX -> GlassGreen
                            AssetType.STOCK -> GlassBlue
                            AssetType.CUSTOM -> GlassPurple
                            AssetType.CRYPTO -> com.kazemieh.designsystem.GlassGold
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(color)
                            )
                            Spacer(Modifier.width(4.dp))
                            FintrackLabelSmallText(
                                text = "${type.dashboardLabel()} ${"%.0f".format(percentage * 100)}%"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetType.dashboardLabel(): String = stringResource(
    when (this) {
        AssetType.GOLD -> Res.string.asset_type_gold
        AssetType.FX -> Res.string.asset_type_fx
        AssetType.STOCK -> Res.string.asset_type_stock
        AssetType.CRYPTO -> Res.string.asset_type_crypto
        AssetType.CUSTOM -> Res.string.asset_type_custom
    }
)

/** A zero-centred chart of each position's actual gain/loss, including fractional holdings. */
@Composable
private fun PortfolioProfitLossChart(assets: List<Asset>, color: Color) {
    val values = assets.map { it.profitOrLoss }.take(8)
    if (values.isEmpty()) return
    val baselineColor = LocalGlassColors.current.text.copy(alpha = 0.2f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val maxAbs = values.maxOf { kotlin.math.abs(it.toDouble()) }.toFloat().takeIf { it > 0f } ?: 1f
        val baseline = size.height / 2f
        drawLine(
            color = baselineColor,
            start = Offset(0f, baseline),
            end = Offset(size.width, baseline),
            strokeWidth = 1.dp.toPx()
        )
        val slotWidth = size.width / values.size
        values.forEachIndexed { index, value ->
            val magnitude = (kotlin.math.abs(value.toDouble()).toFloat() / maxAbs) * (size.height / 2f - 2.dp.toPx())
            val top = if (value >= 0) baseline - magnitude else baseline
            drawRect(
                color = if (value >= 0) color else com.kazemieh.designsystem.GlassRed,
                topLeft = Offset(index * slotWidth + slotWidth * 0.2f, top),
                size = androidx.compose.ui.geometry.Size(slotWidth * 0.6f, magnitude)
            )
        }
    }
}
