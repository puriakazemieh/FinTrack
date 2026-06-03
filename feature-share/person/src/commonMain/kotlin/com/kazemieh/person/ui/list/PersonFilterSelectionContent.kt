package com.kazemieh.person.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Person
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.person.ui.add.AddPersonBottomSheet
import com.kazemieh.person.ui.delete.DeletePersonBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonFilterSelectionContent(
    viewModel: PersonViewModel = koinViewModel(key = "PersonFilterSelectionContent"),
    snackbarHostState: SnackbarHostState,
    selectedPersons: Set<Person>,
    isAllSelected: Boolean,
    onSelectionChanged: (Set<Person>, Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(PersonIntent.GetAllPerson)
    }

    val state by viewModel.state.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.persons),
                    color = GlassText2,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GlassPurple,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { viewModel.onIntent(PersonIntent.ShowAddPerson) }
                )
            }
            FintrackLabelSmallText(
                text = stringResource(Res.string.all),
                color = if (isAllSelected) GlassGreen else GlassText3,
                modifier = Modifier.clickable {
                    val all = state.persons.toSet()
                    onSelectionChanged(if (isAllSelected) emptySet() else all, !isAllSelected)
                }
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.persons.forEach { person ->
                val active = isAllSelected || selectedPersons.contains(person)
                Chip(
                    active = active,
                    color = GlassPurple,
                    onClick = {
                        val fullSet = state.persons.toSet()
                        val currentSelected = if (isAllSelected) fullSet else selectedPersons
                        val newSet = currentSelected.toMutableSet()
                        
                        if (newSet.contains(person)) {
                            newSet.remove(person)
                        } else {
                            newSet.add(person)
                        }
                        
                        val isAllNow = newSet.size == fullSet.size
                        onSelectionChanged(if (isAllNow) emptySet() else newSet, isAllNow)
                    },
                    onLongClick = { viewModel.onIntent(PersonIntent.OnEditClick(person)) }
                ) {
                    FintrackLabelSmallText(
                        text = person.name + if (active) " ✓" else "",
                        color = if (active) GlassBg0 else GlassText2
                    )
                }
            }
        }
    }

    if (state.showAddPerson) {
        AddPersonBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedPerson = state.selectedPerson,
            onDismiss = { viewModel.onIntent(PersonIntent.ShowAddPerson) },
            setPerson = { viewModel.onIntent(PersonIntent.SetSelectedPerson(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedPerson != null) {
        DeletePersonBottomSheet(
            snackbarHostState = snackbarHostState,
            person = state.selectedPerson!!,
            onDismiss = { viewModel.onIntent(PersonIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(PersonIntent.OnDeleteClick()) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
