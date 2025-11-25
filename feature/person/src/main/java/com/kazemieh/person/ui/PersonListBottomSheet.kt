package com.kazemieh.person.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.kazemieh.common.safeLet
import com.kazemieh.designsystem.component.EmptyListScreen
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

                if (state.persons.isNotEmpty())
                    state.persons.forEach { person ->
                        val isSelected =
                            state.selectedPersons?.contains(person.id?.toInt() to person.name) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                safeLet(person.id, person.name) { id, name ->
                                    viewModel.onIntent(PersonIntent.SetSelectedPerson(id.toInt() to name))
                                }
                            },
                            label = {
                                FintrackBodyMediumText(
                                    text = person.name,
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
                else EmptyListScreen()
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonListSelectionBottomSheet(
    viewModel: PersonViewModel = koinViewModel(),
    onPersonClick: (Set<Pair<Int, String>>) -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(true) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
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
                .padding(12.dp)
        ) {

            LazyColumn {
                if (state.persons.isNotEmpty())
                    items(state.persons) { person ->
                        val isSelected =
                            state.selectedPersons?.contains(person.id?.toInt() to person.name) == true
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    safeLet(person.id, person.name) { id, name ->
                                        viewModel.onIntent(PersonIntent.SetSelectedPerson(id.toInt() to name))
                                    }

                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        safeLet(person.id, person.name) { id, name ->
                                            viewModel.onIntent(PersonIntent.SetSelectedPerson(id.toInt() to name))
                                        }

                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))

                                FintrackBodyMediumText(text = person.name)

                            }

                        }
                    }
                else item { EmptyListScreen() }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onPersonClick(state.selectedPersons ?: emptySet()) },
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
    }
}

