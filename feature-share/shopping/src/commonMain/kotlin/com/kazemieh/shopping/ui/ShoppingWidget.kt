package com.kazemieh.shopping.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.shopping_list_empty
import fintrack.core.designsystem.generated.resources.total_sum
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingWidget(
    modifier: Modifier = Modifier,
    onMore: () -> Unit,
    viewModel: ShoppingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val uncheckedItems = state.items.filter { !it.isChecked }

    WidgetCard(
        title = stringResource(Res.string.shopping_list),
        onMore = onMore,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            uncheckedItems.take(3).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FintrackBodyMediumText(text = item.name)
                    if (item.estimatedPrice > 0) {
                        MoneyText(amount = item.estimatedPrice.toLong())
                    }
                }
            }

            if (uncheckedItems.isEmpty()) {
                FintrackBodySmallText(
                    text = stringResource(Res.string.shopping_list_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val total = uncheckedItems.sumOf { it.estimatedPrice }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.total_sum),
                    fontWeight = FontWeight.Bold
                )
                MoneyText(
                    amount = total.toLong(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
