package com.kazemieh.sms_reader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.EntityChip
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.picker.FinTrackIcons
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.amount_rial_label
import fintrack.core.designsystem.generated.resources.bank_label
import fintrack.core.designsystem.generated.resources.card_suffix_label
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.notif_action_register
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.sms_sheet_sub
import fintrack.core.designsystem.generated.resources.sms_sheet_title
import fintrack.core.designsystem.generated.resources.source
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetectionSheet(
    drafts: List<SmsDraft>,
    categories: List<Category>,
    sources: List<Source>,
    onDraftClick: (SmsDraft) -> Unit,
    onIgnore: (SmsDraft) -> Unit,
    onUpdateDraft: (SmsDraft) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var pickingDraft by remember { mutableStateOf<SmsDraft?>(null) }
    var pickingType by remember { mutableStateOf<PickerType?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        AddFrame(
            title = stringResource(Res.string.sms_sheet_title),
            sub = stringResource(Res.string.sms_sheet_sub),
            primaryLabel = stringResource(Res.string.confirm),
            onPrimaryClick = onDismiss,
            onClose = onDismiss,
            showHero = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(drafts, key = { it.id }) { draft ->
                    SmsDraftItem(
                        draft = draft,
                        categories = categories,
                        sources = sources,
                        onClick = { onDraftClick(draft) },
                        onIgnore = { onIgnore(draft) },
                        onUpdate = onUpdateDraft,
                        onPick = { type ->
                            pickingDraft = draft
                            pickingType = type
                        }
                    )
                }
            }
        }
    }

    if (pickingDraft != null && pickingType != null) {
        val title = if (pickingType == PickerType.Category) stringResource(Res.string.category) else stringResource(Res.string.source)
        val items = if (pickingType == PickerType.Category) {
            categories.map { it.toItemUi() }.toSet()
        } else {
            sources.map { it.toItemUi() }.toSet()
        }

        SelectableListBottomSheet(
            title = title,
            items = items,
            onConfirm = { selected, _ ->
                val selectedId = selected.firstOrNull()?.id
                if (selectedId != null) {
                    val updated = if (pickingType == PickerType.Category) {
                        pickingDraft!!.copy(categoryId = selectedId)
                    } else {
                        pickingDraft!!.copy(sourceId = selectedId)
                    }
                    onUpdateDraft(updated)
                }
                pickingDraft = null
                pickingType = null
            },
            onDismiss = {
                pickingDraft = null
                pickingType = null
            }
        )
    }
}

enum class PickerType { Category, Source }

@Composable
fun SmsDraftItem(
    draft: SmsDraft,
    categories: List<Category>,
    sources: List<Source>,
    onClick: () -> Unit,
    onIgnore: () -> Unit,
    onUpdate: (SmsDraft) -> Unit,
    onPick: (PickerType) -> Unit
) {
    val glassColors = LocalGlassColors.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(glassColors.glass)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = GlassBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.bank_label, draft.bankName),
                            fontWeight = FontWeight.Bold
                        )
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.amount_rial_label, draft.amount.toPersianPrice()),
                            color = GlassBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackLabelSmallText(
                            text = (draft.sourceIdentifier?.let { stringResource(Res.string.card_suffix_label, it) } ?: "") + draft.body.take(60) + "...",
                            maxLines = 1,
                            color = glassColors.text.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )
                        
                        FintrackLabelSmallText(
                            text = "${draft.confidence}%",
                            color = if (draft.confidence >= 90) GlassBlue else glassColors.text3,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onIgnore) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = GlassRed.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Picker
                val selectedCategory = categories.find { it.id == draft.categoryId }
                Box(modifier = Modifier.weight(1f)) {
                    EntityChip(
                        name = selectedCategory?.name ?: stringResource(Res.string.select_category),
                        iconRes = selectedCategory?.let { FinTrackIcons.findIcon(it.iconId).resource },
                        tint = GlassBlue,
                        onClick = { onPick(PickerType.Category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Source Picker
                val selectedSource = sources.find { it.id == draft.sourceId }
                Box(modifier = Modifier.weight(1f)) {
                    EntityChip(
                        name = selectedSource?.name ?: stringResource(Res.string.select_source),
                        iconRes = selectedSource?.let { FinTrackIcons.findIcon(it.iconId).resource },
                        tint = GlassBlue,
                        onClick = { onPick(PickerType.Source) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GlassBlue, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                FintrackLabelMediumText(text = stringResource(Res.string.notif_action_register), color = Color.White)
            }
        }
    }
}

private fun Category.toItemUi() = ItemUi(
    id = id ?: 0L,
    title = UiText.DynamicString(name),
    iconId = iconId,
    colorId = colorId
)

private fun Source.toItemUi() = ItemUi(
    id = id ?: 0L,
    title = UiText.DynamicString(name),
    iconId = iconId,
    colorId = colorId
)
