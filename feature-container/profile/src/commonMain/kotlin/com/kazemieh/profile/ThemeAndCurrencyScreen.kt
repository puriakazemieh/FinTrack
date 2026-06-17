package com.kazemieh.profile

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.ThemeMode
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_rial_full
import fintrack.core.designsystem.generated.resources.currency_toman_full
import fintrack.core.designsystem.generated.resources.label_currency
import fintrack.core.designsystem.generated.resources.label_theme
import fintrack.core.designsystem.generated.resources.label_theme_dark_time
import fintrack.core.designsystem.generated.resources.label_theme_mode
import fintrack.core.designsystem.generated.resources.notif_quiet_end_label
import fintrack.core.designsystem.generated.resources.notif_quiet_start_label
import fintrack.core.designsystem.generated.resources.theme_glass_dark
import fintrack.core.designsystem.generated.resources.theme_glass_light
import fintrack.core.designsystem.generated.resources.theme_mode_custom_time
import fintrack.core.designsystem.generated.resources.theme_mode_manual
import fintrack.core.designsystem.generated.resources.theme_mode_sunrise_sunset
import fintrack.core.designsystem.generated.resources.theme_mode_system
import fintrack.core.designsystem.generated.resources.theme_plain_dark
import fintrack.core.designsystem.generated.resources.theme_plain_light
import fintrack.core.designsystem.generated.resources.title_theme_currency
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ThemeAndCurrencyScreen(
    onBack: () -> Unit,
    viewModel: ThemeAndCurrencyViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    val showTimePicker = remember { mutableStateOf(false) }
    var pickingStartTime by remember { mutableStateOf(true) }

    FintrackTimePickerBottomSheet(
        openSheet = showTimePicker,
        initialTime = if (pickingStartTime) state.startTime else state.endTime,
        onConfirm = { time ->
            if (pickingStartTime) {
                viewModel.onIntent(ThemeAndCurrencyIntent.SetStartTime(time))
            } else {
                viewModel.onIntent(ThemeAndCurrencyIntent.SetEndTime(time))
            }
        }
    )

    FintrackScreen(
        title = stringResource(Res.string.title_theme_currency),
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                WidgetCard(
                    title = stringResource(Res.string.label_theme)
                ) {
                    ThemeGrid(
                        selectedTheme = state.selectedTheme,
                        onThemeSelected = { viewModel.onIntent(ThemeAndCurrencyIntent.SelectTheme(it)) }
                    )
                }
            }

            item {
                WidgetCard(
                    title = stringResource(Res.string.label_theme_mode)
                ) {
                    ThemeModeSection(
                        selectedMode = state.selectedMode,
                        onModeSelected = { viewModel.onIntent(ThemeAndCurrencyIntent.SelectMode(it)) }
                    )

                    if (state.selectedMode == ThemeMode.CUSTOM_TIME) {
                        Spacer(modifier = Modifier.height(space.medium))
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.label_theme_dark_time),
                            modifier = Modifier.padding(horizontal = space.medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                        TimePickerItem(
                            title = stringResource(Res.string.notif_quiet_start_label),
                            time = state.startTime,
                            onClick = {
                                pickingStartTime = true
                                showTimePicker.value = true
                            }
                        )
                        TimePickerItem(
                            title = stringResource(Res.string.notif_quiet_end_label),
                            time = state.endTime,
                            onClick = {
                                pickingStartTime = false
                                showTimePicker.value = true
                            }
                        )
                    }
                }
            }

            item {
                WidgetCard(
                    title = stringResource(Res.string.label_currency)
                ) {
                    CurrencySection(
                        selectedCurrency = state.selectedCurrency,
                        onCurrencySelected = {
                            viewModel.onIntent(
                                ThemeAndCurrencyIntent.SelectCurrency(
                                    it
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeModeSection(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val modes = listOf(
        ThemeMode.MANUAL to Res.string.theme_mode_manual,
        ThemeMode.SYSTEM to Res.string.theme_mode_system,
        ThemeMode.SUNRISE_SUNSET to Res.string.theme_mode_sunrise_sunset,
        ThemeMode.CUSTOM_TIME to Res.string.theme_mode_custom_time
    )

    Column {
        modes.forEach { (mode, labelRes) ->
            ThemeModeItem(
                label = stringResource(labelRes),
                isSelected = mode == selectedMode,
                onClick = { onModeSelected(mode) }
            )
        }
    }
}

@Composable
fun ThemeModeItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = space.medium, vertical = space.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun TimePickerItem(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = space.medium, vertical = space.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(
            text = title,
            modifier = Modifier.weight(1f)
        )
        FintrackBodyLargeText(
            text = time.toPersianDigits(),
            color = LocalGlassColors.current.text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ThemeGrid(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    val themes = listOf(
        AppTheme.GLASS_DARK to Res.string.theme_glass_dark,
        AppTheme.GLASS_LIGHT to Res.string.theme_glass_light,
        AppTheme.PLAIN_DARK to Res.string.theme_plain_dark,
        AppTheme.PLAIN_LIGHT to Res.string.theme_plain_light
    )

    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        themes.chunked(2).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowThemes.forEach { (theme, labelRes) ->
                    ThemeItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(labelRes),
                        isSelected = theme == selectedTheme,
                        onClick = { onThemeSelected(theme) },
                        theme = theme
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeItem(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val space = LocalSpacing.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(space.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Theme preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(getThemePreviewColor(theme))
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryColor.copy(alpha = 0.2f))
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(space.small)
                        .size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(space.small))
        FintrackLabelMediumText(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CurrencySection(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit
) {
    val currencies = listOf(
        Currency.TOMAN to Res.string.currency_toman_full,
        Currency.RIAL to Res.string.currency_rial_full
    )

    Column {
        currencies.forEach { (currency, labelRes) ->
            CurrencyItem(
                label = stringResource(labelRes),
                isSelected = currency == selectedCurrency,
                onClick = { onCurrencySelected(currency) }
            )
        }
    }
}

@Composable
fun CurrencyItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(space.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FintrackBodyLargeText(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

fun getThemePreviewColor(theme: AppTheme): Color {
    return when (theme) {
        AppTheme.GLASS_DARK -> Color(0xFF06100E)
        AppTheme.GLASS_LIGHT -> Color(0xFFF0F4F3)
        AppTheme.PLAIN_DARK -> Color(0xFF1A1C18)
        AppTheme.PLAIN_LIGHT -> Color(0xFFFCFDF6)
    }
}
