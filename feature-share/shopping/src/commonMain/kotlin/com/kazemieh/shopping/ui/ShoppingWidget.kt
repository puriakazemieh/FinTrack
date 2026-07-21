package com.kazemieh.shopping.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.shopping_list
import fintrack.core.designsystem.generated.resources.shopping_list_empty
import fintrack.core.designsystem.generated.resources.total_sum
import fintrack.core.designsystem.generated.resources.view_all
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
    var showMoreItems by remember { mutableStateOf(false) }

    WidgetCard(
        title = stringResource(Res.string.shopping_list),
        count = uncheckedItems.size.takeIf { it > 0 },
        onMore = onMore,
        modifier = modifier
    ) {
        if (uncheckedItems.isEmpty()) {
            WidgetEmptyState(
                icon = Icons.Default.ShoppingCart,
                text = stringResource(Res.string.shopping_list_empty)
            )
        } else {
            Column {
                val itemsToShow = if (showMoreItems) 10 else 5
                uncheckedItems.take(itemsToShow).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckCircleSmall(onClick = { viewModel.onIntent(ShoppingIntent.OnToggleItem(item)) })
                        FintrackBodyMediumText(
                            text = item.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        if (item.estimatedPrice > 0) {
                            FintrackLabelSmallText(
                                text = item.estimatedPrice.toLong().toPersianPrice(),
                                color = LocalGlassColors.current.text2
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uncheckedItems.size > 5 && !showMoreItems) {
                        TextButton(
                            onClick = { showMoreItems = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.view_all),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    val total = uncheckedItems.sumOf { it.estimatedPrice }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.total_sum) + ": ",
                            fontSize = 10.sp,
                            color = LocalGlassColors.current.text3
                        )
                        FintrackLabelSmallText(
                            text = total.toLong().toPersianPrice(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                        FintrackLabelSmallText(
                            text = " " + stringResource(Res.string.currency_toman),
                            fontSize = 9.sp,
                            color = LocalGlassColors.current.text3
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckCircleSmall(onClick: () -> Unit) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .border(1.dp, glassColors.glassEdge, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
    }
}
