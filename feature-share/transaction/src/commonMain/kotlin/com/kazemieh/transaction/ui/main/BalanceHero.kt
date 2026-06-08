package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackDisplayLargeText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.financialsource.ui.list.SourceHorizontalList
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.balance_total
import fintrack.core.designsystem.generated.resources.currency_toman
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BalanceHero(
    viewModel: TransactionViewModel = koinViewModel(),
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onAddSourceClick: () -> Unit,
    onSourceClick: (Source) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val color = when {
        state.balance < 0.toString() -> GlassRed
        state.balance > 0.toString() -> GlassGreen
        else -> GlassGreen
    }
    LaunchedEffect(Unit) {
        viewModel.onIntent(TransactionIntent.Init)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        tone = GlassTone.Strong,
        padding = 16.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FintrackTitleLargeText(
                        text = stringResource(Res.string.balance_total),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(space.medium))
                    FintrackDisplayLargeText(
                        text = if (isBalanceVisible) state.balance else "****",
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(space.small))
                    FintrackTitleLargeText(
                        text = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(space.large))

            SourceHorizontalList(
                onAddSourceClick = onAddSourceClick,
                onSourceClick = onSourceClick
            )
        }
    }
}
