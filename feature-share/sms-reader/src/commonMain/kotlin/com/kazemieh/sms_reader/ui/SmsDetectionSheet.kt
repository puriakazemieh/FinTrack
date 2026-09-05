package com.kazemieh.sms_reader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.EntityChip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.bank_label
import fintrack.core.designsystem.generated.resources.card_suffix_label
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.label_most_used
import fintrack.core.designsystem.generated.resources.notif_action_register
import fintrack.core.designsystem.generated.resources.action_delete_all
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.sms_sheet_sub
import fintrack.core.designsystem.generated.resources.sms_sheet_title
import fintrack.core.designsystem.generated.resources.source
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsDetectionSheet(
    drafts: List<SmsDraft>,
    categories: List<Category>,
    sources: List<Source>,
    mostUsedCategories: List<Category>,
    mostUsedSources: List<Source>,
    currency: String,
    onQuickRegister: (SmsDraft) -> Unit,
    onEdit: (SmsDraft) -> Unit,
    onDelete: (SmsDraft) -> Unit,
    onDeleteAll: () -> Unit,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                ScreenHeader(
                    title = stringResource(Res.string.sms_sheet_title),
                    sub = stringResource(Res.string.sms_sheet_sub),
                    onClose = onDismiss
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDeleteAll) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.action_delete_all),
                            color = GlassRed
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    items(drafts, key = { it.id }) { draft ->
                        SmsDraftItem(
                            draft = draft,
                            categories = categories,
                            sources = sources,
                            mostUsedCategories = mostUsedCategories,
                            mostUsedSources = mostUsedSources,
                            currency = currency,
                            onQuickRegister = { onQuickRegister(draft) },
                            onEdit = { onEdit(draft) },
                            onDelete = { onDelete(draft) },
                            onUpdate = onUpdateDraft,
                            onPick = { type ->
                                pickingDraft = draft
                                pickingType = type
                            }
                        )
                    }
                    
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }

    if (pickingDraft != null && pickingType != null) {
        if (pickingType == PickerType.Category) {
            CategoryPickerBottomSheet(
                transactionType = pickingDraft!!.type,
                onCategoryClick = { category ->
                    onUpdateDraft(pickingDraft!!.copy(categoryId = category.id))
                    pickingDraft = null
                    pickingType = null
                },
                onDismiss = {
                    pickingDraft = null
                    pickingType = null
                }
            )
        } else {
            SourcePickerBottomSheet(
                onSourceClick = { source ->
                    onUpdateDraft(pickingDraft!!.copy(sourceId = source.id))
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
}

enum class PickerType { Category, Source }

@Composable
fun SmsDraftItem(
    draft: SmsDraft,
    categories: List<Category>,
    sources: List<Source>,
    mostUsedCategories: List<Category>,
    mostUsedSources: List<Source>,
    currency: String,
    onQuickRegister: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (SmsDraft) -> Unit,
    onPick: (PickerType) -> Unit
) {
    val glassColors = LocalGlassColors.current

    val displayAmount = if (currency == "IRT") draft.amount / 10 else draft.amount

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = GlassGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.bank_label, draft.bankName),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        FintrackLabelMediumText(
                            text = displayAmount.toPersianPrice(),
                            color = if (draft.type == TransactionType.INCOME) GlassGreen else GlassRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackLabelSmallText(
                            text = (draft.sourceIdentifier?.let {
                                stringResource(
                                    Res.string.card_suffix_label,
                                    it
                                )
                            } ?: "") + draft.body.take(60),
                            maxLines = 1,
                            color = glassColors.text.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        )

                        FintrackLabelSmallText(
                            text = "${draft.confidence}%",
                            color = if (draft.confidence >= 90) GlassGreen else glassColors.text3,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = GlassRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Pickers - Matching AddTransaction UI style
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category Selection
                val selectedCategory = categories.find { it.id == draft.categoryId }
                FieldWithMostUsed(
                    label = stringResource(Res.string.category),
                    title = selectedCategory?.name ?: stringResource(Res.string.select_category),
                    iconRes = selectedCategory?.let { FinTrackIcons.findIcon(it.iconId).resource },
                    mostUsedItems = mostUsedCategories.filter { it.type == draft.type }.take(4),
                    onPick = { onPick(PickerType.Category) },
                    onItemClick = { item ->
                        val category = item as Category
                        onUpdate(draft.copy(categoryId = category.id))
                    }
                )

                // Source Selection
                val selectedSource = sources.find { it.id == draft.sourceId }
                FieldWithMostUsed(
                    label = stringResource(Res.string.source),
                    title = selectedSource?.name ?: stringResource(Res.string.select_source),
                    iconRes = selectedSource?.let { FinTrackIcons.findIcon(it.iconId).resource },
                    mostUsedItems = mostUsedSources.take(4),
                    onPick = { onPick(PickerType.Source) },
                    onItemClick = { item ->
                        val source = item as Source
                        onUpdate(draft.copy(sourceId = source.id))
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onQuickRegister,
                    modifier = Modifier.weight(1.5f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.notif_action_register),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(44.dp),
                    border = BorderStroke(1.dp, glassColors.glassEdge.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    FintrackLabelMediumText(
                        text = stringResource(Res.string.edit),
                        color = glassColors.text2
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldWithMostUsed(
    label: String,
    title: String,
    iconRes: Any?,
    mostUsedItems: List<Any>,
    onPick: () -> Unit,
    onItemClick: (Any) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Field(
            label = label,
            required = true,
            onClick = onPick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FintrackBodyMediumText(
                    text = title,
                    fontWeight = FontWeight.SemiBold
                )
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes as DrawableResource),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        if (mostUsedItems.isNotEmpty()) {
            MostUsedRow(
                items = mostUsedItems,
                onItemClick = onItemClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedRow(
    items: List<Any>,
    onItemClick: (Any) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.label_most_used),
            fontSize = 10.sp,
            color = LocalGlassColors.current.text3
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val name = when (item) {
                    is Category -> item.name
                    is Source -> item.name
                    else -> ""
                }
                
                Chip(
                    color = GlassGreen,
                    onClick = { onItemClick(item) }
                ) {
                    FintrackLabelSmallText(
                        text = name,
                        color = GlassGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
