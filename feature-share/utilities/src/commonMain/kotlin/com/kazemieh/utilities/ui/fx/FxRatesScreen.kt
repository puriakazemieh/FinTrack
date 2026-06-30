package com.kazemieh.utilities.ui.fx

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FxRatesScreen(
    viewModel: FxRatesViewModel = koinViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    FintrackScreen(
        title = stringResource(Res.string.title_fx_rates),
        sub = stringResource(Res.string.sub_fx_rates),
        onBack = onBackClick,
        floatingActionButton = {
            IconButton(onClick = { viewModel.onIntent(FxRatesIntent.RefreshRates) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(space.medium),
            verticalArrangement = Arrangement.spacedBy(space.small)
        ) {
            items(state.rates) { rate ->
                RateItem(rate)
            }
        }
    }
}

@Composable
private fun RateItem(rate: AssetRate) {
    val space = LocalSpacing.current
    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                    text = stringResource(Res.string.today), // Should show actual update time
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
