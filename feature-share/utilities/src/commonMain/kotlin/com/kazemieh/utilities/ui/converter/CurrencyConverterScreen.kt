package com.kazemieh.utilities.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CurrencyConverterScreen(
    viewModel: CurrencyConverterViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    FintrackScreen(
        title = stringResource(Res.string.title_currency_converter),
        sub = stringResource(Res.string.sub_currency_converter),
        onBack = onBackClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Live market rates come from an online source; when only the built-in Toman
            // fallback is present, conversions aren't meaningful, so we say so and offer retry
            // instead of showing a misleading 1:1 rate.
            if (!state.isLoading && state.availableRates.size <= 1) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(space.medium),
                    onClick = { viewModel.onIntent(CurrencyConverterIntent.RefreshRates) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(space.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.msg_rates_unavailable),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(space.small))
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.label_retry),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Display Area
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(space.medium),
                tone = GlassTone.Strong
            ) {
                Column(
                    modifier = Modifier.padding(space.medium),
                    horizontalAlignment = Alignment.End
                ) {
                    // From
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFromPicker = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FintrackLabelMediumText(
                                text = (state.fromRate?.code?.let { getFlag(it) } ?: "") + " ",
                                fontSize = 20.sp
                            )
                            FintrackLabelMediumText(
                                text = state.fromRate?.name ?: "",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FintrackHeadlineSmallText(
                            text = state.amount.toPersianDigits(),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = space.small),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { viewModel.onIntent(CurrencyConverterIntent.SwapRates) }) {
                            Icon(
                                Icons.Default.SwapVert,
                                contentDescription = "Swap",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = glassColors.glassEdge,
                            thickness = 0.5.dp
                        )
                    }

                    // To
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showToPicker = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FintrackLabelMediumText(
                                text = (state.toRate?.code?.let { getFlag(it) } ?: "") + " ",
                                fontSize = 20.sp
                            )
                            FintrackLabelMediumText(
                                text = state.toRate?.name ?: "",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FintrackHeadlineSmallText(
                            text = state.result.toLong().toSignedPersianPrice(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Rate Info
            if (state.fromRate != null && state.toRate != null) {
                val rate = state.fromRate!!.price.toDouble() / state.toRate!!.price.toDouble()
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = space.medium),
                    padding = space.small
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(space.small),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            FintrackBodySmallText(text = "نرخ تبدیل", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FintrackTitleMediumText(text = "۱ ${state.fromRate!!.code.uppercase()} = ${rate.toString().toPersianDigits()} ${state.toRate!!.name}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Amounts
            FintrackLabelMediumText(
                text = "مبلغ‌های پرکاربرد",
                modifier = Modifier.padding(space.medium),
                fontWeight = FontWeight.Bold
            )
            val quickAmounts = listOf("10", "50", "100", "500", "1000", "5000")
            LazyRow(
                contentPadding = PaddingValues(horizontal = space.medium),
                horizontalArrangement = Arrangement.spacedBy(space.small)
            ) {
                items(quickAmounts) { amount ->
                    GlassCard(
                        onClick = { viewModel.onIntent(CurrencyConverterIntent.SelectQuickAmount(amount)) }
                    ) {
                        FintrackLabelMediumText(
                            text = amount.toPersianDigits(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Favorite Pairs
            FintrackLabelMediumText(
                text = "جفت‌های مورد علاقه",
                modifier = Modifier.padding(space.medium),
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.padding(horizontal = space.medium),
                verticalArrangement = Arrangement.spacedBy(space.small)
            ) {
                state.favoritePairs.forEach { pair ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.onIntent(CurrencyConverterIntent.SelectFavoritePair(pair)) }
                    ) {
                        Row(
                            modifier = Modifier.padding(space.medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(space.small)
                        ) {
                            FintrackBodyMediumText(text = "${pair.fromCode.uppercase()} → ${pair.toCode.uppercase()}", fontWeight = FontWeight.Bold)
                            FintrackBodySmallText(text = "${pair.fromName} به ${pair.toName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(space.medium))

            // Keypad
            val keys = listOf(
                "7", "8", "9", "⌫",
                "4", "5", "6", "C",
                "1", "2", "3", "0",
                "000", "."
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Fixed height for keypad in scrollable view
                    .padding(space.medium),
                horizontalArrangement = Arrangement.spacedBy(space.small),
                verticalArrangement = Arrangement.spacedBy(space.small)
            ) {
                items(keys) { key ->
                    KeyButton(
                        key = key,
                        onClick = {
                            when (key) {
                                "C" -> viewModel.onIntent(CurrencyConverterIntent.Clear)
                                "⌫" -> viewModel.onIntent(CurrencyConverterIntent.Delete)
                                else -> viewModel.onIntent(CurrencyConverterIntent.InputChar(key))
                            }
                        }
                    )
                }
            }
        }
    }

    if (showFromPicker) {
        CurrencyPickerSheet(
            rates = state.availableRates,
            onSelect = {
                viewModel.onIntent(CurrencyConverterIntent.SelectFromRate(it))
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        CurrencyPickerSheet(
            rates = state.availableRates,
            onSelect = {
                viewModel.onIntent(CurrencyConverterIntent.SelectToRate(it))
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    rates: List<AssetRate>,
    onSelect: (AssetRate) -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = glassColors.bg0,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FintrackTitleMediumText(
                text = stringResource(Res.string.label_select_currency),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rates) { rate ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSelect(rate) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FintrackTitleMediumText(text = rate.name)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FintrackLabelMediumText(text = getFlag(rate.code) + " ")
                                FintrackLabelMediumText(text = rate.code.uppercase())
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun getFlag(code: String): String {
    return when (code.lowercase()) {
        "usd" -> "🇺🇸"
        "eur" -> "🇪🇺"
        "gbp" -> "🇬🇧"
        "irr" -> "🇮🇷"
        "btc" -> "₿"
        "eth" -> "Ξ"
        "gold_18k" -> "🟡"
        else -> "💰"
    }
}

@Composable
private fun KeyButton(
    key: String,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val isAction = key in listOf("C", "⌫")

    val containerColor = if (isAction) glassColors.glass else Color.Transparent
    val textColor = if (key == "C" || key == "⌫") GlassRed else glassColors.text

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            FintrackLabelMediumText(
                text = key.toPersianDigits(),
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
