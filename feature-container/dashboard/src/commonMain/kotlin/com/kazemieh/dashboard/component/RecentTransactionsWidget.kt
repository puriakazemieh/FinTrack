package com.kazemieh.dashboard.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.glass.GlassIcon
import com.kazemieh.designsystem.component.glass.Row
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.transaction.ui.main.TransactionIntent
import com.kazemieh.transaction.ui.main.TransactionViewModel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.recent_transactions
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
            val isPositive = item.transaction.type == TransactionType.INCOME
            val color = if (isPositive) GlassGreen else GlassRed
            val bgColor = if (isPositive) GlassGreenSoft else GlassRedSoft

            val icon = FinTrackIcons.findIcon(item.category.iconId)

            Row(
                label = item.category.name,
                sub = "${item.source.name} · ${item.transaction.date}",
                icon = GlassIcon(
                    painter = painterResource(icon.resource),
                    bg = bgColor,
                    color = color
                ),
                value = {
                    Text(
                        text = "${item.transaction.amount.toLong().toFa()} ت",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.W600,
                        color = color
                    )
                },
                divider = index < state.items.take(5).size - 1
            )
        }
    }
}
