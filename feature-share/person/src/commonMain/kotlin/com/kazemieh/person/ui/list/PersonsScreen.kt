package com.kazemieh.person.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.HeaderAction
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import com.kazemieh.person.ui.delete.DeletePersonBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonsScreen(
    onNavigateToDetail: (Person) -> Unit,
    onBack: () -> Unit,
    onNavigateToTransactions: ((Person) -> Unit)? = null,
    viewModel: PersonViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    FintrackScreen(
        title = stringResource(Res.string.persons),
        sub = stringResource(Res.string.title_person_management),
        onBack = onBack,
        actions = if (state.isReorderShow) {
            listOf(
                HeaderAction(
                    icon = rememberVectorPainter(Icons.Default.Check),
                    label = "Done",
                    onClick = { viewModel.onIntent(PersonIntent.OnToggleReorder) },
                    color = com.kazemieh.designsystem.GlassGreen
                )
            )
        } else {
            emptyList()
        },
        trailingContent = {
            if (!state.isReorderShow) {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.edit)) },
                            onClick = {
                                viewModel.onIntent(PersonIntent.OnToggleReorder)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
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
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.persons.find { it.id == item.id }?.let { callback(it) }
                    }
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
