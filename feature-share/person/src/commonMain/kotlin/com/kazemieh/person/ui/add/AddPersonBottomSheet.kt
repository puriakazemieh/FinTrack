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
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import com.kazemieh.designsystem.component.model.resolveString
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

                is AddPersonEffect.SavedPerson -> setPerson(effect.person)
                AddPersonEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddPersonIntent.OnDismiss) },
        primaryButtonText = stringResource(Res.string.save_person),
        onPrimaryClick = { viewModel.onIntent(AddPersonIntent.Save) }
    ) {
        NameDescriptionFields(
            name = state.draft.name,
            onNameChange = { viewModel.onIntent(AddPersonIntent.UpdateName(it)) },
            nameLabel = stringResource(Res.string.person_name_label),
            description = state.draft.description.orEmpty(),
            onDescriptionChange = { viewModel.onIntent(AddPersonIntent.UpdateDescription(it)) },
            descriptionLabel = stringResource(Res.string.description_label),

            isIconShow = false
        )
    }
}
