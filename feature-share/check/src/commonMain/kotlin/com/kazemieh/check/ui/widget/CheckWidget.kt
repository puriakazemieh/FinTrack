package com.kazemieh.check.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.check.ui.list.CheckListViewModel
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckWidget(
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pendingChecks = state.checks.filter { it.status == CheckStatus.PENDING }

    if (pendingChecks.isNotEmpty()) {
        WidgetCard(
            title = "چک‌های در جریان",
            onMore = onMoreClick,
            modifier = modifier
        ) {
            Column {
                pendingChecks.take(2).forEach { check ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        FintrackTitleSmallText(
                            text = check.personName ?: "نامشخص",
                            fontWeight = FontWeight.SemiBold
                        )
                        FintrackBodyMediumText(
                            text = "${check.amount.toPersianPrice()} تومان - سررسید: ${check.dueDate}" // TODO: Proper Date
                        )
                    }
                }
            }
        }
    }
}
