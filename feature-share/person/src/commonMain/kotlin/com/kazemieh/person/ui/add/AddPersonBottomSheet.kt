package com.kazemieh.person.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.model.resolveString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_save_person
import fintrack.core.designsystem.generated.resources.description_label
import fintrack.core.designsystem.generated.resources.hint_enter_person_name
import fintrack.core.designsystem.generated.resources.hint_optional_note
import fintrack.core.designsystem.generated.resources.label_person_name
import fintrack.core.designsystem.generated.resources.title_edit_person
import fintrack.core.designsystem.generated.resources.title_new_person
import fintrack.core.designsystem.generated.resources.title_person_management
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonBottomSheet(
    viewModel: AddPersonViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedPerson: Person? = null,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Person) -> Unit)? = null,
    setPerson: (Person) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedPerson?.id) {
        if (selectedPerson != null) {
            viewModel.onIntent(AddPersonIntent.StartEdit(selectedPerson))
        } else {
            viewModel.onIntent(AddPersonIntent.StartAdd)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddPersonEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message.resolveString(),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is AddPersonEffect.SavedPerson -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setPerson(effect.person)
                        }
                    }
                }

                AddPersonEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddPersonIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddPersonContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToTransactions = onNavigateToTransactions
        )
    }
}

@Composable
fun AddPersonContent(
    state: AddPersonState,
    onIntent: (AddPersonIntent) -> Unit,
    onNavigateToTransactions: ((Person) -> Unit)? = null
) {
    AddFrame(
        title = if (state.mode == AddPersonMode.Add) stringResource(Res.string.title_new_person) else stringResource(
            Res.string.title_edit_person
        ),
        sub = stringResource(Res.string.title_person_management),
        heroName = state.draft.name,
        iconId = 1, // Default icon for person
        colorId = 1,
        primaryLabel = stringResource(Res.string.btn_save_person),
        onPrimaryClick = { onIntent(AddPersonIntent.Save) },
        onClose = { onIntent(AddPersonIntent.OnDismiss) },
        onFilterClick = if (state.mode is AddPersonMode.Edit && onNavigateToTransactions != null) {
            {
                (state.mode as? AddPersonMode.Edit)?.personId?.let { id ->
                    onNavigateToTransactions(
                        Person(
                            id = id,
                            name = state.draft.name,
                            description = state.draft.description
                        )
                    )
                    onIntent(AddPersonIntent.OnDismiss)
                }
            }
        } else null
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Field(label = stringResource(Res.string.label_person_name), required = true) {
                    TextField(
                        value = state.draft.name,
                        onValueChange = { onIntent(AddPersonIntent.UpdateName(it)) },
                        placeholder = {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.hint_enter_person_name),
                                color = GlassText3
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = GlassText,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Field(label = stringResource(Res.string.description_label)) {
                    TextField(
                        value = state.draft.description.orEmpty(),
                        onValueChange = { onIntent(AddPersonIntent.UpdateDescription(it)) },
                        placeholder = {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.hint_optional_note),
                                color = GlassText3
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = GlassText),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
