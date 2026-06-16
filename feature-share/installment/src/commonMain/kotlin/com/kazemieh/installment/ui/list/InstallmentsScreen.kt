package com.kazemieh.installment.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.common.persiandatetime.domain.PersianDateTime
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.EmptyList
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.installment.ui.InstallmentIntent
import com.kazemieh.installment.ui.InstallmentViewModel
import com.kazemieh.installment.ui.add.AddInstallmentBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.installment_description
import fintrack.core.designsystem.generated.resources.installment_next_date
import fintrack.core.designsystem.generated.resources.installment_overdue
import fintrack.core.designsystem.generated.resources.installment_paid
import fintrack.core.designsystem.generated.resources.installment_reminder_message
import fintrack.core.designsystem.generated.resources.installment_upcoming
import fintrack.core.designsystem.generated.resources.mark_as_paid
import fintrack.core.designsystem.generated.resources.remaining_installments
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InstallmentsScreen(
    viewModel: InstallmentViewModel = koinViewModel(),
    onAddInstallment: () -> Unit // This is now handled internally but kept for signature if needed
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddInstallment by remember { mutableStateOf(false) }

    val tabs = listOf(
        stringResource(Res.string.installment_upcoming),
        stringResource(Res.string.installment_overdue),
        stringResource(Res.string.installment_paid)
    )

    LaunchedEffect(Unit) {
        viewModel.onIntent(InstallmentIntent.Init)
    }

    FintrackScreen(
        floatingActionButton = {
            FAB(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(space.large)
                    .padding(bottom = 100.dp) // Avoid navigation bar
            ) {
                showAddInstallment = true
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            FintrackLabelMediumText(
                                text = title,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            val items = when (selectedTabIndex) {
                0 -> state.upcoming
                1 -> state.overdue
                else -> state.completed
            }

            if (items.isEmpty()) {
                EmptyList(
                    title = tabs[selectedTabIndex],
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(items, key = { it.installment.id }) { item ->
                        val transactionDescription = stringResource(
                            Res.string.installment_description,
                            item.installment.paidInstallments + 1,
                            item.installment.totalInstallments,
                            item.installment.title
                        )
                        val reminderTitle =
                            stringResource(Res.string.installment_next_date) + ": " + item.installment.title
                        val reminderMessage = stringResource(
                            Res.string.installment_reminder_message,
                            item.installment.installmentAmount.toInt().toSignedPersianPrice()
                        )

                        InstallmentItem(
                            item = item,
                            onMarkAsPaid = {
                                viewModel.onIntent(
                                    InstallmentIntent.MarkAsPaid(
                                        installmentId = item.installment.id,
                                        transactionDescription = transactionDescription,
                                        reminderTitle = reminderTitle,
                                        reminderMessage = reminderMessage
                                    )
                                )
                            },
                            onDelete = { viewModel.onIntent(InstallmentIntent.Delete(item.installment.id)) }
                        )
                    }
                }
            }
        }

        if (showAddInstallment) {
            AddInstallmentBottomSheet(
                onDismiss = { showAddInstallment = false },
                onSuccess = { viewModel.onIntent(InstallmentIntent.Init) }
            )
        }
    }
}

@Composable
fun InstallmentItem(
    item: InstallmentWithRelations,
    onMarkAsPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val space = LocalSpacing.current
    val progress = item.installment.paidInstallments.toFloat() / item.installment.totalInstallments

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = space.extraSmall),
        padding = space.medium
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FintrackTitleMediumText(
                        text = item.installment.title,
                        color = glassColors.text
                    )
                    FintrackLabelSmallText(
                        text = stringResource(
                            Res.string.remaining_installments,
                            (item.installment.totalInstallments - item.installment.paidInstallments)
                        ),
                        color = glassColors.text3
                    )
                }

                FintrackTitleMediumText(
                    text = item.installment.installmentAmount.toInt().toSignedPersianPrice(),
                    color = glassColors.text
                )
            }

            Spacer(Modifier.height(space.medium))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GlassGreen,
                trackColor = glassColors.glassEdge
            )

            Spacer(Modifier.height(space.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackLabelSmallText(
                    text = PersianDateTime.parse(item.installment.nextDueDate).toDateString(),
                    color = glassColors.text3
                )

                if (!item.installment.isCompleted) {
                    FintrackButton(
                        text = stringResource(Res.string.mark_as_paid),
                        onClick = onMarkAsPaid
                    )
                }
            }
        }
    }
}
