package com.kazemieh.person.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import com.kazemieh.person.ui.delete.DeletePersonBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.persons
import fintrack.core.designsystem.generated.resources.title_person_management
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonsScreen(
    onNavigateToDetail: (Person) -> Unit,
    onBack: () -> Unit,
    viewModel: PersonViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    FintrackScreen(
        title = stringResource(Res.string.persons),
        sub = stringResource(Res.string.title_person_management),
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.persons),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(PersonIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
                showActions = true,
                items = state.filteredPersons.map {
                    EntityItem(
                        id = it.id ?: 0,
                        name = it.name,
                        sub = it.description,
                        iconId = 1,
                        colorId = 1
                    )
                },
                onEditClick = { item ->
                    state.persons.find { it.id == item.id }?.let {
                        viewModel.onIntent(PersonIntent.OnEditClick(it))
                    }
                },
                onDeleteClick = { item ->
                    state.persons.find { it.id == item.id }?.let {
                        viewModel.onIntent(PersonIntent.OnDeleteClick(it))
                    }
                },
                onItemClick = { item ->
                    state.persons.find { it.id == item.id }?.let { onNavigateToDetail(it) }
                },
                onMove = { from, to ->
                    val list = state.persons.toMutableList()
                    list.add(to, list.removeAt(from))
                    val positions = list.mapIndexed { index, person ->
                        person.id!! to index
                    }.toMap()
                    viewModel.onIntent(PersonIntent.UpdatePositions(positions))
                },
                isReorderMode = state.isReorderShow
            )
        }

        if (state.showAddPerson) {
            AddPersonBottomSheet(
                selectedPerson = state.selectedPerson,
                onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
                setPerson = { viewModel.onIntent(PersonIntent.SetSelectedPerson(it)) }
            )
        }

        if (state.isDeleteShow && state.selectedPerson != null) {
            DeletePersonBottomSheet(
                person = state.selectedPerson!!,
                onDismiss = { viewModel.onIntent(PersonIntent.ResetFlags) },
                deleted = { viewModel.onIntent(PersonIntent.ResetFlags) }
            )
        }
    }
}
