package com.kazemieh.person.ui.add

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedPerson) {
        if (selectedPerson != null) {
            viewModel.onIntent(AddPersonIntent.ShowEditData(selectedPerson))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddPersonEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is AddPersonEffect.AddedPerson -> setPerson(effect.person)
                AddPersonEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddPersonIntent.OnDismiss) },
        primaryButtonText = stringResource(R.string.save_person),
        onPrimaryClick = { viewModel.onIntent(AddPersonIntent.AddPerson) }
    ) {
        NameDescriptionFields(
            name = state.personName.orEmpty(),
            onNameChange = { viewModel.onIntent(AddPersonIntent.SetPersonName(it)) },
            nameLabel = stringResource(R.string.person_name_label),
            description = state.description.orEmpty(),
            onDescriptionChange = { viewModel.onIntent(AddPersonIntent.SetDescription(it)) },
            descriptionLabel = stringResource(R.string.description_label),
        )
    }
}
