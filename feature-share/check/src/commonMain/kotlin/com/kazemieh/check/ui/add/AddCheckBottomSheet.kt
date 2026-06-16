package com.kazemieh.check.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.person.ui.list.PersonPickerSingleBottomSheet
import fintrack.core.designsystem.generated.resources.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCheckBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AddCheckViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddCheckEffect.Saved -> onDismiss()
            }
        }
    }

    var showPersonPicker by remember { mutableStateOf(false) }

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
                    title = stringResource(Res.string.title_add_check),
                    onClose = onDismiss
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(space.large),
                    verticalArrangement = Arrangement.spacedBy(space.medium)
                ) {
                    // Incoming/Outgoing Toggle
                    GlassCard(padding = 0.dp) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TypeButton(
                                text = stringResource(Res.string.label_outgoing_check),
                                selected = !state.isIncoming,
                                onClick = { viewModel.onIntent(AddCheckIntent.SetIsIncoming(false)) },
                                modifier = Modifier.weight(1f),
                                selectedColor = MaterialTheme.colorScheme.error
                            )
                            TypeButton(
                                text = stringResource(Res.string.label_incoming_check),
                                selected = state.isIncoming,
                                onClick = { viewModel.onIntent(AddCheckIntent.SetIsIncoming(true)) },
                                modifier = Modifier.weight(1f),
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Amount
                    FintrackOutlinedTextField(
                        value = state.amount,
                        onValueChange = { viewModel.onIntent(AddCheckIntent.SetAmount(it)) },
                        label = { FintrackBodyMediumText(stringResource(Res.string.label_check_amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Person Picker
                    PickerField(
                        label = stringResource(Res.string.label_counterparty),
                        value = state.person?.name ?: stringResource(Res.string.not_choose),
                        icon = Icons.Default.Person,
                        onClick = { showPersonPicker = true }
                    )

                    // Date Picker (Issued Date)
                    PickerField(
                        label = stringResource(Res.string.label_issue_date),
                        value = Instant.fromEpochMilliseconds(state.date).toPersianDateTime(TimeZone.currentSystemDefault()).toDateString(),
                        icon = Icons.Default.CalendarMonth,
                        onClick = { /* TODO: Date Picker */ }
                    )

                    // Due Date Picker
                    PickerField(
                        label = stringResource(Res.string.label_due_date),
                        value = Instant.fromEpochMilliseconds(state.dueDate).toPersianDateTime(TimeZone.currentSystemDefault()).toDateString(),
                        icon = Icons.Default.CalendarMonth,
                        onClick = { /* TODO: Date Picker */ }
                    )

                    // Description
                    FintrackOutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onIntent(AddCheckIntent.SetDescription(it)) },
                        label = { FintrackBodyMediumText(stringResource(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(space.medium))

                    Button(
                        onClick = { viewModel.onIntent(AddCheckIntent.Submit) },
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
            snackbarHostState = remember { SnackbarHostState() },
            onPersonClick = {
                viewModel.onIntent(AddCheckIntent.SetPerson(it))
                showPersonPicker = false
            },
            onDismiss = { showPersonPicker = false }
        )
    }
}

@Composable
private fun TypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        color = if (selected) selectedColor.copy(alpha = 0.2f) else Color.Transparent,
        contentColor = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            FintrackLabelLargeText(text = text, color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PickerField(
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
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(space.small))
                FintrackBodyLargeText(text = value, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
