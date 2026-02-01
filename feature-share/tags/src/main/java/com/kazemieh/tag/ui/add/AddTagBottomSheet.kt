package com.kazemieh.tag.ui.add

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
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagBottomSheet(
    viewModel: AddTagViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedTag: Tag? = null,
    onDismiss: () -> Unit,
    setTag: (Tag) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedTag) {
        if (selectedTag != null) {
            viewModel.onIntent(AddTagIntent.ShowEditData(selectedTag))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTagEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is AddTagEffect.AddedTag -> setTag(effect.tag)
                AddTagEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddTagIntent.OnDismiss) },
        primaryButtonText = stringResource(R.string.save_tag),
        onPrimaryClick = { viewModel.onIntent(AddTagIntent.AddTag) }
    ) {
        NameDescriptionFields(
            name = state.tagName.orEmpty(),
            onNameChange = { viewModel.onIntent(AddTagIntent.SetTagName(it)) },
            nameLabel = stringResource(R.string.tag_name_label),
            description = state.description.orEmpty(),
            onDescriptionChange = { viewModel.onIntent(AddTagIntent.SetDescription(it)) },
            descriptionLabel = stringResource(R.string.description_label),
        )
    }
}
