package com.kazemieh.person.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.person.R
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonListBottomSheet(
    viewModel: PersonViewModel = koinViewModel(),
    selectedPersons: Set<Pair<Int, String>>?,
    onSubmitClick: (Set<Pair<Int, String>>?) -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(true) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
        viewModel.onIntent(PersonIntent.SetAllSelectedPersons(selectedPersons))
    }

    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.persons?.forEach { person ->
                    val isSelected = state.selectedPersons?.contains(person) == true
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onIntent(PersonIntent.SetSelectedPerson(person.first to person.second))
                        },
                        label = {
                            FintrackBodyMediumText(
                                text = person.second,
                                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onBackground
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            Box(
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.onIntent(PersonIntent.ShowAddPerson(true)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_person)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSubmitClick(state.selectedPersons) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                FintrackBodyMediumText(
                    text = stringResource(R.string.confirm),
                    color = MaterialTheme.colorScheme.background
                )
            }
        }

        if (state.showAddPerson) {
            AddPersonBottomSheet(
                onDismiss = { viewModel.onIntent(PersonIntent.ShowAddPerson(false)) },
                setPerson = { id, name -> viewModel.onIntent(PersonIntent.SetSelectedPerson(id to name)) }
            )
        }
    }
}

