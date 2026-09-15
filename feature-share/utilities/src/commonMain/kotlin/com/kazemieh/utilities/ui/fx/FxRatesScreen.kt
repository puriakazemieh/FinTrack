package com.kazemieh.utilities.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.MarketRateHistory
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_refresh
import fintrack.core.designsystem.generated.resources.btn_add_rate_as_asset
import fintrack.core.designsystem.generated.resources.currency_toman_full
import fintrack.core.designsystem.generated.resources.label_current_price
import fintrack.core.designsystem.generated.resources.label_last_update
import fintrack.core.designsystem.generated.resources.label_retry
import fintrack.core.designsystem.generated.resources.msg_no_rate_results
import fintrack.core.designsystem.generated.resources.msg_rate_history_unavailable
import fintrack.core.designsystem.generated.resources.msg_rates_stale
import fintrack.core.designsystem.generated.resources.msg_rates_unavailable
import fintrack.core.designsystem.generated.resources.sub_fx_rates
import fintrack.core.designsystem.generated.resources.tab_crypto_currency
import fintrack.core.designsystem.generated.resources.tab_gold_and_silver
import fintrack.core.designsystem.generated.resources.tab_physical_currency
import fintrack.core.designsystem.generated.resources.title_fx_rates
import fintrack.core.designsystem.generated.resources.title_rate_history
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FxRatesScreen(
    viewModel: FxRatesViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onAddAssetClick: (AssetRate) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    var selectedTab by remember { mutableStateOf(AssetType.STOCK) }

    FintrackScreen(
        title = stringResource(Res.string.title_fx_rates),
        sub = stringResource(Res.string.sub_fx_rates),
        onBack = onBackClick,
        trailingContent = {
            IconButton(onClick = { viewModel.onIntent(FxRatesIntent.RefreshRates) }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.action_refresh)
                )
            }
        }
    ) {
        when {
            state.isLoading && state.rates.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.rates.isEmpty() && !state.isLoading -> {
                RatesUnavailable(onRetry = { viewModel.onIntent(FxRatesIntent.RefreshRates) })
            }

            else -> {
                Column(Modifier.fillMaxSize()) {
                    // Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(space.medium)
                            .background(glassColors.glass, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            AssetType.STOCK to stringResource(Res.string.tab_crypto_currency),
                            AssetType.FX to stringResource(Res.string.tab_physical_currency),
                            AssetType.GOLD to stringResource(Res.string.tab_gold_and_silver)
                        )
                        tabs.forEach { (type, label) ->
                            val isActive = selectedTab == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .then(
                                        if (isActive) Modifier.background(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.14f
                                            )
                                        ) else Modifier
                                    )
                                    .clickable { selectedTab = type }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                FintrackBodyMediumText(
                                    text = label,
                                    color = if (isActive) MaterialTheme.colorScheme.primary else glassColors.text3,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    val filteredRates = state.rates.filter { it.type == selectedTab }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = space.medium,
                            vertical = space.small
                        ),
                        verticalArrangement = Arrangement.spacedBy(space.small)
                    ) {
                        state.lastUpdate?.let { updatedAt ->
                            item {
                                Column(Modifier.fillMaxWidth().padding(bottom = space.small)) {
                                    FintrackLabelMediumText(
                                        text = stringResource(
                                            Res.string.label_last_update,
                                            DateUtils.formatTimestamp(updatedAt.toEpochMilliseconds())
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (state.error != null) {
                                        FintrackLabelMediumText(
                                            text = stringResource(Res.string.msg_rates_stale),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredRates.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FintrackBodyMediumText(
                                        stringResource(Res.string.msg_no_rate_results),
                                        color = glassColors.text3
                                    )
                                }
                            }
                        } else {
                            items(filteredRates) { rate ->
                                RateItem(rate) { viewModel.onIntent(FxRatesIntent.SelectRate(rate)) }
                            }
                        }
                    }
                }
            }
        }
    }

    state.selectedRate?.let { selectedRate ->
        RateDetailBottomSheet(
            rate = selectedRate,
            history = state.selectedRateHistory,
            onDismiss = { viewModel.onIntent(FxRatesIntent.DismissRateDetails) },
            onAddAsset = {
                onAddAssetClick(selectedRate)
                viewModel.onIntent(FxRatesIntent.DismissRateDetails)
            }
        )
    }
}

@Composable
private fun RatesUnavailable(onRetry: () -> Unit) {
    val space = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(space.large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FintrackBodyMediumText(
            text = stringResource(Res.string.msg_rates_unavailable),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(space.medium))
        FintrackButton(
            text = stringResource(Res.string.label_retry),
            onClick = onRetry
        )
    }
}

@Composable
private fun RateItem(rate: AssetRate, onClick: () -> Unit) {
    val space = LocalSpacing.current
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                FintrackTitleMediumText(
                    text = rate.name,
                    fontWeight = FontWeight.Bold
                )
                FintrackLabelMediumText(
                    text = rate.code.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                FintrackTitleMediumText(
                    text = rate.price.toSignedPersianPrice(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                FintrackLabelMediumText(
                    text = stringResource(Res.string.currency_toman_full),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RateDetailBottomSheet(
    rate: AssetRate,
    history: List<MarketRateHistory>,
    onDismiss: () -> Unit,
    onAddAsset: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = glassColors.bg0,
        dragHandle = null
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FintrackTitleLargeText(
                stringResource(Res.string.title_rate_history, rate.name),
                fontWeight = FontWeight.Bold
            )
            FintrackLabelMediumText(rate.code.uppercase(), color = glassColors.text3)

            Spacer(Modifier.height(24.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                tone = com.kazemieh.designsystem.component.glass.GlassTone.Strong
            ) {
                if (history.size < 2) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        FintrackBodyMediumText(
                            stringResource(Res.string.msg_rate_history_unavailable),
                            color = glassColors.text3
                        )
                    }
                } else {
                    RateHistoryChart(history, modifier = Modifier.fillMaxSize().padding(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackBodyMediumText(stringResource(Res.string.label_current_price))
                FintrackTitleMediumText(
                    text = rate.price.toSignedPersianPrice() + " " + stringResource(Res.string.currency_toman_full),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            FintrackButton(
                text = stringResource(Res.string.btn_add_rate_as_asset),
                onClick = onAddAsset,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RateHistoryChart(history: List<MarketRateHistory>, modifier: Modifier = Modifier) {
    val glassColors = LocalGlassColors.current
    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val values = history.map { it.price.toDouble() }
        val min = values.minOrNull() ?: return@Canvas
        val max = values.maxOrNull() ?: return@Canvas
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * step
            val y = size.height - ((value - min) / range).toFloat() * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawLine(
            color = glassColors.glassEdge,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
