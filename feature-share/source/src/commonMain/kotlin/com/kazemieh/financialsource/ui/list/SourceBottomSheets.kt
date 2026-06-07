package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.kazemieh.common.model.Source
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.component.model.toItemUi
import com.kazemieh.designsystem.component.model.toSource
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.financialsource.ui.delete.DeleteSourceBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_source
import fintrack.core.designsystem.generated.resources.balance
import fintrack.core.designsystem.generated.resources.cancell_
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.financial_sources
import fintrack.core.designsystem.generated.resources.source
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceBottomSheetCore(
    keyViewmodel: String,
    snackbarHostState: SnackbarHostState,
    isDeleteShow: Boolean,
    isEditShow: Boolean,
    clickable: Boolean,
    onSourceClick: (Source) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Source) -> Unit)? = null,
    showEditButton: Boolean = true
) {
    val viewModel: SourceViewModel = koinViewModel(key = keyViewmodel)

    LaunchedEffect(Unit) { viewModel.onIntent(SourceIntent.LoadAllSource) }
    val state by viewModel.state.collectAsState()
    var isEditMode by remember { mutableStateOf(isEditShow) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SourceEffect.AddedSource -> onSourceClick(effect.source)
                SourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.onIntent(SourceIntent.ResetFlags)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = stringResource(Res.string.source),
                onClose = {
                    viewModel.onIntent(SourceIntent.ResetFlags)
                    onDismiss()
                },
                trailingContent = {
                    if (showEditButton) {
                        TextButton(onClick = { isEditMode = !isEditMode }) {
                            FintrackLabelMediumText(
                                text = if (isEditMode) stringResource(Res.string.cancell_) else stringResource(Res.string.edit),
                                color = GlassText2
                            )
                        }
                    }
                }
            )

            EntityList(
                title = stringResource(Res.string.source),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(SourceIntent.UpdateSearchQuery(it)) },
                showActions = isEditMode,
                items = state.filteredSources.map { source ->
                    EntityItem(
                        id = source.id ?: 0L,
                        name = source.name,
                        sub = if (source.type == 1) source.cardNumber else source.description,
                        badge = source.formattedBalance + " " + stringResource(Res.string.currency_toman),
                        iconId = source.iconId,
                        colorId = source.colorId
                    )
                },
                onItemClick = { item ->
                    if (clickable) {
                        state.sources.find { it.id == item.id }?.let {
                            viewModel.onIntent(SourceIntent.SelectedSource(it))
                        }
                    }
                },
                onAddClick = { viewModel.onIntent(SourceIntent.OnAddSourceClick) },
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.sources.find { it.id == item.id }?.let { callback(it) }
                        onDismiss()
                    }
                },
                onEditClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        viewModel.onIntent(SourceIntent.OnEditClick(it))
                    }
                },
                onDeleteClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        viewModel.onIntent(SourceIntent.OnDeleteClick(it))
                    }
                }
            )
        }
    }

    if (state.isAddShow) {
        AddSourceBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedSource = state.selectedSources,
            onDismiss = { viewModel.onIntent(SourceIntent.ResetFlags) },
            onNavigateToTransactions = onNavigateToTransactions?.let { navCallback ->
                { source ->
                    navCallback(source)
                    onDismiss()
                }
            },
            setSource = { viewModel.onIntent(SourceIntent.SelectedSource(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedSources != null) {
        DeleteSourceBottomSheet(
            snackbarHostState = snackbarHostState,
            source = state.selectedSources!!,
            onDismiss = { viewModel.onIntent(SourceIntent.ResetFlags) },
            deleted = { viewModel.onIntent(SourceIntent.ResetFlags) }
        )
    }
}

@Composable
fun SourceManageBottomSheet(
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onSourceClick: (Source) -> Unit = {},
    onNavigateToTransactions: ((Source) -> Unit)? = null,
) = SourceBottomSheetCore(
    keyViewmodel = "SourceManageBottomSheet",
    snackbarHostState = snackbarHostState,
    isDeleteShow = true,
    isEditShow = true,
    clickable = false,
    onSourceClick = onSourceClick,
    onDismiss = onDismiss,
    onNavigateToTransactions = onNavigateToTransactions,
    showEditButton = false
)

@Composable
fun SourcePickerBottomSheet(
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onSourceClick: (Source) -> Unit = {},
    onNavigateToTransactions: ((Source) -> Unit)? = null,
) = SourceBottomSheetCore(
    keyViewmodel = "SourcePickerBottomSheet",
    snackbarHostState = snackbarHostState,
    isDeleteShow = false,
    isEditShow = false,
    clickable = true,
    onSourceClick = onSourceClick,
    onDismiss = onDismiss,
    onNavigateToTransactions = onNavigateToTransactions,
    showEditButton = true
)


@Composable
fun SourceSelectionBottomSheet(
    viewModel: SourceViewModel = koinViewModel(key = "SourceSelectionBottomSheet"),
    initialSelectionPairs: Set<Source> = emptySet(),
    isAllSelected: Boolean = true,
    onConfirmPairs: (Set<Source>, isAllSelected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
    }

    val initialSelectionIds =
        if (isAllSelected) state.items else initialSelectionPairs.map { it.toItemUi() }.toSet()

    SelectableListBottomSheet(
        title = stringResource(Res.string.source),
        items = state.items,
        initialSelection = initialSelectionIds,
        query = state.searchQuery,
        onQueryChange = { viewModel.onIntent(SourceIntent.UpdateSearchQuery(it)) },
        onConfirm = { selectedItems, isAll ->
            onConfirmPairs(selectedItems.map { it.toSource() }.toSet(), isAll)
            viewModel.onIntent(SourceIntent.OnDismiss)
        },
        onDismiss = {
            viewModel.onIntent(SourceIntent.OnDismiss)
        }
    )
}


@Composable
fun SourceList(
    viewModel: SourceViewModel = koinViewModel(),
    onAddSourceClick: () -> Unit
) {
    val space = LocalSpacing.current
    LaunchedEffect(true) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
    }

    val state by viewModel.state.collectAsState()
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FintrackTitleMediumText(
                text = stringResource(Res.string.financial_sources),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = onAddSourceClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_source),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {

            items(state.sources) { source ->
                Card(
                    modifier = Modifier.padding(horizontal = space.small),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = space.one)
                ) {
                    Column(
                        modifier = Modifier.padding(space.large)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FinTrackLeadingIcon(
                                colorId = source.colorId,
                                iconId = source.iconId,
                                style = LeadingIconStyle.Badge,
                                size = space.extraLarge,
                                iconSize = space.large,
                            )
                            Spacer(Modifier.width(space.medium))
                            FintrackBodyMediumText(text = source.name)
                        }
                        FintrackBodySmallText(
                            text = stringResource(
                                Res.string.balance,
                                source.formattedBalance.toPersianDigits()
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        }
    }
}

