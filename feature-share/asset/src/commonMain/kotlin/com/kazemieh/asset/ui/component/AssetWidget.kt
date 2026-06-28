package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.AssetType
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassPurple
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssetWidget(
    viewModel: AssetViewModel = koinViewModel(),
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    WidgetCard(
        title = stringResource(Res.string.title_assets_management),
        onMore = onMore,
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
            }

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
                val custom = state.composition[AssetType.CUSTOM] ?: 0.0

                if (gold > 0) Box(Modifier.weight(gold.toFloat()).fillMaxHeight().background(GlassAmber))
                if (fx > 0) Box(Modifier.weight(fx.toFloat()).fillMaxHeight().background(GlassGreen))
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
                                text = "${(percentage * 100).toInt()}%"
                            )
                        }
                    }
                }
            }
        }
    }
}
