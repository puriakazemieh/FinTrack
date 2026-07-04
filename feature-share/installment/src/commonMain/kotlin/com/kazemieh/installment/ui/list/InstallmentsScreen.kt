package com.kazemieh.installment.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.Tabs
import com.kazemieh.installment.ui.InstallmentIntent
import com.kazemieh.installment.ui.InstallmentViewModel
import com.kazemieh.installment.ui.add.AddInstallmentBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.installment_overdue
import fintrack.core.designsystem.generated.resources.installment_paid
import fintrack.core.designsystem.generated.resources.installment_upcoming
import fintrack.core.designsystem.generated.resources.navigation_installment
import fintrack.core.designsystem.generated.resources.notif_installment_title
import fintrack.core.designsystem.generated.resources.notif_installment_desc
import fintrack.core.designsystem.generated.resources.payment_for
import fintrack.core.designsystem.generated.resources.remaining_installments
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InstallmentsScreen(
    viewModel: InstallmentViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddInstallment by remember { mutableStateOf(false) }
    var selectedInstallmentId by remember { mutableStateOf<Long?>(null) }

    val tabs = listOf(
        stringResource(Res.string.installment_upcoming),
        stringResource(Res.string.installment_overdue),
        stringResource(Res.string.installment_paid)
    )

    LaunchedEffect(Unit) {
        viewModel.onIntent(InstallmentIntent.Init)
    }

    FintrackScreen(
        title = stringResource(Res.string.navigation_installment),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Tabs(
                tabs = tabs,
                active = selectedTabIndex,
                onChange = { selectedTabIndex = it },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                counts = listOf(
                    state.upcoming.size,
                    state.overdue.size,
                    state.completed.size
                )
            )

            val items = when (selectedTabIndex) {
                0 -> state.filteredUpcoming
                1 -> state.filteredOverdue
                else -> state.filteredCompleted
            }

            EntityList(
                title = tabs[selectedTabIndex],
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(InstallmentIntent.UpdateSearchQuery(it)) },
                onAddClick = { selectedInstallmentId = null; showAddInstallment = true },
                items = items.map { item ->
                    val progress =
                        (item.installment.paidInstallments.toFloat() / item.installment.totalInstallments).coerceIn(
                            0f,
                            1f
                        )
                    val percentage = (progress * 100).toInt()
                    val paymentDesc = stringResource(Res.string.payment_for, item.installment.title)
                    val reminderTitle = stringResource(Res.string.notif_installment_title)
                    val reminderMsg = stringResource(Res.string.notif_installment_desc, item.installment.title)

                    EntityItem(
                        id = item.installment.id,
                        name = item.installment.title,
                        sub = stringResource(
                            Res.string.remaining_installments,
                            (item.installment.totalInstallments - item.installment.paidInstallments)
                        ),
                        badge = "$percentage%",
                        color = if (item.installment.isCompleted) GlassGreen else MaterialTheme.colorScheme.primary,
                        sub2 = item.installment.installmentAmount.toInt()
                            .toSignedPersianPrice() + " " + stringResource(Res.string.currency_toman),
                        trailingContent = {
                            if (!item.installment.isCompleted) {
                                val glassColors = LocalGlassColors.current
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(glassColors.glass)
                                        .border(1.dp, glassColors.glassEdge, RoundedCornerShape(9.dp))
                                        .clickable {
                                            viewModel.onIntent(InstallmentIntent.MarkAsPaid(
                                                installmentId = item.installment.id,
                                                transactionDescription = paymentDesc,
                                                reminderTitle = reminderTitle,
                                                reminderMessage = reminderMsg
                                            ))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GlassGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    )
                },
                onEditClick = { selectedInstallmentId = it.id; showAddInstallment = true },
                onDeleteClick = { viewModel.onIntent(InstallmentIntent.Delete(it.id)) },
                showActions = true
            )
        }

        if (showAddInstallment) {
            AddInstallmentBottomSheet(
                installmentId = selectedInstallmentId,
                onDismiss = { showAddInstallment = false; selectedInstallmentId = null },
                onSuccess = { viewModel.onIntent(InstallmentIntent.Init) }
            )
        }
    }
}
