package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.GlassTone
import com.kazemieh.financialsource.ui.list.SourceHorizontalList
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BalanceHero(
    viewModel: TransactionViewModel = koinViewModel(),
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onAddSourceClick: () -> Unit,
    growthPercentage: String = "+2.5%",
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(TransactionIntent.Init)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        tone = GlassTone.Strong,
        padding = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackBodyMediumText(
                    text = stringResource(Res.string.balance_total),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FintrackDisplaySmallText(
                    text = if (isBalanceVisible) state.balance.toFa() else "****",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(space.mediumSmall))
                FintrackBodyMediumText(
                    text = stringResource(Res.string.currency_toman),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(Modifier.weight(1f))

                // Growth Pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    FintrackLabelSmallText(
                        text = growthPercentage.toFa(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(space.large))

            SourceHorizontalList(
                onAddSourceClick = onAddSourceClick
            )
        }
    }
}
