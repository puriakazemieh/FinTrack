package com.kazemieh.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeAndCurrencyScreen(
    onBack: () -> Unit,
    viewModel: ThemeAndCurrencyViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { FintrackTitleLargeText(stringResource(Res.string.title_theme_currency)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = space.large),
            verticalArrangement = Arrangement.spacedBy(space.large),
            contentPadding = PaddingValues(vertical = space.medium)
        ) {
            item {
                FintrackTitleMediumText(
                    text = stringResource(Res.string.label_theme),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(space.medium))
                ThemeGrid(
                    selectedTheme = state.selectedTheme,
                    onThemeSelected = { viewModel.onIntent(ThemeAndCurrencyIntent.SelectTheme(it)) }
                )
            }

            item {
                FintrackTitleMediumText(
                    text = stringResource(Res.string.label_currency),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(space.medium))
                CurrencySection(
                    selectedCurrency = state.selectedCurrency,
                    onCurrencySelected = { viewModel.onIntent(ThemeAndCurrencyIntent.SelectCurrency(it)) }
                )
            }
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    GlassCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium),
        padding = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(space.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Theme preview box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(getThemePreviewColor(theme))
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(space.small))
            FintrackLabelMediumText(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        padding = space.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackBodyLargeText(
                text = label,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
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
