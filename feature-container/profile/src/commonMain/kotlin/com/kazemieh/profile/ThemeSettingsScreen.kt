package com.kazemieh.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.AccentPalette
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.ThemeMode
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.accent_amber
import fintrack.core.designsystem.generated.resources.accent_blue
import fintrack.core.designsystem.generated.resources.accent_emerald
import fintrack.core.designsystem.generated.resources.accent_gold
import fintrack.core.designsystem.generated.resources.accent_purple
import fintrack.core.designsystem.generated.resources.accent_rose
import fintrack.core.designsystem.generated.resources.label_accent_color
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = koinViewModel()
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
                viewModel.onIntent(ThemeSettingsIntent.SetStartTime(time))
            } else {
                viewModel.onIntent(ThemeSettingsIntent.SetEndTime(time))
            }
        }
    )

    FintrackScreen(
        title = stringResource(Res.string.label_theme),
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.medium),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(space.medium))
                ThemeGrid(
                    selectedTheme = state.selectedTheme,
                    onThemeSelected = { viewModel.onIntent(ThemeSettingsIntent.SelectTheme(it)) }
                )
            }

            item {
                WidgetCard(
                    title = stringResource(Res.string.label_accent_color)
                ) {
                    AccentPaletteSection(
                        selectedAccent = state.selectedAccent,
                        onAccentSelected = { viewModel.onIntent(ThemeSettingsIntent.SelectAccent(it)) }
                    )
                }
            }

            item {
                WidgetCard(
                    title = stringResource(Res.string.label_theme_mode)
                ) {
                    ThemeModeSection(
                        selectedMode = state.selectedMode,
                        onModeSelected = { viewModel.onIntent(ThemeSettingsIntent.SelectMode(it)) }
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
fun AccentPaletteSection(
    selectedAccent: AccentPalette,
    onAccentSelected: (AccentPalette) -> Unit
) {
    val space = LocalSpacing.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = space.small, vertical = space.small),
            horizontalArrangement = Arrangement.spacedBy(space.small)
        ) {
            AccentPalette.entries.forEach { palette ->
                AccentSwatch(
                    modifier = Modifier.weight(1f),
                    palette = palette,
                    label = stringResource(accentLabelRes(palette)),
                    isSelected = palette == selectedAccent,
                    onClick = { onAccentSelected(palette) }
                )
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    modifier: Modifier = Modifier,
    palette: AccentPalette,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(palette.accent)
                .border(
                    2.dp,
                    if (isSelected) LocalGlassColors.current.text else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = palette.onAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        FintrackLabelMediumText(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun accentLabelRes(palette: AccentPalette) = when (palette) {
    AccentPalette.EMERALD -> Res.string.accent_emerald
    AccentPalette.BLUE -> Res.string.accent_blue
    AccentPalette.PURPLE -> Res.string.accent_purple
    AccentPalette.AMBER -> Res.string.accent_amber
    AccentPalette.ROSE -> Res.string.accent_rose
    AccentPalette.GOLD -> Res.string.accent_gold
}

@Composable
fun ThemeModeItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
}

@Composable
fun TimePickerItem(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        themes.chunked(2).forEach { rowThemes ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
}

@Composable
fun ThemeItem(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    theme: AppTheme
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = theme.isDark

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent)
            .border(
                2.dp,
                if (isSelected) primaryColor else Color.Transparent,
                MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(getThemePreviewGradient(theme))
        ) {
            // Mock UI inside preview
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(
                            alpha = 0.04f
                        )
                    )
                    .border(
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f),
                        MaterialTheme.shapes.small
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isDark) primaryColor else MaterialTheme.colorScheme.primaryContainer)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 14.dp, end = 10.dp)
                    .width(60.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(
                            alpha = 0.15f
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FintrackLabelMediumText(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    modifier = Modifier.size(12.dp),
                    tint = primaryColor
                )
            }
        }
    }
}

fun getThemePreviewGradient(theme: AppTheme): androidx.compose.ui.graphics.Brush {
    return when (theme) {
        AppTheme.GLASS_DARK -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                Color(
                    0xFF0A1714
                ), Color(0xFF06100E)
            )
        )

        AppTheme.GLASS_LIGHT -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                Color(
                    0xFFFDFBF7
                ), Color(0xFFE7E2D9)
            )
        )

        AppTheme.PLAIN_DARK -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                Color(
                    0xFF1C1C1E
                ), Color(0xFF0C0C0E)
            )
        )

        AppTheme.PLAIN_LIGHT -> androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                Color(
                    0xFFF7F7F8
                ), Color(0xFFE8E8EB)
            )
        )
    }
}
