package com.kazemieh.tag.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.ColorSwatches
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.IconGrid
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_save_tag
import fintrack.core.designsystem.generated.resources.description_label
import fintrack.core.designsystem.generated.resources.hint_optional_note
import fintrack.core.designsystem.generated.resources.hint_search_in
import fintrack.core.designsystem.generated.resources.label_color
import fintrack.core.designsystem.generated.resources.label_icon
import fintrack.core.designsystem.generated.resources.tag_name_label
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_edit_tag
import fintrack.core.designsystem.generated.resources.title_new_tag
import fintrack.core.designsystem.generated.resources.title_tag_management
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagBottomSheet(
    viewModel: AddTagViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedTag: Tag? = null,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Tag) -> Unit)? = null,
    setTag: (Tag) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedTag?.id) {
        if (selectedTag != null) {
            viewModel.onIntent(AddTagIntent.StartEdit(selectedTag))
        } else {
            viewModel.onIntent(AddTagIntent.StartAdd)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTagEffect.SavedTag -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setTag(effect.tag)
                        }
                    }
                }

                AddTagEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddTagIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddTagContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToTransactions = onNavigateToTransactions
        )
    }
}

@Composable
fun AddTagContent(
    state: AddTagState,
    onIntent: (AddTagIntent) -> Unit,
    onNavigateToTransactions: ((Tag) -> Unit)? = null
) {
    val rainbowColors = FinTrackPickerColors.rainbow()
    val colors = rainbowColors.map { it.color }
    val selectedColorIndex = remember(state.draft.colorId, rainbowColors) {
        rainbowColors.indexOfFirst { it.id == state.draft.colorId }.coerceAtLeast(0)
    }
    val selectedIconIndex = remember(state.draft.iconId) {
        FinTrackIcons.icons.indexOfFirst { it.id == state.draft.iconId }.coerceAtLeast(0)
    }

    AddFrame(
        title = if (state.mode == AddTagMode.Add) stringResource(Res.string.title_new_tag) else stringResource(
            Res.string.title_edit_tag
        ),
        sub = stringResource(Res.string.title_tag_management),
        primaryLabel = stringResource(Res.string.btn_save_tag),
        onPrimaryClick = { onIntent(AddTagIntent.Save) },
        onClose = { onIntent(AddTagIntent.OnDismiss) },
        onFilterClick = if (state.mode is AddTagMode.Edit && onNavigateToTransactions != null) {
            {
                (state.mode as? AddTagMode.Edit)?.tagId?.let { id ->
                    onNavigateToTransactions(
                        Tag(
                            id = id,
                            name = state.draft.name,
                            description = state.draft.description,
                            colorId = state.draft.colorId ?: 1,
                            iconId = state.draft.iconId ?: 1
                        )
                    )
                    onIntent(AddTagIntent.OnDismiss)
                }
            }
        } else null,
        iconId = state.draft.iconId,
        colorId = state.draft.colorId,
        heroName = state.draft.name
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Field(label = stringResource(Res.string.tag_name_label), required = true) {
                    TextField(
                        value = state.draft.name,
                        onValueChange = { onIntent(AddTagIntent.UpdateName(it)) },
                        placeholder = {
                            FintrackBodyMediumText(
                                text = stringResource(
                                    Res.string.hint_search_in,
                                    stringResource(Res.string.tags)
                                ).replace(stringResource(Res.string.hint_search_in, ""), ""),
                                color = GlassText3
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = GlassText,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Field(label = stringResource(Res.string.description_label)) {
                    TextField(
                        value = state.draft.description.orEmpty(),
                        onValueChange = { onIntent(AddTagIntent.UpdateDescription(it)) },
                        placeholder = {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.hint_optional_note),
                                color = GlassText3
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = GlassText),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.label_color),
                            color = GlassText3
                        )
                        ColorSwatches(
                            colors = colors,
                            pickedIndex = selectedColorIndex,
                            onColorPick = {
                                onIntent(
                                    AddTagIntent.SetColorIcon(
                                        rainbowColors[it].id,
                                        state.draft.iconId
                                    )
                                )
                            }
                        )
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.label_icon),
                            color = GlassText3
                        )
                        IconGrid(
                            icons = FinTrackIcons.icons,
                            pickedIndex = selectedIconIndex,
                            color = rainbowColors[selectedColorIndex].color,
                            onIconPick = {
                                onIntent(
                                    AddTagIntent.SetColorIcon(
                                        state.draft.colorId,
                                        FinTrackIcons.icons[it].id
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
