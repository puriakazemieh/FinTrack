package com.kazemieh.person.ui.add

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
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonBottomSheet(
    viewModel: AddPersonViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    setPerson: (Person) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddPersonIntent.OnDismiss) },
        sheetState = sheetState
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.mediumLarge)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(space.mediumLarge)) {

                FintrackOutlinedTextField(
                    value = state.personName ?: "",
                    onValueChange = { viewModel.onIntent(AddPersonIntent.SetPersonName(it)) },
                    label = {
                        Row {
                            FintrackBodyMediumText(text = stringResource(R.string.person_name_label))
                            FintrackBodyMediumText(
                                text = stringResource(R.string.required_star),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                FintrackOutlinedTextField(
                    value = state.description ?: "",
                    onValueChange = { viewModel.onIntent(AddPersonIntent.SetDescription(it)) },
                    label = { FintrackBodyMediumText(text = stringResource(R.string.description_label)) }
                )

                Spacer(Modifier.height(space.large))

                Button(
                    onClick = { viewModel.onIntent(AddPersonIntent.AddPerson) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackBodyMediumText(text = stringResource(R.string.save_person))
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

