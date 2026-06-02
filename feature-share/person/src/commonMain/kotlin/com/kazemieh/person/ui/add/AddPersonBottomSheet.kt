package com.kazemieh.person.ui.add

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.model.resolveString
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import fintrack.core.designsystem.generated.resources.*
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
                            onDismiss()
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
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
fun AddPersonContent(
    state: AddPersonState,
    onIntent: (AddPersonIntent) -> Unit
) {
    AddFrame(
        title = if (state.mode == AddPersonMode.Add) stringResource(Res.string.title_new_person) else stringResource(Res.string.title_edit_person),
        sub = stringResource(Res.string.title_person_management),
        primaryLabel = stringResource(Res.string.btn_save_person),
        onPrimaryClick = { onIntent(AddPersonIntent.Save) },
        onClose = { onIntent(AddPersonIntent.OnDismiss) }
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
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_enter_person_name), color = GlassText3) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = GlassText, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Field(label = stringResource(Res.string.description_label)) {
                    TextField(
                        value = state.draft.description.orEmpty(),
                        onValueChange = { onIntent(AddPersonIntent.UpdateDescription(it)) },
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_optional_note), color = GlassText3) },
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
