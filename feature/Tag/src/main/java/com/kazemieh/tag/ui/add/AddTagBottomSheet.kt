package com.kazemieh.tag.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.tag.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagBottomSheet(
    viewModel: AddTagViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    setTag: (id: Int, name: String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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

                is AddTagEffect.AddedTag -> setTag(effect.id, effect.name)
                AddTagEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddTagIntent.OnDismiss) },
        sheetState = sheetState
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                FintrackOutlinedTextField(
                    value = state.tagName ?: "",
                    onValueChange = { viewModel.onIntent(AddTagIntent.SetTagName(it)) },
                    label = {
                        Row {
                            FintrackBodyMediumText(text = stringResource(R.string.tag_name_label))
                            FintrackBodyMediumText(
                                text = stringResource(R.string.required_star),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                FintrackOutlinedTextField(
                    value = state.description ?: "",
                    onValueChange = { viewModel.onIntent(AddTagIntent.SetDescription(it)) },
                    label = { FintrackBodyMediumText(text = stringResource(R.string.description_label)) }
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.onIntent(AddTagIntent.AddTag) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(text = stringResource(R.string.save_tag))
                }
            }

            Box {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

