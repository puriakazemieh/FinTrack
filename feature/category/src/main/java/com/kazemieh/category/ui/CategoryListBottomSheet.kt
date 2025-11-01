package com.kazemieh.category.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.kazemieh.category.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(),
    selectedTransactionType: Int,
    onCategoryClick: (id: Int, name: String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(true) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(selectedTransactionType))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            LazyColumn {
                items(state.categories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                category.id?.toInt()?.let { onCategoryClick(it, category.name) }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            FintrackBodyMediumText(text = category.name)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                FloatingActionButton(
                    onClick = onAddCategoryClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_category)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListSelectionBottomSheet(
    viewModel: CategoryViewModel = koinViewModel(),
    onCategoryClick: (Set<Pair<Int, String>>) -> Unit,
    selectedTransactionType: Int,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(true) {
        viewModel.onIntent(CategoryIntent.LoadCategoryByType(selectedTransactionType))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {

        Box(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            LazyColumn {
                items(state.categories) { category ->
                    val isSelected =
                        state.selectedCategory?.contains(category.id?.toInt() to category.name) == true
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.onIntent(
                                    CategoryIntent.SelectedCategory(
                                        (category.id?.toInt() ?: 0) to category.name
                                    )
                                )
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
                                    viewModel.onIntent(
                                        CategoryIntent.SelectedCategory(
                                            (category.id?.toInt() ?: 0) to category.name
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))

                            FintrackBodyMediumText(text = category.name)

                        }

                    }

                }

                item { Spacer(Modifier.height(40.dp)) }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCategoryClick(state.selectedCategory ?: emptySet()) },
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


}