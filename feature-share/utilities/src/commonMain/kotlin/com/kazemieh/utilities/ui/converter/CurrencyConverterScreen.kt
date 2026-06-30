package com.kazemieh.utilities.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_select_currency
import fintrack.core.designsystem.generated.resources.sub_currency_converter
import fintrack.core.designsystem.generated.resources.title_currency_converter
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
            modifier = Modifier.fillMaxSize()
        ) {
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
                        FintrackLabelMediumText(
                            text = state.fromRate?.name ?: "",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
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
                        FintrackLabelMediumText(
                            text = state.toRate?.name ?: "",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        FintrackHeadlineSmallText(
                            text = state.result.toLong().toSignedPersianPrice(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
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
                            FintrackLabelMediumText(text = rate.code.uppercase())
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
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
