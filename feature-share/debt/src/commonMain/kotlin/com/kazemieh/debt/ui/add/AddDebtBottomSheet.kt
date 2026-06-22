package com.kazemieh.debt.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerSingleBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_debt
import fintrack.core.designsystem.generated.resources.date
import fintrack.core.designsystem.generated.resources.debt_amount
import fintrack.core.designsystem.generated.resources.debt_owed_by_me
import fintrack.core.designsystem.generated.resources.debt_owed_to_me
import fintrack.core.designsystem.generated.resources.description
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.person_choose
import fintrack.core.designsystem.generated.resources.person_name
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.source_choose
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtBottomSheet(
    onDismiss: () -> Unit,
    debtId: Long? = null,
    personId: Long? = null,
    viewModel: AddDebtViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    LaunchedEffect(debtId, personId) {
        if (debtId != null) {
            viewModel.onIntent(AddDebtIntent.LoadDebt(debtId))
        } else if (personId != null) {
            viewModel.onIntent(AddDebtIntent.SetPersonById(personId))
        }
    }

    var showPersonPicker by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ScreenHeader(
                    title = if (state.debtId == null) stringResource(Res.string.add_debt) else stringResource(Res.string.edit),
                    onClose = onDismiss
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(space.large),
                    verticalArrangement = Arrangement.spacedBy(space.medium)
                ) {
                    // Type Selector
                    GlassCard(padding = 0.dp) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TypeButton(
                                text = stringResource(Res.string.debt_owed_by_me),
                                selected = state.type == DebtType.OWED_BY_ME,
                                onClick = { viewModel.onIntent(AddDebtIntent.SetType(DebtType.OWED_BY_ME)) },
                                modifier = Modifier.weight(1f),
                                selectedColor = MaterialTheme.colorScheme.error
                            )
                            TypeButton(
                                text = stringResource(Res.string.debt_owed_to_me),
                                selected = state.type == DebtType.OWED_TO_ME,
                                onClick = { viewModel.onIntent(AddDebtIntent.SetType(DebtType.OWED_TO_ME)) },
                                modifier = Modifier.weight(1f),
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Amount
                    FintrackOutlinedTextField(
                        value = state.amount,
                        onValueChange = { viewModel.onIntent(AddDebtIntent.SetAmount(it)) },
                        label = { FintrackBodyMediumText(stringResource(Res.string.debt_amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Person Picker
                    PickerField(
                        label = stringResource(Res.string.person_name),
                        value = state.person?.name ?: stringResource(Res.string.person_choose),
                        icon = Icons.Default.Person,
                        onClick = { showPersonPicker = true }
                    )

                    // Date Picker (Simplified for now, using text field or existing date logic)
                    PickerField(
                        label = stringResource(Res.string.date),
                        value = Instant.fromEpochMilliseconds(state.date)
                            .toPersianDateTime(TimeZone.currentSystemDefault()).toDateString(),
                        icon = Icons.Default.CalendarMonth,
                        onClick = { /* TODO: Date Picker */ }
                    )

                    // Source Picker (Optional for settlement)
                    PickerField(
                        label = stringResource(Res.string.source),
                        value = state.source?.name ?: stringResource(Res.string.source_choose),
                        icon = Icons.Default.Wallet,
                        onClick = { showSourcePicker = true }
                    )

                    // Description
                    FintrackOutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onIntent(AddDebtIntent.SetDescription(it)) },
                        label = { FintrackBodyMediumText(stringResource(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(space.medium))

                    Button(
                        onClick = { viewModel.onIntent(AddDebtIntent.Submit) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        FintrackBodyLargeText(stringResource(Res.string.save_))
                    }
                }
            }
        }
    }

    if (showPersonPicker) {
        PersonPickerSingleBottomSheet(
            onPersonClick = {
                viewModel.onIntent(AddDebtIntent.SetPerson(it))
                showPersonPicker = false
            },
            onDismiss = { showPersonPicker = false }
        )
    }

    if (showSourcePicker) {
        SourcePickerBottomSheet(
            onSourceClick = {
                viewModel.onIntent(AddDebtIntent.SetSource(it))
                showSourcePicker = false
            },
            onDismiss = { showSourcePicker = false }
        )
    }
}

@Composable
fun TypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color
) {
    val space = LocalSpacing.current
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        color = if (selected) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
        contentColor = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            FintrackLabelLargeText(
                text = text,
                color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PickerField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    Column(modifier = Modifier.fillMaxWidth()) {
        FintrackLabelMediumText(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(space.extraSmall))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.padding(space.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(space.small))
                FintrackBodyLargeText(text = value, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
