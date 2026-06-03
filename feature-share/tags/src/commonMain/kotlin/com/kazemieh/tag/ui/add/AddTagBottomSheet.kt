package com.kazemieh.tag.ui.add

import com.kazemieh.common.model.Tag
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.picker.ColorIconPickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
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
                            onDismiss()
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
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
fun AddTagContent(
    state: AddTagState,
    onIntent: (AddTagIntent) -> Unit
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
        title = if (state.mode == AddTagMode.Add) stringResource(Res.string.title_new_tag) else stringResource(Res.string.title_edit_tag),
        sub = stringResource(Res.string.title_tag_management),
        primaryLabel = stringResource(Res.string.btn_save_tag),
        onPrimaryClick = { onIntent(AddTagIntent.Save) },
        onClose = { onIntent(AddTagIntent.OnDismiss) },
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
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_search_in, stringResource(Res.string.tags)).replace(stringResource(Res.string.hint_search_in, ""), ""), color = GlassText3) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = GlassText, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Field(label = stringResource(Res.string.description_label)) {
                    TextField(
                        value = state.draft.description.orEmpty(),
                        onValueChange = { onIntent(AddTagIntent.UpdateDescription(it)) },
                        placeholder = { FintrackBodyMediumText(text = stringResource(Res.string.hint_optional_note), color = GlassText3) },
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
                        FintrackLabelMediumText(text = stringResource(Res.string.label_color), color = GlassText3)
                        ColorSwatches(
                            colors = colors,
                            pickedIndex = selectedColorIndex,
                            onColorPick = { onIntent(AddTagIntent.SetColorIcon(rainbowColors[it].id, state.draft.iconId)) }
                        )
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(text = stringResource(Res.string.label_icon), color = GlassText3)
                        IconGrid(
                            icons = FinTrackIcons.icons,
                            pickedIndex = selectedIconIndex,
                            color = rainbowColors[selectedColorIndex].color,
                            onIconPick = { onIntent(AddTagIntent.SetColorIcon(state.draft.colorId, FinTrackIcons.icons[it].id)) }
                        )
                    }
                }
            }
        }
    }
}
