package com.kazemieh.tag.ui

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
import com.kazemieh.tag.R
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagListBottomSheet(
    viewModel: TagViewModel = koinViewModel(),
    selectedTags: Set<Pair<Int, String>>?,
    onSubmitClick: (Set<Pair<Int, String>>?) -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(true) {
        viewModel.onIntent(TagIntent.GetAllTag)
        viewModel.onIntent(TagIntent.SetAllSelectedTags(selectedTags))
    }

    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.tags?.forEach { tag ->
                    val isSelected = state.selectedTags?.contains(tag) == true
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onIntent(TagIntent.SetSelectedTag(tag.first to tag.second))
                        },
                        label = { FintrackBodyMediumText(text = tag.second) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // دکمه افزودن تگ را کنار FlowRow یا بالای لیست قرار بده
            FloatingActionButton(
                onClick = { viewModel.onIntent(TagIntent.ShowAddTag(true)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.End) // بالا سمت راست
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_tag)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSubmitClick(state.selectedTags) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                FintrackBodyMediumText(text = stringResource(R.string.confirm))
            }
        }

        if (state.showAddTag) {
            AddTagBottomSheet(
                onDismiss = { viewModel.onIntent(TagIntent.ShowAddTag(false)) },
                setTag = { id, name -> viewModel.onIntent(TagIntent.SetSelectedTag(id to name)) }
            )
        }
    }
}

