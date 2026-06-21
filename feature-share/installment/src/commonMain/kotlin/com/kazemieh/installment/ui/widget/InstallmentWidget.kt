package com.kazemieh.installment.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.installment.ui.InstallmentIntent
import com.kazemieh.installment.ui.InstallmentViewModel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.installment_widget_title
import fintrack.core.designsystem.generated.resources.remaining_installments
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InstallmentWidget(
    viewModel: InstallmentViewModel = koinViewModel(),
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(InstallmentIntent.Init)
    }

    if (state.upcoming.isEmpty() && state.overdue.isEmpty()) return

    WidgetCard(
        title = stringResource(Res.string.installment_widget_title),
        onMore = onMore,
        modifier = modifier
    ) {
        val displayItems = (state.overdue + state.upcoming).take(3)
        displayItems.forEach { item ->
            InstallmentRowMinimal(item)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InstallmentRowMinimal(item: InstallmentWithRelations) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            FintrackLabelMediumText(
                text = item.installment.title
            )
            FintrackLabelSmallText(
                text = stringResource(
                    Res.string.remaining_installments,
                    (item.installment.totalInstallments - item.installment.paidInstallments)
                )
            )
        }

        FintrackLabelMediumText(
            text = item.installment.installmentAmount.toInt().toSignedPersianPrice()
        )
    }
}
