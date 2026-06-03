package com.kazemieh.dashboard.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.formatNumberLong
import com.kazemieh.common.formatted
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toFa
import com.kazemieh.common.toFormattedFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassIcon
import com.kazemieh.designsystem.component.glass.Row
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.transaction.ui.main.TransactionIntent
import com.kazemieh.transaction.ui.main.TransactionViewModel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_source_date_summary
import fintrack.core.designsystem.generated.resources.recent_transactions
import fintrack.core.designsystem.generated.resources.unit_toman_short
import fintrack.core.designsystem.generated.resources.transfer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecentTransactionsWidget(
    viewModel: TransactionViewModel = koinViewModel(),
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(TransactionIntent.Init)
    }

    WidgetCard(
        title = stringResource(Res.string.recent_transactions),
        onMore = onMoreClick,
        modifier = modifier
    ) {
        state.items.take(5).forEachIndexed { index, item ->
            val isIncome = item.transaction.type == TransactionType.INCOME
            val isTransfer = item.transaction.type == TransactionType.TRANSFER
            
            val color = when {
                isIncome -> GlassGreen
                isTransfer -> GlassBlue
                else -> GlassRed
            }
            val bgColor = when {
                isIncome -> GlassGreenSoft
                isTransfer -> GlassBlueSoft
                else -> GlassRedSoft
            }

            val iconId = if (isTransfer) 19 else item.category.iconId
            val icon = FinTrackIcons.findIcon(iconId)
            val label = if (isTransfer) stringResource(Res.string.transfer) else item.category.name

            Row(
                label = label,
                sub = stringResource(Res.string.label_source_date_summary, item.source.name, item.transaction.date),
                icon = GlassIcon(
                    painter = painterResource(icon.resource),
                    bg = bgColor,
                    color = color
                ),
                value = {
                    val amountText = if (isTransfer) {
                        item.transaction.amount.toLong().formatNumberLong()
                    } else {
                        item.transaction.amount.toLong().formatNumberLong()
                    }
                    FintrackTitleSmallText(
                        text = "$amountText " + stringResource(Res.string.unit_toman_short),
                        fontWeight = FontWeight.W600,
                        color = color
                    )
                },
                divider = index < state.items.take(5).size - 1
            )
        }
    }
}
